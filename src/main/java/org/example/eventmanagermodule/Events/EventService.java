package org.example.eventmanagermodule.Events;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.example.eventmanagermodule.Events.Converter.EventConverterEntity;
import org.example.eventmanagermodule.Events.Registration.EventRegistration;
import org.example.eventmanagermodule.Events.Registration.EventRegistrationRepository;
import org.example.eventmanagermodule.Location.LocationRepository;
import org.example.eventmanagermodule.User.User;
import org.example.eventmanagermodule.User.UserEntity;
import org.example.eventmanagermodule.User.UserRepository;
import org.example.eventmanagermodule.User.UserService;
import org.example.eventmanagermodule.security.CustomerDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


import static java.time.LocalDateTime.now;

@Service
public class EventService {

    private final static Logger log = LoggerFactory.getLogger(EventService.class);
    private final LocalDateTime pointInTime;
    private final ObjectMapper objectMapper;
    private final CustomerDetailsService userDetailsService;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final EventConverterEntity eventConverterEntity;
    private final UserService userService;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserRepository userRepository;


    public EventService(@Value("${event.date.min}") String pointInTimeStr, ObjectMapper objectMapper, CustomerDetailsService userDetailsService, LocationRepository locationRepository, EventRepository eventRepository, EventConverterEntity eventConverterEntity, UserService userService, EventRegistrationRepository eventRegistrationRepository, UserRepository userRepository) {
        this.pointInTime = LocalDateTime.parse(pointInTimeStr);
        this.objectMapper = objectMapper;
        this.userDetailsService = userDetailsService;
        this.locationRepository = locationRepository;
        this.eventRepository = eventRepository;
        this.eventConverterEntity = eventConverterEntity;
        this.userService = userService;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.userRepository = userRepository;
    }

    public Event createEvent(EventCreateRequestDto eventCreateRequestDto) {

        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();

        if (!locationRepository.existsById(eventCreateRequestDto.getLocationId())) {
            throw new EntityNotFoundException("Location with id " + eventCreateRequestDto.getLocationId() + " not found");
        }

        var location = locationRepository.findById(eventCreateRequestDto.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));

        Long locationId = location.getId();

        if (eventCreateRequestDto.getMaxPlaces() > location.getCapacity()) {
            throw new IllegalArgumentException("Max places exceeds location capacity");
        }

        if (!eventCreateRequestDto.getDate().isAfter(pointInTime)) {
            throw new IllegalArgumentException(
                    String.format("Дата мероприятия должна быть позже %s", pointInTime.toLocalDate())
            );
        }

        var domainEvent = new Event(
                null,
                eventCreateRequestDto.getName(),
                ownerId,
                locationId,
                eventCreateRequestDto.getMaxPlaces(),
                0,
                eventCreateRequestDto.getDate(),
                eventCreateRequestDto.getCost(),
                eventCreateRequestDto.getDuration(),
                EventStatus.WAIT_START
        );
        var savedEntity = eventRepository.save(eventConverterEntity.toEntity(domainEvent));

        var savedDomain = eventConverterEntity.toDomain(savedEntity);

        return savedDomain;
    }

    public void deleteEvent(Long eventId) {

        var searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();
        String userRole = currentUser.role().name();

        var eventOwnerId = searchEvent.getOwner().getId();

        if (!userRole.equals("ADMIN") && !Objects.equals(ownerId, eventOwnerId)) {
            throw new AccessDeniedException("Недостаточно прав для удаления мероприятия");
        }
        eventRepository.delete(searchEvent);

    }

    public Event getEvent(Long eventId) {
        var searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));

        return eventConverterEntity.toDomain(searchEvent);
    }


    public Event updateEvent(Long eventId,
                             EventUpdateRequestDto eventUpdateRequestDto) {

        var searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));

        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();
        String userRole = currentUser.role().name();

        var eventOwnerId = searchEvent.getOwner().getId();

        if (!userRole.equals("ADMIN") && !Objects.equals(ownerId, eventOwnerId)) {
            throw new AccessDeniedException("Недостаточно прав для удаления мероприятия");
        }

        if (!eventUpdateRequestDto.getDate().isAfter(pointInTime)) {
            throw new IllegalArgumentException(
                    String.format("Дата мероприятия должна быть позже %s", pointInTime.toLocalDate())
            );
        }
        if (eventUpdateRequestDto.getMaxPlaces() < searchEvent.getOccupiedPlaces()) {
            throw new IllegalArgumentException("Не можем уменьшить количество мест меньше уже записанных участников");
        }

        if (!locationRepository.existsById(eventUpdateRequestDto.getLocationId())) {
            throw new EntityNotFoundException("Location with id " + eventUpdateRequestDto.getLocationId() + " not found");
        }


        var updatedEvent = new Event(
                searchEvent.getId(),
                eventUpdateRequestDto.getName(),
                ownerId,
                eventUpdateRequestDto.getLocationId(),
                eventUpdateRequestDto.getMaxPlaces(),
                searchEvent.getOccupiedPlaces(),
                eventUpdateRequestDto.getDate(),
                eventUpdateRequestDto.getCost(),
                eventUpdateRequestDto.getDuration(),
                EventStatus.WAIT_START
        );
        eventRepository.save(eventConverterEntity.toEntity(updatedEvent));
        return updatedEvent;
    }


    public List<Event> getMyEvent() {
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();

        List<EventEntity> searchedEvent = eventRepository.findAllByOwnerId(ownerId);
        if (searchedEvent.isEmpty()) {
            return Collections.emptyList(); // просто пустой список, без исключения
        }

        return searchedEvent.stream()
                .map(eventConverterEntity::toDomain)
                .toList();

    }

    public List<Event> getSearchEvent(EventSearchRequestDto eventSearchRequestDto) {

        var eventSearchFilter = new EventSearchFilter(
                eventSearchRequestDto.getName(),
                eventSearchRequestDto.getPlacesMin(),
                eventSearchRequestDto.getPlacesMax(),
                eventSearchRequestDto.getDateStartAfter(),
                eventSearchRequestDto.getDateStartBefore(),
                eventSearchRequestDto.getCostMin(),
                eventSearchRequestDto.getCostMax(),
                eventSearchRequestDto.getDurationMin(),
                eventSearchRequestDto.getDurationMax(),
                eventSearchRequestDto.getLocationId(),
                eventSearchRequestDto.getStatus()
        );

        List<EventEntity> entities = eventRepository.searchEvents(
                eventSearchFilter.name(),
                eventSearchFilter.placesMin(),
                eventSearchFilter.placesMax(),
                eventSearchFilter.dateStartAfter(),
                eventSearchFilter.dateStartBefore(),
                eventSearchFilter.costMin(),
                eventSearchFilter.costMax(),
                eventSearchFilter.durationMin(),
                eventSearchFilter.durationMax(),
                eventSearchFilter.locationId(),
                eventSearchFilter.status()
        );
        return entities.stream()
                .map(eventConverterEntity::toDomain)
                .toList();
    }


    @Transactional
    public Event registerEvent(Long eventId) {

        var searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));


        User currentUser = getCurrentUser();
        UserEntity userEntity = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (eventRegistrationRepository.existsByEventEntityAndUserEntity(searchEvent, userEntity)) {
            throw new IllegalArgumentException("Пользователь уже зарегистрирован на это мероприятие");
        }
        var registeredEntity = new EventRegistration(
                null,
                searchEvent,
                userEntity,
                now()
        );
        log.info("Сохраняем регистрацию: event={}, user={}", searchEvent.getId(), userEntity.getId());
        eventRegistrationRepository.save(registeredEntity);


        if (searchEvent.getStatus() == EventStatus.CLOSED || searchEvent.getStatus() == EventStatus.FINISHED) {
            throw new IllegalArgumentException("Невозможно зарегистрироваться на мероприятие, которое завершено или отменено");
        }

        int freePlaces = searchEvent.getMaxPlaces() - searchEvent.getOccupiedPlaces();
        if (freePlaces <= 0) {
            throw new IllegalArgumentException("Невозможно зарегистрироваться на мероприятие, так как нет свободных мест");
        }

        searchEvent.setOccupiedPlaces(searchEvent.getOccupiedPlaces() + 1);
        var savedEntity = eventRepository.save(searchEvent);
        return eventConverterEntity.toDomain(savedEntity);

    }

    public List<Event> myRegisterEvent() {

        User currentUser = getCurrentUser();
        UserEntity userEntity = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        var eventEntity = eventRegistrationRepository.myRegisterEvent(userEntity);
        return eventEntity.stream()
                .map(eventConverterEntity::toDomain)
                .toList();
    }


    @Transactional
    public void deleteRegisterEvent(Long eventId) {
        User currentUser = getCurrentUser();
        UserEntity userEntity = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        var searchEvent = eventRepository.findById(eventId) // проверка мероприятия в БД
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
        if (searchEvent.getStatus() == EventStatus.FINISHED || searchEvent.getStatus() == EventStatus.STARTED) {
            throw new IllegalArgumentException("Невозможно отменить регистрацию на мероприятие, которое уже началось или завершено");
        }

        var registration = eventRegistrationRepository
                .findByEventEntityAndUserEntity(searchEvent, userEntity)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не зарегистрирован на мероприятие"));
        eventRegistrationRepository.delete(registration);
    }


    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("Пользователь не аутентифицирован или неправильный тип principal");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userService.findByLogin(userDetails.getUsername());
    }
}

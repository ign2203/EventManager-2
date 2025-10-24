package org.example.eventmanagermodule.Events;

import jakarta.persistence.EntityNotFoundException;
import org.example.eventmanagermodule.Events.Converter.EventConverterEntity;
import org.example.eventmanagermodule.Events.Registration.EventRegistration;
import org.example.eventmanagermodule.Events.Registration.EventRegistrationRepository;
import org.example.eventmanagermodule.Location.LocationEntity;
import org.example.eventmanagermodule.Location.LocationRepository;
import org.example.eventmanagermodule.User.User;
import org.example.eventmanagermodule.User.UserEntity;
import org.example.eventmanagermodule.User.UserRepository;
import org.example.eventmanagermodule.User.UserService;
import org.example.eventmanagermodule.producer.*;
import org.example.eventmanagermodule.producer.status.EventStatusChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final EventConverterEntity eventConverterEntity;
    private final UserService userService;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserRepository userRepository;
    private final EventProducerService eventProducerService;

    public EventService(@Value("${event.date.min}") String pointInTimeStr, LocationRepository locationRepository, EventRepository eventRepository, EventConverterEntity eventConverterEntity, UserService userService, EventRegistrationRepository eventRegistrationRepository, UserRepository userRepository, EventProducerService eventProducerService) {
        this.pointInTime = LocalDateTime.parse(pointInTimeStr);
        this.locationRepository = locationRepository;
        this.eventRepository = eventRepository;
        this.eventConverterEntity = eventConverterEntity;
        this.userService = userService;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.userRepository = userRepository;
        this.eventProducerService = eventProducerService;
    }

    public Event postCreateEvent(EventCreateRequestDto eventCreateRequestDto) {
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();
        if (!locationRepository.existsById(eventCreateRequestDto.getLocationId())) {
            throw new EntityNotFoundException("Location with id " + eventCreateRequestDto.getLocationId() + " not found");
        }
        LocationEntity location = locationRepository.findById(eventCreateRequestDto.getLocationId())
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
        Event domainEvent = new Event(
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
        EventEntity savedEntity = eventRepository.save(eventConverterEntity.toEntity(domainEvent));
        return eventConverterEntity.toDomain(savedEntity);

    }

    public void deleteEvent(Long eventId) {
        EventEntity searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();
        String userRole = currentUser.role().name();
        Long eventOwnerId = searchEvent.getOwner().getId();
        if (!userRole.equals("ADMIN") && !Objects.equals(ownerId, eventOwnerId)) {
            throw new AccessDeniedException("Недостаточно прав для удаления мероприятия");
        }
        eventRepository.delete(searchEvent);
    }

    public Event getEvent(Long eventId) {
        EventEntity searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
        return eventConverterEntity.toDomain(searchEvent);
    }

    public Event putUpdateEvent(Long eventId,
                                EventUpdateRequestDto eventUpdateRequestDto) {
        var searchEvent =  getAuthorizedEvent(eventId);
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();
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
        Event updatedEvent = new Event(
                searchEvent.getId(),
                eventUpdateRequestDto.getName(),
                ownerId,
                eventUpdateRequestDto.getLocationId(),
                eventUpdateRequestDto.getMaxPlaces(),
                searchEvent.getOccupiedPlaces(),
                eventUpdateRequestDto.getDate(),
                eventUpdateRequestDto.getCost(),
                eventUpdateRequestDto.getDuration(),
                searchEvent.getStatus()
        );
        FieldChangeString nameChange = new  FieldChangeString(searchEvent.getName(),updatedEvent.name());
        FieldChangeInteger maxPlacesChange = new FieldChangeInteger(searchEvent.getMaxPlaces(), updatedEvent.maxPlaces());
        FieldChangeDateTime dateChange = new FieldChangeDateTime(searchEvent.getDate(),updatedEvent.date());
        FieldChangeDecimal costChange = new FieldChangeDecimal(searchEvent.getCost(),updatedEvent.cost());
        FieldChangeInteger durationChange = new FieldChangeInteger(searchEvent.getDuration(),updatedEvent.duration());
        FieldChangeLong locationIdChange = new FieldChangeLong(searchEvent.getLocation().getId(),updatedEvent.locationId());
        EventChangeNotification notification = new EventChangeNotification(// забыл добавить конструктор
                updatedEvent.id(),
                nameChange,
                maxPlacesChange,
                dateChange,
                costChange,
                durationChange,
                locationIdChange
        );
        eventRepository.save(eventConverterEntity.toEntity(updatedEvent));
        eventProducerService.sendEventChange(notification);
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
        EventSearchFilter eventSearchFilter = new EventSearchFilter(
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
    public Event postRegisterEvent(Long eventId) {
        EventEntity searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
        User currentUser = getCurrentUser();
        UserEntity userEntity = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (eventRegistrationRepository.existsByEventEntityAndUserEntity(searchEvent, userEntity)) {
            throw new IllegalArgumentException("Пользователь уже зарегистрирован на это мероприятие");
        }
        EventRegistration registeredEntity = new EventRegistration(
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
        EventEntity savedEntity = eventRepository.save(searchEvent);
        return eventConverterEntity.toDomain(savedEntity);
    }

    @Transactional(readOnly = true)
    public List<Event> getMyRegisterEvent() {
        User currentUser = getCurrentUser();
        UserEntity userEntity = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        var eventEntity = eventRegistrationRepository.myRegisterEvent(userEntity);
        if (eventEntity.isEmpty()) {
            return List.of();
        }
        return eventEntity.stream()
                .map(eventConverterEntity::toDomain)
                .toList();
    }

    @Transactional
    public void deleteRegisterEvent(Long eventId) {
        User currentUser = getCurrentUser();
        UserEntity userEntity = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        EventEntity searchEvent = eventRepository.findById(eventId) // проверка мероприятия в БД
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
        if (searchEvent.getStatus() == EventStatus.FINISHED || searchEvent.getStatus() == EventStatus.STARTED) {
            throw new IllegalArgumentException("Невозможно отменить регистрацию на мероприятие, которое уже началось или завершено");
        }
        EventRegistration registration = eventRegistrationRepository
                .findByEventEntityAndUserEntity(searchEvent, userEntity)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не зарегистрирован на мероприятие"));
        eventRegistrationRepository.delete(registration);
        int newPlaces = Math.max(0, searchEvent.getOccupiedPlaces() - 1);
        searchEvent.setOccupiedPlaces(newPlaces);
        eventRepository.save(searchEvent);
    }


    @Transactional
    public void closeEvent(Long eventId) {
       EventEntity searchEvent =  getAuthorizedEvent(eventId);
        EventStatus oldStatus = searchEvent.getStatus();
        searchEvent.setStatus(EventStatus.CLOSED);
        EventStatusChangeNotification  eventStatusChangeNotification = new EventStatusChangeNotification(
                searchEvent.getId(),
                oldStatus,
                EventStatus.CLOSED
        );
        eventRepository.save(searchEvent);
        eventProducerService.sendStatusChangeNotification(eventStatusChangeNotification);
    }

    private  User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Пользователь не аутентифицирован");
        }
        String username = authentication.getName(); // loginFromToken
        return userService.findByLogin(username);
    }

    private EventEntity getAuthorizedEvent(Long eventId) {
        EventEntity searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();
        String userRole = currentUser.role().name();
        Long eventOwnerId = searchEvent.getOwner().getId();//id пользователя кто создал мероприятие
        if (!userRole.equals("ADMIN") && !Objects.equals(ownerId, eventOwnerId)) {
            throw new AccessDeniedException("Недостаточно прав для обновления мероприятия");
        }
        if (!searchEvent.getStatus().equals(EventStatus.WAIT_START)) {
            throw new AccessDeniedException("Изменение статуса невозможно: мероприятие уже началось или завершено.");
        }
        return searchEvent;
    }
}

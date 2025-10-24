package org.example.eventmanagermodule.Events;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.example.eventmanagermodule.Events.Converter.EventConverterEntity;
import org.example.eventmanagermodule.Events.Registration.EventRegistration;
import org.example.eventmanagermodule.Events.Registration.EventRegistrationRepository;
import org.example.eventmanagermodule.Events.dto.EventCreateRequestDto;
import org.example.eventmanagermodule.Events.dto.EventSearchRequestDto;
import org.example.eventmanagermodule.Events.dto.EventUpdateRequestDto;
import org.example.eventmanagermodule.Location.LocationEntity;
import org.example.eventmanagermodule.Location.LocationRepository;
import org.example.eventmanagermodule.User.User;
import org.example.eventmanagermodule.User.UserEntity;
import org.example.eventmanagermodule.User.UserRepository;
import org.example.eventmanagermodule.User.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import static java.time.LocalDateTime.now;

@Service
public class EventService {
    private final static Logger log = LoggerFactory.getLogger(EventService.class);
    @Value("${event.date.min}")
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final EventConverterEntity eventConverterEntity;
    private final UserService userService;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserRepository userRepository;

    public EventService(LocationRepository locationRepository,
                        EventRepository eventRepository,
                        EventConverterEntity eventConverterEntity,
                        UserService userService,
                        EventRegistrationRepository eventRegistrationRepository,
                        UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.eventRepository = eventRepository;
        this.eventConverterEntity = eventConverterEntity;
        this.userService = userService;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.userRepository = userRepository;
    }

    public Event postCreateEvent(EventCreateRequestDto eventCreateRequestDto) {
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();
        LocationEntity location = locationRepository.findById(eventCreateRequestDto.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));
        Long locationId = location.getId();
        if (eventCreateRequestDto.getMaxPlaces() > location.getCapacity()) {
            throw new IllegalArgumentException("Max places exceeds location capacity");
        }
        if (!eventCreateRequestDto.getDate().isAfter(LocalDateTime.now())) {
            throw new ValidationException(
                    String.format("The event date must be after %s", LocalDateTime.now().toLocalDate())
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
        log.info("User id={} is creating event '{}' at location id={}", ownerId, eventCreateRequestDto.getName(), locationId);
        EventEntity savedEntity = eventRepository.save(eventConverterEntity.toEntity(domainEvent));
        log.info("Event '{}' (id={}) successfully created by user id={}", savedEntity.getName(), savedEntity.getId(), ownerId);
        return eventConverterEntity.toDomain(savedEntity);
    }

    public void deleteEvent(Long eventId) {
        EventEntity searchEvent = verifyEventAccess(eventId);
        log.info("Deleting event with id={}", eventId);
        eventRepository.delete(searchEvent);
        log.info("Event with id={} successfully deleted", eventId);
    }

    public Event getEvent(Long eventId) {
        EventEntity searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
        return eventConverterEntity.toDomain(searchEvent);
    }

    public Event putUpdateEvent(Long eventId,
                                EventUpdateRequestDto eventUpdateRequestDto) {
        EventEntity searchEvent = verifyEventAccess(eventId);
        if (!searchEvent.getStatus().equals(EventStatus.WAIT_START)) {
            throw new IllegalStateException("It is not possible to update an event if its status is CLOSED, FINISHED, or HAS WAIT_START");
        }
        if (!eventUpdateRequestDto.getDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    String.format("The event date must be after %s", LocalDateTime.now().toLocalDate())
            );
        }
        if (eventUpdateRequestDto.getMaxPlaces() < searchEvent.getOccupiedPlaces()) {
            throw new ValidationException("We cannot reduce the number of places to less than the number of participants already registered.");
        }
        if (!locationRepository.existsById(eventUpdateRequestDto.getLocationId())) {
            throw new EntityNotFoundException("Location with id " + eventUpdateRequestDto.getLocationId() + " not found");
        }
        Event updatedEvent = new Event(
                searchEvent.getId(),
                eventUpdateRequestDto.getName(),
                getCurrentUser().id(),
                eventUpdateRequestDto.getLocationId(),
                eventUpdateRequestDto.getMaxPlaces(),
                searchEvent.getOccupiedPlaces(),
                eventUpdateRequestDto.getDate(),
                eventUpdateRequestDto.getCost(),
                eventUpdateRequestDto.getDuration(),
                searchEvent.getStatus()
        );
        Long userId = getCurrentUser().id();
        log.info("User id={} is updating event '{}' at location id={}", userId, updatedEvent.name(), updatedEvent.locationId());
        eventRepository.save(eventConverterEntity.toEntity(updatedEvent));
        log.info("Event '{}' (id={}) successfully updated by user id={}", updatedEvent.name(), updatedEvent.id(), userId);
        return updatedEvent;
    }

    public List<Event> getMyEvent() {
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();
        List<EventEntity> searchedEvent = eventRepository.findAllByOwnerId(ownerId);
        log.info("Retrieving events created by user id={} ({} events found)", ownerId, searchedEvent.size());
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
            throw new ValidationException("The user is already registered for this event");
        }
        log.info("User id={} is registering for event id={} (status={})", userEntity.getId(), searchEvent.getId(), searchEvent.getStatus());
        EventRegistration registeredEntity = new EventRegistration(
                null,
                searchEvent,
                userEntity,
                now()
        );
        if (searchEvent.getStatus() == EventStatus.CLOSED || searchEvent.getStatus() == EventStatus.FINISHED) {
            throw new ValidationException("You cannot register for an event that has FINISHED or been CLOSED");
        }
        int freePlaces = searchEvent.getMaxPlaces() - searchEvent.getOccupiedPlaces();
        if (freePlaces <= 0) {
            throw new ValidationException("You cannot register for the event because there are no available seats");
        }
        eventRegistrationRepository.save(registeredEntity);
        searchEvent.setOccupiedPlaces(searchEvent.getOccupiedPlaces() + 1);
        EventEntity savedEntity = eventRepository.save(searchEvent);
        log.info("User id={} successfully registered for event id={}", userEntity.getId(), searchEvent.getId());
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
        EventEntity searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
        if (searchEvent.getStatus() == EventStatus.FINISHED || searchEvent.getStatus() == EventStatus.STARTED) {
            throw new ValidationException("Cannot cancel registration for an event that has already started or finished");
        }
        EventRegistration registration = eventRegistrationRepository
                .findByEventEntityAndUserEntity(searchEvent, userEntity)
                .orElseThrow(() -> new ValidationException("User is not registered for this event"));// нужно подумать по поводу исключения
        eventRegistrationRepository.delete(registration);
        int newPlaces = Math.max(0, searchEvent.getOccupiedPlaces() - 1);
        searchEvent.setOccupiedPlaces(newPlaces);
        eventRepository.save(searchEvent);
        log.info("Registration removed: user id={} from event id={}", userEntity.getId(), eventId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("The user is not authenticated");
        }
        String username = authentication.getName();
        log.info("Authenticated request by user '{}'", username);
        return userService.findByLogin(username);
    }

    public EventEntity verifyEventAccess(Long eventId) {
        EventEntity searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();
        String userRole = currentUser.role().name();
        Long eventOwnerId = searchEvent.getOwner().getId();
        if (!userRole.equals("ADMIN") && !Objects.equals(ownerId, eventOwnerId)) {
            log.warn("Access denied: user={} attempted to modify event={}", ownerId, eventId);
            throw new AccessDeniedException("Insufficient rights to perform the operation");
        }
        return searchEvent;
    }
}
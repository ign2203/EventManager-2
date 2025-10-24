package org.example.eventmanagermodule.Events;

import jakarta.validation.Valid;
import org.example.eventmanagermodule.Events.Converter.EventConverterDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private final static Logger log = LoggerFactory.getLogger(EventController.class);
    private final EventService eventService;
    private final EventConverterDto converterDto;

    public EventController(EventService eventService, EventConverterDto converterDto) {
        this.eventService = eventService;
        this.converterDto = converterDto;
    }

    @PostMapping()
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<EventDto> postCreateEvent(
            @RequestBody @Valid EventCreateRequestDto eventCreateRequestDto) {
        log.info("REST request to save Event : {}", eventCreateRequestDto);
        Event eventDomain = eventService.postCreateEvent(eventCreateRequestDto);
        log.info("Service Created Event : {}", eventDomain);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(converterDto.toDto(eventDomain));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<Void> deleteEvent
            (@PathVariable(name = "eventId") Long eventId) {
        log.info("REST request to delete Event : {}", eventId);
        eventService.deleteEvent(eventId);
        log.info("Service Deleted Event : {}", eventId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<EventDto> getEvent(
            @PathVariable(name = "eventId") Long eventId) {
        log.info("REST request to get Event : {}", eventId);
        Event searchEventDomain = eventService.getEvent(eventId);
        log.info("Service Returned Event : {}", searchEventDomain);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(converterDto.toDto(searchEventDomain));
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<EventDto> putUpdateEvent(
            @PathVariable(name = "eventId") Long eventId,
            @RequestBody @Valid EventUpdateRequestDto eventUpdateRequestDto) {
        log.info("REST request to update Event: {}", eventUpdateRequestDto);
        Event updateEventDomain = eventService.putUpdateEvent(eventId, eventUpdateRequestDto);
        log.info("Service Updated Event : {}", updateEventDomain);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(converterDto.toDto(updateEventDomain));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<List<EventDto>> getMyEvent() {
        log.info("REST request to get My Events");
        List<Event> myEventDomain = eventService.getMyEvent();
        List<EventDto> eventDtoList = myEventDomain
                .stream()
                .map(converterDto::toDto)
                .toList();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventDtoList);
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<List<EventDto>> getSearchEvent(
            @Valid EventSearchRequestDto eventSearchRequestDto) {
        log.info("REST request to get Search all events by filter : {}", eventSearchRequestDto);
        List<Event> searchEventDomain = eventService.getSearchEvent(eventSearchRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(searchEventDomain.stream()
                        .map(converterDto::toDto)
                        .toList());
    }

    @PostMapping("/registrations/{eventId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<EventDto> postRegisterEvent(
            @PathVariable(name = "eventId") Long eventId) {
        log.info("REST request to register Event : {}", eventId);
        Event registerEvent = eventService.postRegisterEvent(eventId);
        log.info("Service Registered Event : {}", registerEvent);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(converterDto.toDto(registerEvent));
    }

    @GetMapping("/registrations/my")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<List<EventDto>> getMyRegisterEvent() {
        log.info("REST request All events for which the user is registered");
        List<Event> myRegisterEvent = eventService.getMyRegisterEvent();
        log.info("Service Returned my Event : {}", myRegisterEvent);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(myRegisterEvent
                        .stream()
                        .map(converterDto::toDto)
                        .toList());
    }

    @DeleteMapping("/registrations/cancel/{eventId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<Void> deleteRegisterEvent(
            @PathVariable(name = "eventId") Long eventId) {
        log.info("REST request to cancelling registration for an event");
        eventService.deleteRegisterEvent(eventId);
        log.info("Service Deleted registration Event : {}", eventId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}

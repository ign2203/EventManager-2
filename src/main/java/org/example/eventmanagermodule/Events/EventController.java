package org.example.eventmanagermodule.Events;

import jakarta.validation.Valid;
import org.example.eventmanagermodule.Events.Converter.EventConverterDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<EventDto> createEvent(
            @RequestBody @Valid EventCreateRequestDto eventCreateRequestDto) {
        log.info("REST request to save Event : {}", eventCreateRequestDto);

        var eventDomain = eventService.createEvent(eventCreateRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(converterDto.toDto(eventDomain));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<Void> deleteEvent(@PathVariable(name = "eventId") Long eventId) {
        log.info("DELETE /events/{} requested by {}", eventId, SecurityContextHolder.getContext().getAuthentication().getName());
        log.info("REST request to delete Event : {}", eventId);
        eventService.deleteEvent(eventId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<EventDto> getEvent(
            @PathVariable(name = "eventId") Long eventId) {
        log.info("REST request to get Event : {}", eventId);
        var searchEventDomain = eventService.getEvent(eventId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(converterDto.toDto(searchEventDomain));
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<EventDto> updateEvent(
            @PathVariable(name = "eventId") Long eventId,
            @RequestBody @Valid EventUpdateRequestDto eventUpdateRequestDto) {

        log.info("REST request to update Event: {}", eventUpdateRequestDto);
        var updateEventDomain = eventService.updateEvent(eventId, eventUpdateRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(converterDto.toDto(updateEventDomain));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<List<EventDto>> getMyEvent() {
        log.info("REST request to get My Events");
        var myEventDomain = eventService.getMyEvent();

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
            @Valid EventSearchRequestDto eventSearchRequestDto)
    {
        log.info("REST request to get Search all events by filter : {}", eventSearchRequestDto);
        var searchEventDomain = eventService.getSearchEvent(eventSearchRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(searchEventDomain.stream()
                        .map(converterDto::toDto)
                        .toList());
    }


    @PostMapping("/registrations/{eventId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<EventDto> registerEvent(
            @PathVariable(name = "eventId") Long eventId) {

        log.info("REST request to register Event : {}", eventId);
        var registerEvent = eventService.registerEvent(eventId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(converterDto.toDto(registerEvent));
    }

    @GetMapping("/registrations/my")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<List<EventDto>> myRegisterEvent() {
        log.info("REST request All events for which the user is registered");
        var myRegisterEvent = eventService.myRegisterEvent();
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
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}

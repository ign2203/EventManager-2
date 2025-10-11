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

/*
Учтите валидацию всех полей. Все поля обязательные при создании, также должна быть соблюдена логика (например время не может быть в прошлом, стомость > 0 и т.д.)
Локация должна существовать и ее вместимость должна позволять провести это мероприятие,
 т.е. кол-во мест должно быть достаточно для всех участников.
 */

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


    /*
    Доступно только ADMIN, либо создателю мероприятия. Т.е. роль из JWT токена должна быть ADMIN,
    либо userId должен быть равен создателю мероприятия.
    Должно быть реализовано "мягкое(soft) удаление" - на самом деле строка в БД не удалется, только меняется статус мероприятия на CANCELLED.
     При этом нужно учесть то, что не каждое мероприятие можно отменить (можно отменить только те мероприятия, которые еще не начались).
     */
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


    /*
    Доступно только ADMIN, либо создателю мероприятия. Т.е. роль из JWT токена должна быть ADMIN, либо userId должен быть равен создателю мероприятия.
    Учтите то, как можно менять мероприятие.
    Валидируйте входные значения, например maxPlaces должно быть больше, чем уже записанных пользователей, стоимость > 0, длительность > 0 и т.д
     */
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

    //Все мероприятия созданные пользователем, который выполняет запрос.
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

    //Поиск всех мероприятий по фильтру, только у ментора видимо ошибка, так как аннотация стоит именно POST
    @PostMapping("/search")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<List<EventDto>> getSearchEvent(
            @Valid EventSearchRequestDto eventSearchRequestDto) // только здесь нужно подумать по поводу аннотаций, пользователь не обязан передавать все поля для поиска
    // поэтому нужно будет явно указать в классе EventSearchRequestDto необходимые аннотации
    {
        log.info("REST request to get Search all events by filter : {}", eventSearchRequestDto);
        var searchEventDomain = eventService.getSearchEvent(eventSearchRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(searchEventDomain.stream()
                        .map(converterDto::toDto)
                        .toList());
    }

// Далее идет логика регистрации пользователя на мероприятие,

/*
//Необходимо учесть, что статус мероприятия должен позволять регистрацию.
//Можно зарегестрироваться только на мероприятие, которое не законочилось и не отменено.
    // далее здесь
    здесь нужно подумать, что мы будем возвращать пользователю при успешной регистрации на мероприятие
        похорошему нужно будет сделать логирование
        пока возвращаем EventDto
 */


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

    //Все мероприятия на которые записан пользователь. Мероприятия должны возвращаться все, даже те, которые отменены или закончены.
    @GetMapping("/registrations/my")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public  ResponseEntity<List<EventDto>> myRegisterEvent () {
        log.info("REST request All events for which the user is registered");
        var myRegisterEvent = eventService.myRegisterEvent();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(myRegisterEvent
                        .stream()
                        .map(converterDto::toDto)
                        .toList());
    }

//Необходимо учесть статус мероприятия. Нельзя отменить регистрацию, если мероприятие уже началось или закончилось.

    @DeleteMapping("/registrations/cancel/{eventId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<Void> deleteRegisterEvent(
            @PathVariable(name = "eventId") Long eventId){
        log.info("REST request to cancelling registration for an event");
        eventService.deleteRegisterEvent(eventId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}

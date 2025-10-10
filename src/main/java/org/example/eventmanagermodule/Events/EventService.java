package org.example.eventmanagermodule.Events;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.example.eventmanagermodule.Events.Converter.EventConverterEntity;
import org.example.eventmanagermodule.Location.LocationRepository;
import org.example.eventmanagermodule.User.User;
import org.example.eventmanagermodule.User.UserService;
import org.example.eventmanagermodule.security.CustomerDetailsService;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final LocalDateTime pointInTime;
    private final ObjectMapper objectMapper;
    private final CustomerDetailsService userDetailsService;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final EventConverterEntity eventConverterEntity;
    private final UserService userService;


    public EventService(@Value("${event.date.min}") String pointInTimeStr, ObjectMapper objectMapper, CustomerDetailsService userDetailsService, LocationRepository locationRepository, EventRepository eventRepository, EventConverterEntity eventConverterEntity, UserService userService) {
        this.pointInTime = LocalDateTime.parse(pointInTimeStr);
        this.objectMapper = objectMapper;
        this.userDetailsService = userDetailsService;
        this.locationRepository = locationRepository;
        this.eventRepository = eventRepository;
        this.eventConverterEntity = eventConverterEntity;
        this.userService = userService;
    }

/*
Мы пока набросали сырой контроллер по управлению запросами, теперь нужно реализовывать сервис, начнем по следующему плану
сперва реализовываем метод в сервисе, потом редактируем метод в контроллере.
Что нам нужно знать по созданию мероприятия
Права: Любой может создать мероприятие, сделаем и в контроллере и в цепочке
Далее по ТЗ:
"Все поля обязательные при создании, также должна быть соблюдена логика (например время не может быть в прошлом, стомость > 0 и т.д.)" -
здесь по стоимости, у нас стоит валидация, то есть если стоимость будет меньше нуля, выйдет ошибка 404 ошибка валидации"
Мой комментарий
по поводу времени: здесь нужно подумать, если мы будет отталкиваться от сегоднишних реалий, то можем прописать условия
что указанное время пользотеля должен указать быть больше .now
а можно, указать где нибудь в классе, даже в классе сервиса переменную(например 01.01.2024)
где это эту переменную будет поттягивать из пропертис файла, то есть логика какая, мы проставим время в проперстис файле
сервис подтянет, эту переменную из пропертис файла, к себе в константу, тем самым у нас будет возможность проверить в будущем как у нас будут меняться проверка статусов мероприятий

далее по ТЗ "Локация должна существовать и ее вместимость должна позволять провести это мероприятие, т.е. кол-во мест должно быть достаточно для всех участников."
еще одно условие для проверки, есть мероприятие и его переменная maxPlaces (максимальное количество мест), она должна быть меньше или равна capacity в Location
но тут у меня сразу появляется вопрос, как у нас по логике локация завязана к мероприятию?
я пониманию, что есть мероприятие, и оно проходит например на такой-то локации, у нас Event и Location вообще нет полей общих? обсудим...


Прилетает к нас в сервис переменная в формате eventUpdateRequestDto в формате Json
то есть чтобы сервис заработал нормально, нужно будет ее перевести в формат Domain, то только указать недостоующие переменные,
переменных которых не хватает, будет вытаскивать с помощью репозитория или других сервисов

Объект нужно будет сохранить в репозиторий, не забываем применять конвектор, он нам пригодится

Вернуть нам нужно именно domain файл, в контроллере будет менять его Dto, так будет правильнее

 */


    public Event createEvent(EventCreateRequestDto eventCreateRequestDto) {

        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id(); // ID текущего пользователя

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
                locationId,// здесь нужно проверить существует ли локации с данном id
                eventCreateRequestDto.getMaxPlaces(), // здесь нужно проверить условие, меньше ли количество чем у самой локации
                0,// так как при создании меропрития, никто его не зарегистрирован на него, пока делаем его пустым
                eventCreateRequestDto.getDate(), // еще одна проверка
                eventCreateRequestDto.getCost(), // здесь проверка не нужна, делаем на этапе валидации
                eventCreateRequestDto.getDuration(),
                EventStatus.WAIT_START
        );
        var savedEntity = eventRepository.save(eventConverterEntity.toEntity(domainEvent));

        var savedDomain = eventConverterEntity.toDomain(savedEntity);

        return savedDomain;
    }

/*
Переходим к написанию удалению мероприятия.
Сперва рассуждаем, сперва пользователь передает, нам id , самого меропрития,
проверяем есть ли такое меропритиятие, если то бросаем исключение
Далее Админ, может удалить любое мероприятие
Пользователь может удалить, только мероприятие которые сам создал, поэтому
нам нужно выдернуть из аутентификации пользователя и  его id
если id пользователя при аутентификации в самом запросе равен Id пользователя мероприятие, то идем дальше по коду, иначе исключение

далее находим по id мероприятие, хотя мы уже ранее нашли, то потом просто его удаляем из репозитория
ничего не возращаем, метод void

 */


    public void deleteEvent(Long eventId) {

        var searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id(); // ID текущего пользователя
        String userRole = currentUser.role().name();

        var eventOwnerId = searchEvent.getOwner().getId();

        if (!userRole.equals("ADMIN") && !Objects.equals(ownerId, eventOwnerId)) {
            throw new AccessDeniedException("Недостаточно прав для удаления мероприятия");
        }
        eventRepository.delete(searchEvent);

    }
/*
Давай по логике, это метод по выводу необходимого мероприятия по id
проверка условий, сперва проверяем есть ли такое мероприятие в БД,


Далее просто выдаем найденное мероприятие, если все ок возвращаем именно объект Domain
в контроллере будем его переводить в EventDto
 */


    public Event getEvent(Long eventId) {
        var searchEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));

        return eventConverterEntity.toDomain(searchEvent);
    }


    /*
    ТЗ: Доступно только ADMIN, либо создателю мероприятия. Т.е. роль из JWT токена должна быть ADMIN, либо userId должен быть равен создателю мероприятия.
    Учтите то, как можно менять мероприятие.
    Валидируйте входные значения, например maxPlaces должно быть больше, чем уже записанных пользователей,

    стоимость > 0, длительность > 0 и т.д

    Фишка, данного метода, что мы получаем два аргумента в запросе, это id
     и тело запроса в формате eventUpdateRequestDto
     Сперва, как обычно, проверяем есть ли такое мероприятие в БД
     Далее, нам нужно перевести файл который нам передали в формат сервиса
     валидность полей мы проверяем на этапе запроса пользователя
     duration у нас минимум 30, поэтому валидность проверена, на этапе запроса
     Далее, нужно написать условие, чтобы occupiedPlaces это записанные пользователи на мероприятии
     если maxPlaces запроса  < occupiedPlaces БД, то выбрасываем исключение
стоимость у нас на этапе запроса проверяется
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal cost;

     */

    public Event updateEvent(Long eventId,
                             EventUpdateRequestDto eventUpdateRequestDto) {

        var searchEvent = eventRepository.findById(eventId) // проверка мероприятия в БД
                .orElseThrow(() -> new EntityNotFoundException("Event with id " + eventId + " not found"));

        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id(); // ID текущего пользователя
        String userRole = currentUser.role().name();// роль текущего пользователя

        var eventOwnerId = searchEvent.getOwner().getId();

        if (!userRole.equals("ADMIN") && !Objects.equals(ownerId, eventOwnerId)) { // проверка прав
            throw new AccessDeniedException("Недостаточно прав для удаления мероприятия");
        }

        if (!eventUpdateRequestDto.getDate().isAfter(pointInTime)) { // проверка даты мероприятия
            throw new IllegalArgumentException(
                    String.format("Дата мероприятия должна быть позже %s", pointInTime.toLocalDate())
            );
        }
        if (eventUpdateRequestDto.getMaxPlaces() < searchEvent.getOccupiedPlaces()) {// проверка количества, не может быть меньше зарегистрированных пользователй
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

    /*
    Метод: Все мероприятия созданные пользователем, который выполняет запрос.
    Мысли по реализации данного метода, здесь нужно вывести все мероприятия которые создал пользователь который выполняет этот запрос
    если пользователь не создал мероприятия то и выводить нечего, просто выводим пустой экран
    Что нам нужно сделать, сперва получаем пользователя по аутентификации, если он есть, то идем дальше
    далее получаем самого пользователя, а после выводим список его мероприятий
    здесь я думаю, можно сделать через запрос Query, где мы просто выводим результаты EventEntity где user_id, будет соответствовать Id пользователя
    но только я незнаю как будет лучше, сделать это через JPQL запрос или через методы которые предоставляет нам спринг

     */

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


    private User getCurrentUser() { // в этом методе по аутентификации мы возвращаем ЛОГИН
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("Пользователь не аутентифицирован или неправильный тип principal");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userService.findByLogin(userDetails.getUsername());
    }


    //Поиск всех мероприятий по фильтру, только у ментора видимо ошибка, так как аннотация стоит именно POST

    /*
    Вот суть этого метода, о том же мы можем осуществлять поиск мероприятия по нескольким параметрам, какие как:
    стоимость cost min, cost max
    количество мест places min, places max
    дата мероприятия dateStartAfter, dateStartBefore
    длительность duration min, duration max
    поиск по id локации locationId
    поиск по статусу
    поиск по названию

    Делаем мы это в любом случае через Query запросы
    Но самое главное нужно учесть, что пользователь не обязан передавать все поля для поиска
    он может передать только одно поле, а может и все сразу, поэтому я думаю нужно поставить валидацию на уровевне DTO
    что пользователь может передавать не все поля, а только те которые он хочет
    Если пользователь не передал ни одного поля, то мы просто возвращаем все мероприятия которые есть
    поэтому в класс EventSearchRequestDto нужно добавить аннотацию,@JsonInclude(JsonInclude.Include.NON_NULL)
     */
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
}

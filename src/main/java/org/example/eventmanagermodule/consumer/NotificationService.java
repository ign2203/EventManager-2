package org.example.eventmanagermodule.consumer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.eventmanagermodule.Events.EventStatus;
import org.example.eventmanagermodule.Events.Registration.EventRegistrationService;
import org.example.eventmanagermodule.User.User;
import org.example.eventmanagermodule.User.UserEntity;
import org.example.eventmanagermodule.User.UserService;
import org.example.eventmanagermodule.producer.EventChangeNotification;
import org.example.eventmanagermodule.producer.status.EventStatusChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final EventRegistrationService registrationService;
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public NotificationService(EventRegistrationService registrationService, NotificationRepository notificationRepository, UserService userService, ObjectMapper objectMapper) {
        this.registrationService = registrationService;
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }
    public void handleNotification(EventChangeNotification eventChangeNotification) throws JsonProcessingException {
        Long eventId = eventChangeNotification.getEventId();
        List<UserEntity> usersRegisterEvent = registrationService.getAllUsersRegisterEvent(eventId);
        for (UserEntity user : usersRegisterEvent) {
            NotificationEntity notification = new NotificationEntity();
            notification.setUser(user);
            notification.setMessage("Событие " + eventId + " изменилось");
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);
            notification.setEventId(eventId);
            String json = objectMapper.writeValueAsString(eventChangeNotification);
            notification.setPayload(json);
            notificationRepository.save(notification);
        }
    }
    @Transactional
    public List<EventChangeNotification> getUnreadUserNotifications() {
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();
        List<NotificationEntity> notificationEntities = notificationRepository.findAllByUserIdAndReadFalse(ownerId);

        List<String> notificationString = notificationEntities
                .stream()
                .map(NotificationEntity::getPayload)
                .toList();
        var notificationDto = notificationString
                .stream()
                .map(s -> {
                    try {
                        return objectMapper.readValue(s, EventChangeNotification.class);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("Не удалось десериализовать уведомление", e);
                    }
                })
                .toList();
        log.info("NotificationService успешно достает всю полезную нагрузку из notificationRepository");
        notificationEntities.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notificationEntities);
        log.info("NotificationService успешно изменил статус уведомления на true");
        return notificationDto;
    }
    @Transactional
    public void markAsRead(List<Long> notificationIds) {
        User currentUser = getCurrentUser();
        Long ownerId = currentUser.id();
        List<NotificationEntity> found = notificationRepository.findAllByIdInAndUserId(notificationIds, ownerId);
        if (found.isEmpty()) {
            return;
        }
        found.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(found);
    }
    public void processEventStatusChange(EventStatusChangeNotification notification) {
        Long eventId = notification.eventId();
        EventStatus newStatus = notification.newStatus();
        EventStatus oldStatus = notification.oldStatus();
        if (newStatus == EventStatus.CLOSED) {
            log.warn("Event [{}] was closed — user notifications may be required.", eventId);
        } else {
            log.info("Event [{}] changed status from {} to {}.", eventId, oldStatus, newStatus);
        }
    }
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Пользователь не аутентифицирован");
        }
        String username = authentication.getName(); // loginFromToken
        return userService.findByLogin(username);
    }

}

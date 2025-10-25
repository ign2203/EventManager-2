package org.example.eventmanagermodule.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.eventmanagermodule.Events.Registration.EventRegistrationService;
import org.example.eventmanagermodule.User.SecurityUtils;
import org.example.eventmanagermodule.User.User;
import org.example.eventmanagermodule.User.UserEntity;
import org.example.eventmanagermodule.producer.EventChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final EventRegistrationService registrationService;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final SecurityUtils securityUtils;

    public NotificationService(EventRegistrationService registrationService, NotificationRepository notificationRepository, ObjectMapper objectMapper, SecurityUtils securityUtils) {
        this.registrationService = registrationService;
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
        this.securityUtils = securityUtils;
    }

    public void handleNotification(EventChangeNotification eventChangeNotification) throws JsonProcessingException {
        Long eventId = eventChangeNotification.getEventId();
        List<UserEntity> usersRegisterEvent = registrationService.getAllUsersRegisterEvent(eventId);
        for (UserEntity user : usersRegisterEvent) {
            NotificationEntity notification = new NotificationEntity();
            notification.setUser(user);
            notification.setMessage("Event " + eventId + " changed");
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
        User currentUser = securityUtils.getCurrentUser();
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
                        log.error("Failed to deserialize notification payload: {}", s, e);
                        throw new IllegalStateException("Не удалось десериализовать уведомление", e);// нужно подумать насчет логгера
                    }
                })
                .toList();
        log.info("Fetched {} unread notifications for userId={}", notificationEntities.size(), ownerId);
        notificationEntities.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notificationEntities);
        log.info("Marked {} notifications as read for userId={}", notificationEntities.size(), ownerId);
        return notificationDto;
    }

    @Transactional
    public void markAsRead(List<Long> notificationIds) {
        User currentUser = securityUtils.getCurrentUser();
        Long ownerId = currentUser.id();
        List<NotificationEntity> found = notificationRepository.findAllByIdInAndUserId(notificationIds, ownerId);
        if (found.isEmpty()) {
            return;
        }
        found.forEach(n -> log.info("Found notification id={} read={}", n.getId(), n.isRead()));
        found.forEach(notification -> notification.setRead(true));
        log.info("Marked {} notifications as read for userId={}", found.size(), ownerId);
        notificationRepository.saveAll(found);
    }
    @Scheduled(cron = "${scheduler.notifications.cleanup-cron}")
    @Transactional
    public void deleteOldNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        int beforeCount = notificationRepository.findAll().size();
        notificationRepository.deleteAllOlderThan(threshold);
        log.info("Deleted notifications older than 7 days (before={}, after={})", beforeCount, notificationRepository.findAll().size());
    }
}

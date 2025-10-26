package dev.sorokin.eventnotificator.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sorokin.eventnotificator.consumer.fieldChange.EventChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public NotificationService(
            NotificationRepository notificationRepository,
            ObjectMapper objectMapper, WebClient webClient
    ) {
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
        this.webClient = webClient;
    }

    public void handleNotification(EventChangeNotification eventChangeNotification) throws JsonProcessingException {
        Long eventId = eventChangeNotification.getEventId();
        List<Long> userIds = webClient.get()
                .uri("http://localhost:8080/events/internal/{eventId}/users", eventId)
                .header("X-INTERNAL-KEY", "super-secret-key")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Long>>() {
                })
                .block();
        if (userIds == null || userIds.isEmpty()) {
            log.warn("No registered users found for eventId={}", eventId);
            return;
        }
        log.info("Received event change notification for eventId={} with {} registered users",
                eventId, userIds.size());
        for (Long userId : userIds) {
            NotificationEntity notification = new NotificationEntity();
            notification.setUserId(userId);
            notification.setEventId(eventId);
            notification.setMessage("Event " + eventId + " changed");
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);
            String json = objectMapper.writeValueAsString(eventChangeNotification);
            notification.setPayload(json);
            notificationRepository.save(notification);
        }
        log.info("Saved {} notifications for eventId={}", userIds.size(), eventId);
    }

    @Transactional
    public List<EventChangeNotification> getUnreadUserNotifications() {
        Long userId = getCurrentUserId();
        log.info("Fetching unread notifications for userId={}", userId);
        List<NotificationEntity> notificationEntities = notificationRepository.findAllByUserIdAndReadFalse(userId);
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
                        log.error("Failed to deserialize notification payload for userId={}, payload={}", userId, s, e);
                        throw new IllegalStateException("Failed to deserialize notification", e);
                    }
                })
                .toList();
        log.info("Fetched {} unread notifications for userId={}", notificationEntities.size(), userId);
        notificationEntities.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notificationEntities);
        log.info("Marked {} notifications as read for userId={}", notificationEntities.size(), userId);
        return notificationDto;
    }

    @Transactional
    public void markAsRead(List<Long> notificationIds) {
        Long userId = getCurrentUserId();
        List<NotificationEntity> found = notificationRepository.findAllByIdInAndUserId(notificationIds, userId);
        if (found.isEmpty()) {
            return;
        }
        found.forEach(n -> log.info("Found notification id={} read={}", n.getId(), n.isRead()));
        found.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(found);
        log.info("Marked {} notifications as read for userId={}", found.size(), userId);
    }

    @Scheduled(cron = "${scheduler.notifications.cleanup-cron}")
    @Transactional
    public void deleteOldNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        int beforeCount = notificationRepository.findAll().size();
        notificationRepository.deleteAllOlderThan(threshold);
        log.info("Deleted notifications older than 7 days (before={}, after={})", beforeCount, notificationRepository.findAll().size());
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        Object details = authentication.getDetails();
        if (details instanceof Long userId) {
            return userId;
        }
        throw new IllegalStateException("User ID not found in authentication token");
    }
}
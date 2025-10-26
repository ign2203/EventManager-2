package dev.sorokin.eventnotificator.consumer;

import dev.sorokin.eventnotificator.consumer.fieldChange.EventChangeNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<List<EventChangeNotification>> getUnreadUserNotifications() {
        log.info("REST request to get unread user notifications");
        List<EventChangeNotification> notification = notificationService.getUnreadUserNotifications();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notification);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    ResponseEntity<?> markAsRead(@RequestBody NotificationReadRequest request) {
        log.info("REST request to mark as read user notification");
        notificationService.markAsRead(request.getNotificationIds());
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}



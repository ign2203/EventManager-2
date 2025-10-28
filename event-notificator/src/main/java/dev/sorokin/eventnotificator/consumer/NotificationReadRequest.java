package dev.sorokin.eventnotificator.consumer;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class NotificationReadRequest {
    private List<Long> notificationIds;
}
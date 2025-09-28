package ru.danilgordienko.notification_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import ru.danilgordienko.notification_service.model.dto.response.NotificationResponseDto;
import ru.danilgordienko.notification_service.model.entity.Notification;
import ru.danilgordienko.notification_service.service.NotificationSender;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationSender implements NotificationSender {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(Long userId, NotificationResponseDto notification) {
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                notification
        );
        log.info("Notification sent to user {}", userId);
    }
}

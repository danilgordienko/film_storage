package ru.danilgordienko.notification_service.service;

import ru.danilgordienko.notification_service.model.dto.response.NotificationResponseDto;

public interface NotificationSender {

    void sendNotification(Long userId, NotificationResponseDto notification);
}

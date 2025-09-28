package ru.danilgordienko.notification_service.service;

import ru.danilgordienko.notification_service.model.dto.request.NotificationRequestDto;

public interface NotificationService {

    void acceptNotification(NotificationRequestDto notification);
}

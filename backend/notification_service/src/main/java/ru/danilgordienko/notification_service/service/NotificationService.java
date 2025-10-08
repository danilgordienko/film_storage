package ru.danilgordienko.notification_service.service;

import ru.danilgordienko.notification_service.model.dto.request.NotificationRequestDto;
import ru.danilgordienko.notification_service.model.dto.response.NotificationListResponseDto;
import ru.danilgordienko.notification_service.model.dto.response.NotificationsInfoDto;

public interface NotificationService {

    void acceptNotification(NotificationRequestDto notification);
    NotificationListResponseDto getAllNotifications(Long id);
    NotificationsInfoDto getNotificationsInfo(Long id);
}

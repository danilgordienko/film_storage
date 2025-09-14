package ru.danilgordienko.notification_service.service;

import ru.danilgordienko.notification_service.model.entity.Notification;

public interface NotificationSender {

    void sendNotification(Long userId, Notification notification);
}

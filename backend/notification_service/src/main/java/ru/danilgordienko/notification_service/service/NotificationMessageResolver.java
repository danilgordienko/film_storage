package ru.danilgordienko.notification_service.service;

import ru.danilgordienko.notification_service.model.entity.Notification;

public interface NotificationMessageResolver {

    String getMessage(Notification notification);

}

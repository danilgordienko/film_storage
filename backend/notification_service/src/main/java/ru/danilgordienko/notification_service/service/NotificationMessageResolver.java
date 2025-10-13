package ru.danilgordienko.notification_service.service;

import ru.danilgordienko.notification_service.model.entity.Notification;
import ru.danilgordienko.notification_service.model.enums.Type;

public interface NotificationMessageResolver {

    String getMessage(String type, String sender);

}

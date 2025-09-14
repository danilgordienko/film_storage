package ru.danilgordienko.notification_service.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.danilgordienko.notification_service.model.entity.Notification;

public class NotificationReceivedEvent extends ApplicationEvent {

    @Getter
    private final Notification notification;

    public NotificationReceivedEvent(Object source, Notification notification) {
        super(source);
        this.notification = notification;
    }
}

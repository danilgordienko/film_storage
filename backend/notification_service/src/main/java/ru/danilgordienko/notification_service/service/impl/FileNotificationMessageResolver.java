package ru.danilgordienko.notification_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.danilgordienko.notification_service.model.entity.Notification;
import ru.danilgordienko.notification_service.service.NotificationMessageResolver;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileNotificationMessageResolver implements NotificationMessageResolver {

    private final MessageSource messageSource;

    @Override
    public String getMessage(Notification notification) {
        return messageSource.getMessage(
                "notification." + notification.getType().getValue(),
                new Object[]{notification.getSender()},
                Locale.forLanguageTag("ru")
        );
    }
}

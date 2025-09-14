package ru.danilgordienko.notification_service.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.danilgordienko.notification_service.config.RabbitConfig;
import ru.danilgordienko.notification_service.model.entity.Notification;
import ru.danilgordienko.notification_service.model.events.NotificationReceivedEvent;
import ru.danilgordienko.notification_service.repository.NotificationRepository;
import ru.danilgordienko.notification_service.service.NotificationSender;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher  applicationEventPublisher;
    private final NotificationSender notificationSender;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    @Transactional
    public void acceptNotification(Notification notification) {
        notificationRepository.save(notification);
        applicationEventPublisher.publishEvent(
                new NotificationReceivedEvent(this, notification)
        );
    }


    @EventListener
    @Async
    public void sendNotification(NotificationReceivedEvent event) {
        notificationSender.sendNotification(event.getNotification().getUserId(), event.getNotification());
    }

}

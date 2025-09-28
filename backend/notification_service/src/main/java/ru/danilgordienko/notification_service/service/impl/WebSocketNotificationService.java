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
import ru.danilgordienko.notification_service.model.dto.request.NotificationRequestDto;
import ru.danilgordienko.notification_service.model.dto.response.NotificationResponseDto;
import ru.danilgordienko.notification_service.model.entity.Notification;
import ru.danilgordienko.notification_service.model.events.NotificationReceivedEvent;
import ru.danilgordienko.notification_service.model.mapping.NotificationMapping;
import ru.danilgordienko.notification_service.repository.NotificationRepository;
import ru.danilgordienko.notification_service.service.NotificationMessageResolver;
import ru.danilgordienko.notification_service.service.NotificationSender;
import ru.danilgordienko.notification_service.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher  applicationEventPublisher;
    private final NotificationSender notificationSender;
    private final NotificationMessageResolver notificationMessageResolver;
    private final NotificationMapping notificationMapping;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    @Transactional
    public void acceptNotification(NotificationRequestDto notification) {
        Notification notificationEntity = notificationMapping.toNotification(notification);
        //notificationRepository.save(notificationEntity);
        applicationEventPublisher.publishEvent(
                new NotificationReceivedEvent(this, notificationEntity)
        );
        log.info("Received notification request: {}", notification);
    }


    @EventListener
    @Async
    public void sendNotification(NotificationReceivedEvent event) {
        String message = notificationMessageResolver.getMessage(event.getNotification());
        log.info(message);
        NotificationResponseDto response = notificationMapping.toNotificationResponseDto(
                event.getNotification(), message);
        notificationSender.sendNotification(event.getNotification().getUserId(), response);
    }

}

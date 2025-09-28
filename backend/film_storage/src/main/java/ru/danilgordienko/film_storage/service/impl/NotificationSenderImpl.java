package ru.danilgordienko.film_storage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.danilgordienko.film_storage.config.RabbitConfig;
import ru.danilgordienko.film_storage.model.dto.NotificationDto;
import ru.danilgordienko.film_storage.service.BrokerClient;
import ru.danilgordienko.film_storage.service.NotificationSender;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSenderImpl implements NotificationSender {

    private final BrokerClient  brokerClient;

    @Override
    @Async
    public void send(NotificationDto notificationDto) {
        brokerClient.send(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_NOTIFICATION, notificationDto);
    }
}

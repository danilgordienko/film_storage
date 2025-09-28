package ru.danilgordienko.film_storage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.danilgordienko.film_storage.service.BrokerClient;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitBrokerClient implements BrokerClient {

    private final RabbitTemplate rabbitTemplate;

    public Optional<Object> getResponse(String exchange, String routingKey, Object body) {
        log.debug("Request to RabbitMQ with body: {}", body.toString());
        try {
            return Optional.ofNullable(rabbitTemplate.convertSendAndReceive(
                    exchange,
                    routingKey,
                    body
            ));
        } catch (AmqpException e) {
            log.error("Error while working with RabbitMQ: {}", e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public void send(String exchange, String routingKey, Object body) {
        log.debug("Sending to RabbitMQ with body: {}", body.getClass());
        try {
            rabbitTemplate.convertAndSend(
                    exchange,
                    routingKey,
                    body
            );
        } catch (AmqpException e) {
            log.error("Error while working with RabbitMQ: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
        }
    }


}

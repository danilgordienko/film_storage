package ru.danilgordienko.film_fetcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_fetcher.config.RabbitConfig;
import ru.danilgordienko.film_fetcher.model.dto.response.TmdbMovieResponse;
import ru.danilgordienko.film_fetcher.service.BrokerClient;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitBrokerClient implements BrokerClient {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public boolean sendMovies(List<TmdbMovieResponse> movies) {
        log.debug("Sending movies to rabbit");
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY,
                    movies
            );
            return true;
        } catch (AmqpException e) {
            log.error("Error while sending movies to rabbit", e);
            return false;
        }

    }
}

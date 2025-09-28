package ru.danilgordienko.film_storage.service;

import java.util.Optional;

public interface BrokerClient {

    Optional<Object> getResponse(String exchange, String routingKey, Object body);
    void send(String exchange, String routingKey, Object body);

}

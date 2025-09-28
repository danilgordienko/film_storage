package ru.danilgordienko.film_storage.service;

import ru.danilgordienko.film_storage.model.dto.NotificationDto;

public interface NotificationSender {

    void send(NotificationDto notificationDto);
}

package ru.danilgordienko.notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.danilgordienko.notification_service.model.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}

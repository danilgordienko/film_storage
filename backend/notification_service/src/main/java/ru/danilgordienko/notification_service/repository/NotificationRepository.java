package ru.danilgordienko.notification_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.danilgordienko.notification_service.model.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserId(Long id);

    List<Notification> findAllByUserIdAndIsRead(Long id, boolean isRead);
}

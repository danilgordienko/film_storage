package ru.danilgordienko.notification_service.model.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.danilgordienko.notification_service.model.dto.request.NotificationRequestDto;
import ru.danilgordienko.notification_service.model.dto.response.NotificationResponseDto;
import ru.danilgordienko.notification_service.model.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapping {

    @Mapping(target = "isRead", expression = "java(false)")
    Notification toNotification(NotificationRequestDto notificationRequestDto);

    @Mapping(target = "message", expression = "java(message)")
    NotificationResponseDto  toNotificationResponseDto(Notification notification, String message);
}

package ru.danilgordienko.notification_service.model.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.danilgordienko.notification_service.model.dto.request.NotificationRequestDto;
import ru.danilgordienko.notification_service.model.dto.response.NotificationListResponseDto;
import ru.danilgordienko.notification_service.model.dto.response.NotificationResponseDto;
import ru.danilgordienko.notification_service.model.dto.response.NotificationsInfoDto;
import ru.danilgordienko.notification_service.model.entity.Notification;
import ru.danilgordienko.notification_service.service.NotificationMessageResolver;
import ru.danilgordienko.notification_service.service.impl.FileNotificationMessageResolver;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapping {

    @Mapping(target = "isRead", expression = "java(false)")
    Notification toNotification(NotificationRequestDto notificationRequestDto);

    @Mapping(target = "message", expression = "java(message)")
    NotificationResponseDto  toNotificationResponseDto(Notification notification, String message);

    List<NotificationResponseDto> toNotificationResponseDtoList(List<Notification> notifications);

    default NotificationListResponseDto toNotificationListResponseDto(List<Notification> notifications){
        return new NotificationListResponseDto(
                toNotificationResponseDtoList(notifications), notifications.size());
    }

    default NotificationsInfoDto toNotificationsInfoDto(List<Notification> notifications){
        return new NotificationsInfoDto(notifications.size());
    }

}

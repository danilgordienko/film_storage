package ru.danilgordienko.notification_service.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationListResponseDto {
    private List<NotificationResponseDto> notifications;
    private int count;
}

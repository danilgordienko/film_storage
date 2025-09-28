package ru.danilgordienko.notification_service.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.danilgordienko.notification_service.model.enums.Type;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequestDto {
    private String sender;
    private Long userId;
    private Type type;
}

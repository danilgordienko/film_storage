package ru.danilgordienko.notification_service.model.dto.response;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.danilgordienko.notification_service.model.enums.Type;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private Long id;
    private Boolean isRead;
    private String message;
    private String sender;
    private String type;
    private Instant createdAt;
}

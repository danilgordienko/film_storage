package ru.danilgordienko.film_storage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.danilgordienko.film_storage.model.enums.Type;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private String sender;
    private Long userId;
    private Type type;
}

package ru.danilgordienko.notification_service.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import ru.danilgordienko.notification_service.model.enums.Type;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private Long  userId;

    @Column(name = "is_read",  nullable = false)
    private Boolean isRead;

//    @Column(columnDefinition = "jsonb")
//    @ColumnTransformer(write = "?::jsonb")
//    private String payload;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

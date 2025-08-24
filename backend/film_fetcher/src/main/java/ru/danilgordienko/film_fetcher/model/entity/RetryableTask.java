package ru.danilgordienko.film_fetcher.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import ru.danilgordienko.film_fetcher.model.enums.RetryableTaskStatus;
import ru.danilgordienko.film_fetcher.model.enums.RetryableTaskType;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetryableTask {

    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    private RetryableTaskType type;

    @Enumerated(EnumType.STRING)
    private RetryableTaskStatus status;

    private Instant retryTime;
}

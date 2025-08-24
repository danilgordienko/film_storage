package ru.danilgordienko.film_fetcher.repository;


import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.danilgordienko.film_fetcher.model.entity.RetryableTask;
import ru.danilgordienko.film_fetcher.model.enums.RetryableTaskStatus;
import ru.danilgordienko.film_fetcher.model.enums.RetryableTaskType;

import java.time.Instant;
import java.util.List;

public interface RetryableTaskRepository extends CrudRepository<RetryableTask,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r from RetryableTask r where r.type= :type " +
            "AND r.retryTime<= :retryTime " +
            "AND r.status= :status " +
            "order by r.retryTime asc")
    List<RetryableTask> findRetryableTaskForProcessing(RetryableTaskType type, Instant retryTime,
                                                       RetryableTaskStatus status, Pageable pageable);

}

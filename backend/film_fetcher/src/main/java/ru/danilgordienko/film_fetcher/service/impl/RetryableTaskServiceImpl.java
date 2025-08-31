package ru.danilgordienko.film_fetcher.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_fetcher.model.dto.response.MovieDto;
import ru.danilgordienko.film_fetcher.model.entity.RetryableTask;
import ru.danilgordienko.film_fetcher.model.enums.RetryableTaskStatus;
import ru.danilgordienko.film_fetcher.model.enums.RetryableTaskType;
import ru.danilgordienko.film_fetcher.model.mapping.RetryableTaskMapping;
import ru.danilgordienko.film_fetcher.repository.RetryableTaskRepository;
import ru.danilgordienko.film_fetcher.service.RetryableTaskService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryableTaskServiceImpl implements RetryableTaskService {

    private final RetryableTaskRepository retryableTaskRepository;
    private final RetryableTaskMapping  retryableTaskMapping;
    @Value("${retryabletask.timeout}")
    private int timeout;
    @Value("${retryabletask.batchsize}")
    private int batchSize;

    @Override
    @Transactional
    public RetryableTask createRetryableTask(List<MovieDto> movies, RetryableTaskType type) {
        RetryableTask retryableTask = retryableTaskMapping.toSendMovieRetryableTask(movies, type);
        retryableTask.setStatus(RetryableTaskStatus.IN_PROGRESS);
        retryableTask.setRetryTime(Instant.now());
        return retryableTaskRepository.save(retryableTask);
    }

    @Override
    @Transactional
    public List<RetryableTask> getRetryableTasks(RetryableTaskType type) {
        var currentTime = Instant.now();
        Pageable pageable = PageRequest.of(0,batchSize);
        List<RetryableTask> retryableTasks = retryableTaskRepository.findRetryableTaskForProcessing(
                type, currentTime, RetryableTaskStatus.IN_PROGRESS, pageable);

        for (RetryableTask retryableTask : retryableTasks) {
            retryableTask.setRetryTime(currentTime.plus(Duration.ofSeconds(timeout)));
        }
        return retryableTasks;
    }

    @Transactional
    @Override
    public void markRetryableTasksAsCompleted(List<RetryableTask> retryableTasks) {
        for (RetryableTask retryableTask : retryableTasks) {
            retryableTask.setStatus(RetryableTaskStatus.SUCCESS);
        }
        retryableTaskRepository.saveAll(retryableTasks);
    }

}

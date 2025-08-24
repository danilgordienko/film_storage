package ru.danilgordienko.film_fetcher.service;

import ru.danilgordienko.film_fetcher.model.dto.request.TmdbMovie;
import ru.danilgordienko.film_fetcher.model.entity.RetryableTask;
import ru.danilgordienko.film_fetcher.model.enums.RetryableTaskType;

import java.util.List;

public interface RetryableTaskService {

    RetryableTask createRetryableTask(List<TmdbMovie> movies, RetryableTaskType type);
    List<RetryableTask> getRetryableTasks(RetryableTaskType type);
    void markRetryableTasksAsCompleted(List<RetryableTask> retryableTasks);
}

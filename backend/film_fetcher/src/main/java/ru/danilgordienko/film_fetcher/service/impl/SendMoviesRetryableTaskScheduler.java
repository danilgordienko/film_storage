package ru.danilgordienko.film_fetcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.danilgordienko.film_fetcher.model.entity.RetryableTask;
import ru.danilgordienko.film_fetcher.model.enums.RetryableTaskType;
import ru.danilgordienko.film_fetcher.model.mapping.RetryableTaskMapping;
import ru.danilgordienko.film_fetcher.service.BrokerClient;
import ru.danilgordienko.film_fetcher.service.RetryableTaskScheduler;
import ru.danilgordienko.film_fetcher.service.RetryableTaskService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendMoviesRetryableTaskScheduler implements RetryableTaskScheduler {

    private final RetryableTaskService  retryableTaskService;
    private final RetryableTaskMapping  retryableTaskMapping;
    private final BrokerClient brokerClient;

    @Scheduled(fixedRate = 5000)
    @Override
    public void executeRetryableTask() {
        log.debug("Обработчик задач запущен");
        try {
            var retryableTasks = retryableTaskService.getRetryableTasks(RetryableTaskType.SEND_MOVIE_REQUEST);
            if (retryableTasks.isEmpty()) {
                log.debug("Ни одной задачи не найдено");
                return;
            }
            List<RetryableTask> successRetryableTasks = new ArrayList<>();
            for (var retryableTask : retryableTasks) {
                var result = brokerClient.sendMovies(retryableTaskMapping.jsonToTmdbMovies(retryableTask));
                if (result) {
                    successRetryableTasks.add(retryableTask);
                    log.debug("Задача с id {} успешно обработана", retryableTask.getId());
                } else
                    log.warn("Не удалось обработать задачу с id {}", retryableTask.getId());
            }
            retryableTaskService.markRetryableTasksAsCompleted(successRetryableTasks);
            log.debug("Обработка задач завершена, обработано задач: {}", successRetryableTasks.size());
        } catch (Exception e) {
            log.error("Ошибка при обработке задач " + e.getMessage(), e);
        }
    }
}

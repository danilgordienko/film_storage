package ru.danilgordienko.film_storage.MovieAPI;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieDto;
import ru.danilgordienko.film_storage.config.RabbitConfig;
import ru.danilgordienko.film_storage.model.Genre;
import ru.danilgordienko.film_storage.model.Movie;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieApiClient {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final int size = 20;

    private Optional<Object> getRabbitResponse(String exchange, String routingKey, Object body) {
        log.info("Запрос к RabbitMQ с телом: {}", body.toString());
        try {
            return Optional.ofNullable(rabbitTemplate.convertSendAndReceive(
                    exchange,
                    routingKey,
                    body
            ));
        } catch (AmqpException e) {
            log.error("Ошибка при работе с RabbitMQ: {}", e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public byte[] getPoster(Movie movie) {
        log.info("Получение постера к фильму с id: {}", movie.getId());
        var response = getRabbitResponse(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY_POSTER,
                movie.getPoster()
        );
        return response.map(r -> {
            log.info("");
            return (byte[]) r;
        }).orElse(new byte[0]);
    }

    public List<MovieDto> getPopularMoviesPage(int page){
        log.info("Получение страницы {} фильмов", page);
        var response = getRabbitResponse(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY_PAGE,
                page
        );

        if (response.isEmpty()) {
            log.warn("Ответ от сервиса фильмов не получен");
            return List.of();
        }

        List<MovieDto> movies;
        try {
            movies = (List<MovieDto>) response.get();
        } catch (ClassCastException e) {
            log.error("Ошибка приведения типов: {}", e.getMessage());
            return List.of();
        }
        return movies;
    }

    @RabbitListener(queues = "movies.queue")
    public void getRecentMovies(List<MovieDto> movies) {
        log.info("Получение недавно вышедших фильмов");
        eventPublisher.publishEvent(new MoviesReceivedEvent(this, movies));
    }

    public class MoviesReceivedEvent extends ApplicationEvent {
        private final List<MovieDto> movies;

        public MoviesReceivedEvent(Object source, List<MovieDto> movies) {
            super(source);
            this.movies = movies;
        }

        public List<MovieDto> getMovies() {
            return movies;
        }
    }
}
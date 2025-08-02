package ru.danilgordienko.film_storage.service.impl;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.danilgordienko.film_storage.DTO.RatingDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserRatingDto;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.exception.DatabaseConnectionException;
import ru.danilgordienko.film_storage.exception.ElasticsearchConnectionException;
import ru.danilgordienko.film_storage.exception.RatingAlreadyExistsException;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.model.Rating;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.MovieSearchRepository;
import ru.danilgordienko.film_storage.repository.RatingRepository;
import ru.danilgordienko.film_storage.repository.UserRepository;
import ru.danilgordienko.film_storage.service.MovieService;
import ru.danilgordienko.film_storage.service.RatingService;
import ru.danilgordienko.film_storage.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;
    private final MovieSearchRepository  movieSearchRepository;
    private final UserMapping  userMapping;
    private final UserService userService;
    private final MovieService movieService;


    //добавляем рейтинг к фильму
    @Transactional
    public void addRating(Long id, RatingDto rating, String username)
    {
        try {
            var user = userService.getUserByUsername(username);
            var movie = movieService.getMovieById(id);


            if (ratingRepository.existsByUserAndMovie(user, movie)) {
                log.warn("Отзыв не добавлен: пользователь с именем '{}' уже оставлял отзыв ", username);
                throw new RatingAlreadyExistsException("Рейтинг от пользователя "+ username + " уже был оставлен");
            }

            Rating rate = Rating.builder()
                    .movie(movie)
                    .user(user)
                    .rating(rating.getRating())
                    .comment(rating.getComment())
                    .build();

            ratingRepository.save(rate);
            // Обновляем среднюю оценку
            // В случае если вознкнет ошибка транзакция откатится
            updateAverageRatingInElasticsearch(movie);
        } catch (DataAccessException e) {
            log.error("Ошибка подключения к БД при поиске по ID", e);
            throw new DatabaseConnectionException("Не удалось получить пользователя из БД", e);
        }
    }

    // обноляет среднюю оценку у фильма в Elasticsearch
    private void updateAverageRatingInElasticsearch(Movie movie) {
        try {
            double avgRating = calculateAverageRating(movie);

            movieSearchRepository.findById(movie.getId()).ifPresent(movieDoc -> {
                movieDoc.setAverageRating(avgRating);
                movieSearchRepository.save(movieDoc);
                log.info("Средняя оценка обновлена в Elasticsearch: {}", avgRating);
            });
        } catch (ElasticsearchException | RestClientException e) {
            log.error("Ошибка при работе с Elasticsearch", e);
            throw new ElasticsearchConnectionException("Не удалось найти пользователя в Elasticsearch", e);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при обнолении оценки: {}", e.getMessage(), e);
            throw new RuntimeException("", e);
        }
    }

    // считает среднуюю оценку фильма
    private double calculateAverageRating(Movie movie) {
        List<Rating> ratings = movie.getRatings();
        if (ratings == null || ratings.isEmpty()) return 0.0;

        return ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
    }

    // получение оценка пользователя по username
    public UserRatingDto getUserRatingsByUsername(String username){
        User user = userService.getUserByUsername(username);
        return userMapping.toUserRatingDto(user);
    }

    // получение оценка пользователя по id
    public UserRatingDto getUserRatings(Long id){
        User user = userService.getUserById(id);
        return userMapping.toUserRatingDto(user);
    }
}

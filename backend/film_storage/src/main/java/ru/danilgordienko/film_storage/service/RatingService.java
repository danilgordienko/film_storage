package ru.danilgordienko.film_storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.RatingDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserRatingDto;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.model.Rating;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.MovieSearchRepository;
import ru.danilgordienko.film_storage.repository.RatingRepository;
import ru.danilgordienko.film_storage.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;
    private final MovieSearchRepository  movieSearchRepository;
    private final UserMapping  userMapping;


    //добавляем рейтинг к фильму
    public boolean addRating(Long id, RatingDto rating, String username)
    {
        var user = userRepository.findByUsername(username);
        var movie = movieRepository.findById(id);

        if (user.isEmpty()){
            log.warn("Пользователь с именем '{}' не найден", username);
            return false;
        }
        if (movie.isEmpty()){
            log.warn("Фильм с ID '{}' не найден", id);
            return false;
        }

        if (ratingRepository.existsByUserAndMovie(user.get(), movie.get())){
            log.warn("Отзыв не добавлен: пользователь с именем '{}' уже оставлял отзыв ", username);
            return false;
        }

        Rating rate = Rating.builder()
                .movie(movie.get())
                .user(user.get())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .build();

        ratingRepository.save(rate);
        log.info("Рейтинг успешно добавлен. Пользователь: {}, Фильм: {}, Оценка: {}",
                user.get().getUsername(), movie.get().getTitle(), rate.getRating());

        // Обновляем среднюю оценку
        updateAverageRatingInElasticsearch(movie.get());
        return true;
    }

    // обноляет среднюю оценку у фильма в Elasticsearch
    private void updateAverageRatingInElasticsearch(Movie movie) {
        double avgRating = calculateAverageRating(movie);

        movieSearchRepository.findById(movie.getId()).ifPresent(movieDoc -> {
            movieDoc.setAverageRating(avgRating);
            movieSearchRepository.save(movieDoc);
            log.info("Средняя оценка обновлена в Elasticsearch: {}", avgRating);
        });
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

    public Optional<UserRatingDto> getUserRatingsByUsername(String username){
        log.info("Получение пользователя {} из бд", username);
        return userRepository.findByUsername(username)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserRatingDto(user);
                });
    }

    public Optional<UserRatingDto> getUserRatings(Long id){
        log.info("Получение пользователя из бд с ID = {}", id);
        return userRepository.findById(id)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserRatingDto(user);
                });
    }
}

package ru.danilgordienko.film_storage.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.MappingException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.danilgordienko.film_storage.DTO.RatingDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserRatingDto;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.model.Rating;
import ru.danilgordienko.film_storage.model.User;
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
    private final UserService userService;
    private final MovieService movieService;


    //добавляем рейтинг к фильму
    @Transactional
    public boolean addRating(Long id, RatingDto rating, String username)
    {
        try {
            var user = userService.getUserByUsername(username);
            var movie = movieService.getMovieById(id);


            if (ratingRepository.existsByUserAndMovie(user, movie)) {
                log.warn("Отзыв не добавлен: пользователь с именем '{}' уже оставлял отзыв ", username);
                return false;
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
            return true;
        } catch (DataAccessException e ) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            return false;
        } catch(EntityNotFoundException e) {
            return false;
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
            log.error("Ошибка подключения к Elasticsearch: {}", e.getMessage(), e);
            throw e;
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
    public Optional<UserRatingDto> getUserRatingsByUsername(String username){
        try {
            User user = userService.getUserByUsername(username);
            return Optional.of(userMapping.toUserRatingDto(user));
        } catch (DataAccessException | EntityNotFoundException e ) {
            return Optional.empty();
        } catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при добавлении избранного: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    // получение оценка пользователя по id
    public Optional<UserRatingDto> getUserRatings(Long id){
        try {
            User user = userService.getUserById(id);
            return Optional.of(userMapping.toUserRatingDto(user));
        } catch (DataAccessException | EntityNotFoundException e ) {
            return Optional.empty();
        } catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при добавлении избранного: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}

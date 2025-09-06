package ru.danilgordienko.film_storage.service.impl;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.danilgordienko.film_storage.exception.RatingNotVisibleException;
import ru.danilgordienko.film_storage.model.dto.RatingDto;
import ru.danilgordienko.film_storage.model.dto.UsersDto.UserRatingDto;
import ru.danilgordienko.film_storage.model.dto.mapping.UserMapping;
import ru.danilgordienko.film_storage.exception.DatabaseConnectionException;
import ru.danilgordienko.film_storage.exception.ElasticsearchConnectionException;
import ru.danilgordienko.film_storage.exception.RatingAlreadyExistsException;
import ru.danilgordienko.film_storage.model.entity.Movie;
import ru.danilgordienko.film_storage.model.entity.Rating;
import ru.danilgordienko.film_storage.model.entity.User;
import ru.danilgordienko.film_storage.model.enums.RatingVisibility;
import ru.danilgordienko.film_storage.repository.MovieSearchRepository;
import ru.danilgordienko.film_storage.repository.RatingRepository;
import ru.danilgordienko.film_storage.service.MovieService;
import ru.danilgordienko.film_storage.service.RatingService;
import ru.danilgordienko.film_storage.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final MovieSearchRepository  movieSearchRepository;
    private final UserMapping  userMapping;
    private final UserService userService;
    private final MovieService movieService;


    //добавляем рейтинг к фильму
    @Transactional
    public void addRating(Long id, RatingDto rating, String username) {
        try {
            var user = userService.getUserByEmail(username);
            var movie = movieService.getMovieById(id);

            if (ratingRepository.existsByUserAndMovie(user, movie)) {
                log.warn("Rating not added: user '{}' has already rated this movie", username);
                throw new RatingAlreadyExistsException("Rating by user " + username + " already exists");
            }

            Rating rate = Rating.builder()
                    .movie(movie)
                    .user(user)
                    .rating(rating.getRating())
                    .comment(rating.getComment())
                    .build();

            ratingRepository.save(rate);

            // Update average rating in Elasticsearch
            // Transaction will roll back if an error occurs
            updateAverageRatingInElasticsearch(movie);
            log.debug("Rating added by user '{}' for movie '{}'", username, movie.getTitle());
        } catch (DataAccessException e) {
            log.error("Database access error while fetching by ID", e);
            throw new DatabaseConnectionException("Failed to retrieve user from DB", e);
        }
    }

    // обноляет среднюю оценку у фильма в Elasticsearch
    private void updateAverageRatingInElasticsearch(Movie movie) {
        try {
            double avgRating = calculateAverageRating(movie);

            movieSearchRepository.findById(movie.getId()).ifPresent(movieDoc -> {
                movieDoc.setAverageRating(avgRating);
                movieSearchRepository.save(movieDoc);
                log.debug("Average rating updated in Elasticsearch: {}", avgRating);
            });
        } catch (ElasticsearchException | RestClientException e) {
            log.error("Elasticsearch error while updating average rating", e);
            throw new ElasticsearchConnectionException("Failed to update movie in Elasticsearch", e);
        } catch (Exception e) {
            log.error("Unexpected error while updating rating: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error while updating rating", e);
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
    @Override
    public UserRatingDto getUserRatingsByUsername(String username) {
        User user = userService.getUserByEmail(username);
        log.debug("Retrieving ratings for user '{}'", username);
        return userMapping.toUserRatingDto(user);
    }

    // получение оценка пользователя по id
    @Override
    public UserRatingDto getUserRatings(Long id, String username) {
        User user = userService.getUserById(id);
        User currentUser = userService.getUserByEmail(username);
        checkAccessable(user, currentUser);
        log.debug("Retrieving ratings for user with ID '{}'", id);
        return userMapping.toUserRatingDto(user);
    }

    private void checkAccessable(User user, User currentUser) {
        if (user.getRatingVisibility().equals(RatingVisibility.FRIENDS)){
            if (!user.getFriends().contains(currentUser))
                throw new RatingNotVisibleException("User is not friends");
        }
    }
}

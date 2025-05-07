package ru.danilgordienko.film_storage.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.RatingDto;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.model.Rating;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.RatingRepository;
import ru.danilgordienko.film_storage.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;


    //добавляем рейтинг к фильму
    public void addRating(Long id, RatingDto rating) {
        //забираем username из текущей аунтификации
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Попытка добавить рейтинг. Пользователь: {}, Фильм ID: {}, Рейтинг: {}, Комментарий: {}",
                username, id, rating.getRating(), rating.getComment());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь с именем '{}' не найден", username);
                    return new EntityNotFoundException("Пользователь не найден");
                });

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с ID '{}' не найден", id);
                    return new EntityNotFoundException("Фильм не найден");
                });

        Rating rate = Rating.builder()
                .movie(movie)
                .user(user)
                .rating(rating.getRating())
                .comment(rating.getComment())
                .build();

        ratingRepository.save(rate);
        log.info("Рейтинг успешно добавлен. Пользователь: {}, Фильм: {}, Оценка: {}",
                user.getUsername(), movie.getTitle(), rate.getRating());
    }

}

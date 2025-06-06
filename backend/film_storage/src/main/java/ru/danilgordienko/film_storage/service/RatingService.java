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

        Rating rate = Rating.builder()
                .movie(movie.get())
                .user(user.get())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .build();

        ratingRepository.save(rate);
        log.info("Рейтинг успешно добавлен. Пользователь: {}, Фильм: {}, Оценка: {}",
                user.get().getUsername(), movie.get().getTitle(), rate.getRating());
        return true;
    }

}

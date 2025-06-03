package ru.danilgordienko.film_storage.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.model.Favorite;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.FavoriteRepository;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    //добавление фильма в избранное
    public void addFavorite(Long id) {
        //забираем username из текущей аунтификации.
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Добавление фильма в избранное. Пользователь: {}, Фильм ID: {}", username, id);

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

        Favorite favorite = Favorite
                .builder()
                .movie(movie)
                .user(user)
                .build();

        favoriteRepository.save(favorite);
        log.info("Фильм '{}' добавлен в избранное пользователем '{}'", movie.getTitle(), user.getUsername());
    }

    public void removeFavorite(Long id) {
        //забираем username из текущей аунтификации
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Удаление фильма из избранного. Пользователь: {}, Фильм ID: {}", username, id);

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

        favoriteRepository.deleteByUserAndMovie(user, movie);
        log.info("Фильм '{}' удалён из избранного пользователем '{}'", movie.getTitle(), user.getUsername());
    }

}

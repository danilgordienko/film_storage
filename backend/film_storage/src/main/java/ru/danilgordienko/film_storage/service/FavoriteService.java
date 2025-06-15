package ru.danilgordienko.film_storage.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserFavoritesDto;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.model.Favorite;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.FavoriteRepository;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final UserMapping userMapping;

    //добавление фильма в избранное
    public boolean addFavorite(Long id, String username) {
        log.info("Добавление фильма в избранное. Пользователь: {}, Фильм ID: {}", username, id);

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

        if (favoriteRepository.existsByUserAndMovie(user.get(), movie.get())){
            log.warn("Фильм '{}' не добавлен в избранное пользователем '{}': фильм уже в избранном",
                    movie.get().getTitle(), user.get().getUsername());
            return false;
        }

        Favorite favorite = Favorite
                .builder()
                .movie(movie.get())
                .user(user.get())
                .build();

        favoriteRepository.save(favorite);
        log.info("Фильм '{}' добавлен в избранное пользователем '{}'", movie.get().getTitle(), user.get().getUsername());
        return true;
    }

    public boolean removeFavorite(Long id, String username) {
        log.info("Удаление фильма из избранного. Пользователь: {}, Фильм ID: {}", username, id);

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

        favoriteRepository.deleteByUserAndMovie(user.get(), movie.get());
        log.info("Фильм '{}' удалён из избранного пользователем '{}'", movie.get().getTitle(), user.get().getUsername());
        return true;
    }

    public Optional<UserFavoritesDto> getUserFavoritesByUsername(String username) {
        log.info("Получение пользователя {} из бд", username);
        return userRepository.findByUsername(username)
                .map(user -> {
                    log.info("Пользователь найден: {}", user.getUsername());
                    return userMapping.toUserFavoritesDto(user);
                });
    }
}

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

}

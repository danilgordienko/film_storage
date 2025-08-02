package ru.danilgordienko.film_storage.service.impl;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserFavoritesDto;
import ru.danilgordienko.film_storage.DTO.mapping.UserMapping;
import ru.danilgordienko.film_storage.exception.DatabaseConnectionException;
import ru.danilgordienko.film_storage.exception.FavoriteAlreadyExistsException;
import ru.danilgordienko.film_storage.exception.FavoriteNotFoundException;
import ru.danilgordienko.film_storage.model.Favorite;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.FavoriteRepository;
import ru.danilgordienko.film_storage.service.FavoriteService;
import ru.danilgordienko.film_storage.service.MovieService;
import ru.danilgordienko.film_storage.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserMapping userMapping;
    private final UserService userService;
    private final MovieService movieService;

    //добавление фильма в избранное
    public void addFavorite(Long id, String username) {
        try {
            log.info("Добавление фильма в избранное. Пользователь: {}, Фильм ID: {}", username, id);

            var user = userService.getUserByUsername(username);
            var movie = movieService.getMovieById(id);

            if (favoriteRepository.existsByUserAndMovie(user, movie)) {
                log.warn("Фильм '{}' уже в избранное пользователем '{}': фильм уже в избранном",
                        movie.getTitle(), user.getUsername());
                throw new FavoriteAlreadyExistsException("Фильм уже в избранном");
            }

            Favorite favorite = Favorite
                    .builder()
                    .movie(movie)
                    .user(user)
                    .build();

            favoriteRepository.save(favorite);
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    @Transactional
    public void removeFavorite(Long id, String username) {
        try {
            log.info("Удаление фильма из избранного. Пользователь: {}, Фильм ID: {}", username, id);

            var user = userService.getUserByUsername(username);
            var movie = movieService.getMovieById(id);

            if (!favoriteRepository.existsByUserAndMovie(user, movie)) {
                log.warn("Попытка удалить фильм '{}' из избранного пользователем '{}': фильм не находится в избранном",
                        movie.getTitle(), user.getUsername());
                throw new FavoriteNotFoundException("Фильм не находится в избранном");
            }

            favoriteRepository.deleteByUserAndMovie(user, movie);
        } catch (DataAccessException e) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Ошибка подключения к базе данных", e);
        }
    }

    public UserFavoritesDto getUserFavoritesByUsername(String username) {
        User user =  userService.getUserByUsername(username);
        return userMapping.toUserFavoritesDto(user);
    }
}

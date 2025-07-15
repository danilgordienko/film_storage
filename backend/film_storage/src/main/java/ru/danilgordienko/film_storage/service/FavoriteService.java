package ru.danilgordienko.film_storage.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.MappingException;
import org.springframework.dao.DataAccessException;
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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserMapping userMapping;
    private final UserService userService;
    private final MovieService movieService;

    //добавление фильма в избранное
    public boolean addFavorite(Long id, String username) {
        try {
            log.info("Добавление фильма в избранное. Пользователь: {}, Фильм ID: {}", username, id);

            var user = userService.getUserByUsername(username);
            var movie = movieService.getMovieById(id);

            if (favoriteRepository.existsByUserAndMovie(user, movie)) {
                log.warn("Фильм '{}' уже в избранное пользователем '{}': фильм уже в избранном",
                        movie.getTitle(), user.getUsername());
                return false;
            }

            Favorite favorite = Favorite
                    .builder()
                    .movie(movie)
                    .user(user)
                    .build();

            favoriteRepository.save(favorite);
            return true;
        } catch (DataAccessException e ) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            return false;
        } catch(EntityNotFoundException e) {
            return false;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при добавлении избранного: {}", e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public boolean removeFavorite(Long id, String username) {
        try {
            log.info("Удаление фильма из избранного. Пользователь: {}, Фильм ID: {}", username, id);

            var user = userService.getUserByUsername(username);
            var movie = movieService.getMovieById(id);

            favoriteRepository.deleteByUserAndMovie(user, movie);
            return true;
        } catch (DataAccessException e ) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            return false;
        } catch(EntityNotFoundException e) {
            return false;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при добавлении избранного: {}", e.getMessage(), e);
            return false;
        }
    }

    public Optional<UserFavoritesDto> getUserFavoritesByUsername(String username) {
        try {
            User user =  userService.getUserByUsername(username);
            return Optional.of(userMapping.toUserFavoritesDto(user));
        } catch (DataAccessException e ) {
            log.error("Ошибка доступа к базе данных: {}", e.getMessage(), e);
            return Optional.empty();
        } catch(MappingException e) {
            log.error("Ошибка при маппинге {}", e.getMessage(), e);
            return Optional.empty();
        } catch(EntityNotFoundException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка получении избранного: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}

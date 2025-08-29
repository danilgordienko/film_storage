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

    // Adding a movie to favorites
    public void addFavorite(Long id, String username) {
        try {
            log.debug("Adding movie to favorites. User: {}, Movie ID: {}", username, id);

            var user = userService.getUserByEmail(username);
            var movie = movieService.getMovieById(id);

            if (favoriteRepository.existsByUserAndMovie(user, movie)) {
                log.warn("Movie '{}' already in favorites for user '{}'", movie.getTitle(), user.getUsername());
                throw new FavoriteAlreadyExistsException("Movie already in favorites");
            }

            Favorite favorite = Favorite.builder()
                    .movie(movie)
                    .user(user)
                    .build();

            favoriteRepository.save(favorite);
            log.debug("Movie '{}' successfully added to favorites for user '{}'", movie.getTitle(), user.getUsername());
        } catch (DataAccessException e) {
            log.error("Database access error while adding favorite", e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }

    @Transactional
    public void removeFavorite(Long id, String username) {
        try {
            log.debug("Removing movie from favorites. User: {}, Movie ID: {}", username, id);

            var user = userService.getUserByEmail(username);
            var movie = movieService.getMovieById(id);

            if (!favoriteRepository.existsByUserAndMovie(user, movie)) {
                log.warn("Attempt to remove movie '{}' from favorites by user '{}': movie not found in favorites",
                        movie.getTitle(), user.getUsername());
                throw new FavoriteNotFoundException("Movie not found in favorites");
            }

            favoriteRepository.deleteByUserAndMovie(user, movie);
            log.debug("Movie '{}' successfully removed from favorites for user '{}'", movie.getTitle(), user.getUsername());
        } catch (DataAccessException e) {
            log.error("Database access error while removing favorite", e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }

    public UserFavoritesDto getUserFavoritesByUsername(String username) {
        User user = userService.getUserByEmail(username);
        log.debug("Retrieving favorites for user '{}'", username);
        return userMapping.toUserFavoritesDto(user);
    }
}

package ru.danilgordienko.film_storage.service;

import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.model.dto.UsersDto.UserFavoritesDto;

@Service
public interface FavoriteService {
    void addFavorite(Long id, String username);
    void removeFavorite(Long id, String username);
    UserFavoritesDto getUserFavoritesByUsername(String username);
}

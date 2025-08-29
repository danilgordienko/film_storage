package ru.danilgordienko.film_storage.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserFavoritesDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserRatingDto;
import ru.danilgordienko.film_storage.service.FavoriteService;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * добавление фильма в избранное
     */
    @PostMapping("/add/movies/{id}")
    public ResponseEntity<String> addFavorite(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[POST /api/favorites/add/movies/{}] Request to add movie to favorites by user={}", id, userDetails.getUsername());
        favoriteService.addFavorite(id, userDetails.getUsername());
        log.info("[POST /api/favorites/add/movies/{}] Movie successfully added to favorites by user={}", id, userDetails.getUsername());
        return ResponseEntity.ok("Favorite added");
    }

    /**
     * удаление фильма из избранного
     */
    @DeleteMapping("/remove/movies/{id}")
    public ResponseEntity<String> deleteFavorite(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[DELETE /api/favorites/remove/movies/{}] Request to remove movie from favorites by user={}", id, userDetails.getUsername());
        favoriteService.removeFavorite(id, userDetails.getUsername());
        log.info("[DELETE /api/favorites/remove/movies/{}] Movie successfully removed from favorites by user={}", id, userDetails.getUsername());
        return ResponseEntity.ok("Favorite deleted");
    }

    //получение избранного текущего пользователя
    @GetMapping
    public ResponseEntity<UserFavoritesDto> getCurrentUserFavorites(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("[GET /api/favorites] Request to get current user's favorites: {}", userDetails.getUsername());
        UserFavoritesDto userFavoritesDto = favoriteService.getUserFavoritesByUsername(userDetails.getUsername());
        log.info("[GET /api/favorites] Favorites successfully retrieved for user={}", userDetails.getUsername());
        return ResponseEntity.ok(userFavoritesDto);
    }

}

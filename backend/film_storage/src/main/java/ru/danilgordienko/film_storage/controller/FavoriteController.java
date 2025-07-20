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
                                              @AuthenticationPrincipal UserDetails userDetails){
        log.info("Запрос на добавление фильма в избранное: id={}", id);
        favoriteService.addFavorite(id, userDetails.getUsername());
        log.info("Фильм успешно добавлен в избранное: id={}", id);
        return ResponseEntity.ok("Favorite added");
    }

    /**
     * удаление фильма из избранного
     */
    @DeleteMapping("/remove/movies/{id}")
    public ResponseEntity<String> deleteFavorite(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserDetails userDetails){
        log.info("Запрос на удаление фильма из избранного: id={}", id);
        favoriteService.removeFavorite(id,  userDetails.getUsername());
        log.info("Фильм успешно удалён из избранного: id={}", id);
        return ResponseEntity.ok("Favorite delete");
    }

    //получение избранного текущего пользователя
    @GetMapping
    public ResponseEntity<UserFavoritesDto> getCurrentUserFavorites(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Запрос избранного текущего пользователя: {}", userDetails.getUsername());
        UserFavoritesDto  userFavoritesDto = favoriteService.getUserFavoritesByUsername(userDetails.getUsername());
        log.info("избранное текущего пользователя {} успешно полученно", userDetails.getUsername());
        return ResponseEntity.ok(userFavoritesDto);
    }

}

package ru.danilgordienko.film_storage.controller;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.danilgordienko.film_storage.service.FavoriteService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * добавление фильма в избранное
     */
    @PostMapping("movies/{id}/favorites")
    public ResponseEntity<String> addFavorite(@PathVariable Long id){
        log.info("Запрос на добавление фильма в избранное: id={}", id);
        try {
            favoriteService.addFavorite(id);
            log.info("Фильм успешно добавлен в избранное: id={}", id);
            return ResponseEntity.ok("Favorite added");
        } catch (EntityNotFoundException e) {
            log.warn("Фильм не найден при добавлении в избранное: id={}, message={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка сервера при добавлении фильма в избранное: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка сервера");
        }
    }

    /**
     * удаление фильма из избранного
     */
    @DeleteMapping("movies/{id}/favorites")
    public ResponseEntity<String> deleteFavorite(@PathVariable Long id){
        log.info("Запрос на удаление фильма из избранного: id={}", id);
        try {
            favoriteService.removeFavorite(id);
            log.info("Фильм успешно удалён из избранного: id={}", id);
            return ResponseEntity.ok("Favorite delete");
        } catch (EntityNotFoundException e) {
            log.warn("Фильм не найден при удалении из избранного: id={}, message={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка сервера при удалении фильма из избранного: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка сервера");
        }
    }





}

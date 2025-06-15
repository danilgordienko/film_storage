package ru.danilgordienko.film_storage.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import ru.danilgordienko.film_storage.DTO.RatingDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserRatingDto;
import ru.danilgordienko.film_storage.service.RatingService;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final RatingService ratingService;

    /**
     * добавление рейтинга к фильму рейтинга
     * @param rating - рейтинг от 1 до 10
     * @param bindingResult - собирает все ошибки валидации
     */
    @PostMapping("/add/movies/{id}")
    public ResponseEntity<String> addRating(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody @Valid RatingDto rating,
                                            BindingResult bindingResult) {
        log.info("Запрос на добавление рейтинга фильму: id={}, rating={}", id, rating);

        try {
            //проверяем есть ли ошибки валидации полученного рейтинга
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getAllErrors()
                        .stream()
                        .map(ObjectError::getDefaultMessage)
                        .collect(Collectors.joining(", "));
                log.warn("Ошибки валидации рейтинга для фильма id={}: {}", id, errorMessage);
                return ResponseEntity.badRequest().body(errorMessage);
            }

            if(!ratingService.addRating(id, rating, userDetails.getUsername())){
                log.warn("Ошибка при добавлении рейтинга фильму: id={}", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            log.info("Рейтинг успешно добавлен фильму: id={}", id);
            return ResponseEntity.ok("Рейтинг добавлен");
        } catch (Exception e) {
            log.error("Ошибка сервера при добавлении рейтинга фильму: id={}, error={}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка сервера");
        }
    }

    @GetMapping("users/me")
    public ResponseEntity<UserRatingDto> getCurrentUserRatings(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Запрос оценок текущего пользователя: {}", userDetails.getUsername());

        return ratingService.getUserRatingsByUsername(userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Оценки пользователя с username: {} не найдены", userDetails.getUsername());
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("users/{id}")
    public ResponseEntity<UserRatingDto> getUserRatings(@PathVariable Long id){
        log.info("Запрос оценок пользователя с id: {}", id);

        return ratingService.getUserRatings(id)
                .map(user -> {
                    log.info("Пользователь найден, всего оценок: {}", user.getRatings().size());
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    log.warn("Пользователь с id: {} не найден", id);
                    return  ResponseEntity.notFound().build();
                });
    }
}

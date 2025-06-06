package ru.danilgordienko.film_storage.controller;


import jakarta.persistence.EntityNotFoundException;
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
import ru.danilgordienko.film_storage.service.MovieService;
import ru.danilgordienko.film_storage.service.RatingService;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final RatingService ratingService;

    /**
     * добавление рейтинга к фильму рейтинга
     * @param rating - рейтинг от 1 до 10
     * @param bindingResult - собирает все ошибки валидации
     */
    @PostMapping("/movies/{id}/ratings")
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
}

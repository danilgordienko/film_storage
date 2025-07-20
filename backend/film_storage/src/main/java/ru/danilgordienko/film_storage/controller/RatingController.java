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
        ratingService.addRating(id, rating, userDetails.getUsername());
        log.info("Рейтинг успешно добавлен фильму: id={}", id);
        return ResponseEntity.ok("Рейтинг добавлен");
    }

    // получение оценок текущего пользователя
    @GetMapping("users/me")
    public ResponseEntity<UserRatingDto> getCurrentUserRatings(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Запрос оценок текущего пользователя: {}", userDetails.getUsername());
        return ResponseEntity.ok(ratingService.getUserRatingsByUsername(userDetails.getUsername()));
    }

    // получение оценок полльзоваля по id
    @GetMapping("users/{id}")
    public ResponseEntity<UserRatingDto> getUserRatings(@PathVariable Long id){
        log.info("Запрос оценок пользователя с id: {}", id);

        UserRatingDto user = ratingService.getUserRatings(id);
        log.info("Пользователь найден, всего оценок: {}", user.getRatings().size());
        return ResponseEntity.ok(user);
    }
}

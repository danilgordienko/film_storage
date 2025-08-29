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
     */
    @PostMapping("/add/movies/{id}")
    public ResponseEntity<String> addRating(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody @Valid RatingDto rating) {
        log.info("POST /api/ratings/add/movies/{} - Adding rating {} by user {}", id, rating, userDetails.getUsername());
        ratingService.addRating(id, rating, userDetails.getUsername());
        log.info("POST /api/ratings/add/movies/{} - Rating added successfully by user {}", id, userDetails.getUsername());
        return ResponseEntity.ok("Rating added");
    }

    // получение оценок текущего пользователя
    @GetMapping("users/me")
    public ResponseEntity<UserRatingDto> getCurrentUserRatings(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/ratings/users/me - Fetching ratings for current user {}", userDetails.getUsername());
        var response = ratingService.getUserRatingsByUsername(userDetails.getUsername());
        log.info("GET /api/ratings/users/me - Successfully fetched ratings for current user {}", userDetails.getUsername());

        return ResponseEntity.ok(response);
    }

    // получение оценок полльзоваля по id
    @GetMapping("users/{id}")
    public ResponseEntity<UserRatingDto> getUserRatings(@PathVariable Long id){
        log.info("GET /api/ratings/users/{} - Fetching user ratings", id);
        UserRatingDto user = ratingService.getUserRatings(id);
        log.info("GET /api/ratings/users/{} - Successfully fetched ratings, count {}", id, user.getRatings().size());
        return ResponseEntity.ok(user);
    }
}

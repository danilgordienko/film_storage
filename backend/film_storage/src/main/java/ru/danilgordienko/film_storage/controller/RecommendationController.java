package ru.danilgordienko.film_storage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.danilgordienko.film_storage.DTO.RecommendationDto;
import ru.danilgordienko.film_storage.service.RecommendationService;

import javax.management.InstanceAlreadyExistsException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Отправка рекомендации фильма другому пользователю
     */
    @PostMapping
    public ResponseEntity<Void> sendRecommendation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long receiverId,
            @RequestParam Long movieId) {

        String username = userDetails.getUsername();
        log.info("POST /api/recommendations - User {} sends recommendation request of movie {} to user {}", username, movieId, receiverId);
        recommendationService.sendRecommendation(username, receiverId, movieId);
        log.info("POST /api/recommendations - Recommendation sent successfully by user {}", username);
        return ResponseEntity.ok().build();
    }

    /**
     * Отмена отправленной рекомендации
     */
    @DeleteMapping
    public ResponseEntity<Void> cancelRecommendation(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long receiverId,
            @RequestParam Long movieId) {

        String username = userDetails.getUsername();
        log.info("DELETE /api/recommendations - User {} cancels recommendation request of movie {} to user {}", username, movieId, receiverId);
        recommendationService.cancelRecommendation(username, receiverId, movieId);
        log.info("DELETE /api/recommendations - Recommendation cancelled successfully by user {}", username);
        return ResponseEntity.ok().build();
    }

    /**
     * Получение всех отправленных рекомендаций текущего пользователя
     */
    @GetMapping("/sent")
    public ResponseEntity<List<RecommendationDto>> getSentRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("GET /api/recommendations/sent - Fetching sent recommendations of user {}", username);
        List<RecommendationDto> recommendations = recommendationService.findAllBySender(username);
        log.info("GET /api/recommendations/sent - Found {} sent recommendations for user {}", recommendations.size(), username);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Получение всех полученных рекомендаций текущего пользователя
     */
    @GetMapping("/received")
    public ResponseEntity<List<RecommendationDto>> getReceivedRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("GET /api/recommendations/received - Fetching received recommendations of user {}", username);
        List<RecommendationDto> recommendations = recommendationService.findAllByReceiver(username);
        log.info("GET /api/recommendations/received - Found {} received recommendations for user {}", recommendations.size(), username);
        return ResponseEntity.ok(recommendations);
    }
}
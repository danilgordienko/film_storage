package ru.danilgordienko.film_storage.service;

import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.RecommendationDto;
import java.util.List;

@Service
public interface RecommendationService {
    void sendRecommendation(String username, Long receiverId, Long movieId);
    void cancelRecommendation(String username, Long receiverId, Long movieId);
    List<RecommendationDto> findAllBySender(String username);
    List<RecommendationDto> findAllByReceiver(String username);
}

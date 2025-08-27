package ru.danilgordienko.film_storage.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.RecommendationDto;
import ru.danilgordienko.film_storage.DTO.mapping.RecommendationMapping;
import ru.danilgordienko.film_storage.exception.RecommendationAlreadyExistsException;
import ru.danilgordienko.film_storage.exception.RecommendationNotFoundException;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.model.Recommendation;
import ru.danilgordienko.film_storage.model.User;
import ru.danilgordienko.film_storage.repository.RecommendationRepository;
import ru.danilgordienko.film_storage.service.MovieService;
import ru.danilgordienko.film_storage.service.RecommendationService;
import ru.danilgordienko.film_storage.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserService userService;
    private final MovieService movieService;
    private final RecommendationMapping  recommendationMapping;

    @Transactional
    public void sendRecommendation(String username, Long receiverId, Long movieId) {
        User sender = userService.getUserByEmail(username);
        User receiver = userService.getUserById(receiverId);
        Movie movie = movieService.getMovieById(movieId);

        if (recommendationRepository.existsBySenderAndReceiverAndMovie(sender, receiver, movie)) {
            log.warn("Отзыв не добавлен: пользователь с именем '{}' уже оставлял отзыв ", username);
            throw new RecommendationAlreadyExistsException("Рекомендация фильма " + movie.getTitle()
                    + " от " + sender.getUsername() + " к "
                    + receiver.getUsername() + " уже существует");
        }

        Recommendation recommendation = Recommendation.builder()
                .receiver(receiver)
                .movie(movie)
                .sender(sender)
                .build();

        recommendationRepository.save(recommendation);
    }

    @Transactional
    public void cancelRecommendation(String username, Long receiverId, Long movieId) {
        User sender = userService.getUserByEmail(username);
        User receiver = userService.getUserById(receiverId);
        Movie movie = movieService.getMovieById(movieId);

        Recommendation recommendation = recommendationRepository.findBySenderAndReceiverAndMovie(sender, receiver, movie)
                .orElseThrow(() -> {
                    log.warn("Рекомендация не найдена");
                    return new RecommendationNotFoundException("Рекомендация фильма " + movie.getTitle()
                            + " от " + sender.getUsername() + " к "
                            + receiver.getUsername() + " не существует");
                });

        recommendationRepository.delete(recommendation);
    }

    // получение рекомендаций от текущего пользоавтеля
    public List<RecommendationDto> findAllBySender(String username) {
        User sender = userService.getUserByEmail(username);

        return recommendationRepository.findBySender(sender).stream()
                .map(recommendationMapping::toRecommendationDto).toList();
    }

    // получение рекомендаций для текущего пользоавтеля
    public List<RecommendationDto> findAllByReceiver(String username) {
        User receiver = userService.getUserByEmail(username);

        return recommendationRepository.findByReceiver(receiver).stream()
                .map(recommendationMapping::toRecommendationDto).toList();
    }
}

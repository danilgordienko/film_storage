package ru.danilgordienko.film_storage.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.model.dto.RecommendationDto;
import ru.danilgordienko.film_storage.model.dto.mapping.RecommendationMapping;
import ru.danilgordienko.film_storage.exception.RecommendationAlreadyExistsException;
import ru.danilgordienko.film_storage.exception.RecommendationNotFoundException;
import ru.danilgordienko.film_storage.model.entity.Movie;
import ru.danilgordienko.film_storage.model.entity.Recommendation;
import ru.danilgordienko.film_storage.model.entity.User;
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
    private final RecommendationMapping recommendationMapping;

    @Transactional
    public void sendRecommendation(String username, Long receiverId, Long movieId) {
        User sender = userService.getUserByEmail(username);
        User receiver = userService.getUserById(receiverId);
        Movie movie = movieService.getMovieById(movieId);

        if (recommendationRepository.existsBySenderAndReceiverAndMovie(sender, receiver, movie)) {
            log.warn("Recommendation not added: user '{}' has already recommended this movie", username);
            throw new RecommendationAlreadyExistsException("Recommendation for movie " + movie.getTitle()
                    + " from " + sender.getUsername() + " to "
                    + receiver.getUsername() + " already exists");
        }

        Recommendation recommendation = Recommendation.builder()
                .receiver(receiver)
                .movie(movie)
                .sender(sender)
                .build();

        recommendationRepository.save(recommendation);
        log.debug("Recommendation sent by user '{}' for movie '{}' to user '{}'",
                sender.getUsername(), movie.getTitle(), receiver.getUsername());
    }

    @Transactional
    public void cancelRecommendation(String username, Long receiverId, Long movieId) {
        User sender = userService.getUserByEmail(username);
        User receiver = userService.getUserById(receiverId);
        Movie movie = movieService.getMovieById(movieId);

        Recommendation recommendation = recommendationRepository.findBySenderAndReceiverAndMovie(sender, receiver, movie)
                .orElseThrow(() -> {
                    log.warn("Recommendation not found");
                    return new RecommendationNotFoundException("Recommendation for movie " + movie.getTitle()
                            + " from " + sender.getUsername() + " to "
                            + receiver.getUsername() + " does not exist");
                });

        recommendationRepository.delete(recommendation);
        log.debug("Recommendation cancelled by user '{}' for movie '{}' to user '{}'",
                sender.getUsername(), movie.getTitle(), receiver.getUsername());
    }

    // получение рекомендаций от текущего пользоавтеля
    public List<RecommendationDto> findAllBySender(String username) {
        User sender = userService.getUserByEmail(username);

        log.debug("Fetching all recommendations sent by user '{}'", username);
        return recommendationRepository.findBySender(sender).stream()
                .map(recommendationMapping::toRecommendationDto).toList();
    }

    // получение рекомендаций для текущего пользоавтеля
    public List<RecommendationDto> findAllByReceiver(String username) {
        User receiver = userService.getUserByEmail(username);

        log.debug("Fetching all recommendations received by user '{}'", username);
        return recommendationRepository.findByReceiver(receiver).stream()
                .map(recommendationMapping::toRecommendationDto).toList();
    }
}


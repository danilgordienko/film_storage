package ru.danilgordienko.film_storage.service;

import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.model.dto.RatingDto;
import ru.danilgordienko.film_storage.model.dto.UsersDto.UserRatingDto;

@Service
public interface RatingService {
    void addRating(Long id, RatingDto rating, String username);
    UserRatingDto getUserRatingsByUsername(String username);
    UserRatingDto getUserRatings(Long id, String username);
}

package ru.danilgordienko.film_storage.service;

import org.springframework.stereotype.Service;
import ru.danilgordienko.film_storage.DTO.RatingDto;
import ru.danilgordienko.film_storage.DTO.UsersDto.UserRatingDto;

@Service
public interface RatingService {
    void addRating(Long id, RatingDto rating, String username);
    UserRatingDto getUserRatingsByUsername(String username);
    UserRatingDto getUserRatings(Long id);
}

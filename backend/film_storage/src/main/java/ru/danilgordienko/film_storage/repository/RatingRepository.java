package ru.danilgordienko.film_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.danilgordienko.film_storage.model.entity.Movie;
import ru.danilgordienko.film_storage.model.entity.Rating;
import ru.danilgordienko.film_storage.model.entity.User;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    boolean existsByUserAndMovie(User user, Movie movie);
}

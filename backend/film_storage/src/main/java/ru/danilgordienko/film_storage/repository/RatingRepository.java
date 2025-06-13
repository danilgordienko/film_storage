package ru.danilgordienko.film_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.danilgordienko.film_storage.model.Rating;
import ru.danilgordienko.film_storage.model.User;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    boolean existsByUser(User user);
}

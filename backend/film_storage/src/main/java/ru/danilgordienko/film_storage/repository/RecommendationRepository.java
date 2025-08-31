package ru.danilgordienko.film_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.danilgordienko.film_storage.model.entity.Movie;
import ru.danilgordienko.film_storage.model.entity.Recommendation;
import ru.danilgordienko.film_storage.model.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation,Long> {

    boolean existsBySenderAndReceiverAndMovie(User sender, User receiver, Movie movie);

    List<Recommendation> findBySender(User sender);

    List<Recommendation> findByReceiver(User receiver);

    Optional<Recommendation> findBySenderAndReceiverAndMovie(User sender, User receiver, Movie movie);
}

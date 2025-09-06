package ru.danilgordienko.film_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.danilgordienko.film_storage.model.entity.Favorite;
import ru.danilgordienko.film_storage.model.entity.Movie;
import ru.danilgordienko.film_storage.model.entity.User;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite,Long> {

    void deleteByUserAndMovie(User user, Movie movie);

    boolean existsByUserAndMovie(User user, Movie movie);
}

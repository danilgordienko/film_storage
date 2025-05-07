package ru.danilgordienko.film_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.danilgordienko.film_storage.model.Favorite;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.model.User;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite,Long> {

    void deleteByUserAndMovie(User user, Movie movie);
}

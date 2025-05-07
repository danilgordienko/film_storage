package ru.danilgordienko.film_storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.danilgordienko.film_storage.model.Genre;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findByName(String name);

    Optional<Genre> findByTmdbId(Long tmdbId);
}

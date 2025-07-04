package ru.danilgordienko.film_storage.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.danilgordienko.film_storage.model.Movie;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @EntityGraph(attributePaths = {"genres", "ratings"})
    Optional<Movie> findById(Long id);

    @EntityGraph(attributePaths = {"genres", "ratings"})
    List<Movie> findAll();

    @EntityGraph(attributePaths = {"genres", "ratings"})
    Page<Movie> findAll(Pageable pageable);
}

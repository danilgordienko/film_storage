package ru.danilgordienko.film_storage.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ru.danilgordienko.film_storage.model.MovieDocument;

import java.util.List;

public interface MovieSearchRepository extends ElasticsearchRepository<MovieDocument, Long> {

    List<MovieDocument> searchByTitle(String title);

    Page<MovieDocument> findByTitleContaining(String title, Pageable pageable);

}

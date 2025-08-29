package ru.danilgordienko.film_storage.service.impl;


import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieDetailsDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListCacheDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListDto;
import ru.danilgordienko.film_storage.DTO.PageDto;
import ru.danilgordienko.film_storage.DTO.mapping.MovieMapping;
import ru.danilgordienko.film_storage.MovieAPI.MovieApiClient;
import ru.danilgordienko.film_storage.exception.*;
import ru.danilgordienko.film_storage.model.*;
import ru.danilgordienko.film_storage.repository.GenreRepository;
import ru.danilgordienko.film_storage.repository.MovieRepository;
import ru.danilgordienko.film_storage.repository.MovieSearchRepository;
import ru.danilgordienko.film_storage.service.MovieService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableCaching
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieMapping  movieMapping;
    private final MovieSearchRepository movieSearchRepository;
    private final MovieApiClient movieApiClient;
    private final int size = 20;

    // Получение всех фильмов из бд
    public List<MovieListDto> getAllMovies(){
        try {
            var movies = movieRepository.findAll().stream()
                    .map(movieMapping::toMovieListDto)
                    .toList();
            log.debug("Found {} movies", movies.size());
            return movies;
        } catch (DataAccessException e) {
            log.error("Database access error: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }

    // Получение страницы фильмов
    @Cacheable(value = "movies", key = "#page", condition = "#page == 0")
    public PageDto getMoviesPage(int page){
        try {
            if (page < 0) {
                log.warn("Attempted to get page < 0: {}", page);
            }
            log.debug("Getting all movies from database");
            Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
            Page<Movie> moviePage = movieRepository.findAll(pageable);

            List<MovieListCacheDto> dtoList = moviePage.getContent()
                    .stream()
                    .map(movieMapping::toMovieListCacheDto)
                    .toList();

            Page<MovieListCacheDto> dtoPage = new PageImpl<>(dtoList, pageable, moviePage.getTotalElements());
            log.debug("Found {} movies", dtoList.size());
            return movieMapping.toPageDto(dtoPage);
        } catch (DataAccessException e) {
            log.error("Database access error: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }

    // Получение всех жанров из бд
    private List<Genre> getAllGenres(){
        try {
            log.debug("Getting all genres from database");
            var genres = genreRepository.findAll();
            log.debug("Found {} genres", genres.size());
            return genres;
        } catch (DataAccessException e) {
            log.error("Database access error: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }

    // Получение фильма по id
    public MovieDetailsDto getMovie(Long id) {
        var movie = getMovieById(id);
        return movieMapping.toMovieDetailsDto(movie);
    }

    public Movie getMovieById(Long id) {
        try {
            log.debug("Getting movie with ID = {}", id);
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> {
                        log.debug("Movie with ID {} not found", id);
                        return new MovieNotFoundException("Movie with id " + id + " not found");
                    });
            log.debug("Movie found: {}", movie.getTitle());
            return movie;
        } catch (DataAccessException e) {
            log.error("Database access error: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Database connection error", e);
        }
    }

    // Поиск фильмов по запросу
    public List<MovieListDto> searchMoviesByTitle(String query) {
        try {
            log.debug("Searching movies in Elasticsearch by title: {}", query);

            if (query == null || query.isBlank()) {
                log.warn("Empty or null search query");
                return List.of();
            }
            var searchResults = movieSearchRepository.searchByTitle(query);

            var movies = searchResults.stream()
                    .map(movieMapping::toMovieListDto)
                    .toList();

            log.debug("Found {} movies in Elasticsearch for query '{}'", movies.size(), query);

            return movies;
        } catch (ElasticsearchException | RestClientException e) {
            log.error("Error interacting with Elasticsearch", e);
            throw new ElasticsearchConnectionException("Failed to search movies in Elasticsearch", e);
        }
    }

    public PageDto searchMoviesPageByTitle(String query, int page) {
        try {
            log.debug("Searching movies in Elasticsearch by title: {}", query);
            Pageable pageable = PageRequest.of(page, size);
            var searchResults = movieSearchRepository.findByTitleContaining(query, pageable);

            List<MovieListDto> dtoList = searchResults.getContent()
                    .stream()
                    .map(movieMapping::toMovieListDto)
                    .toList();

            Page<MovieListDto> dtoPage = new PageImpl<>(dtoList, pageable, searchResults.getTotalElements());
            log.debug("Found {} movies", dtoList.size());
            return movieMapping.toMovieListPageDto(dtoPage);
        } catch (ElasticsearchException | RestClientException e) {
            log.error("Error interacting with Elasticsearch", e);
            throw new ElasticsearchConnectionException("Failed to search movies in Elasticsearch", e);
        }
    }

    @Cacheable(value = "movies", key = "#id", cacheManager = "binaryCacheManager")
    public byte[] getPoster(Long id) {
        Movie movie = getMovieById(id);
        return movieApiClient.getPoster(movie);
    }

    private void attachGenresToMovies(List<Movie> movies) {
        Map<Long, Genre> genresMap = getAllGenres().stream()
                .collect(Collectors.toMap(Genre::getTmdbId, Function.identity()));

        for (Movie movie : movies) {
            Set<Genre> attachedGenres = movie.getGenres().stream()
                    .map(genre -> genresMap.get(genre.getTmdbId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            movie.setGenres(attachedGenres);
        }
    }

    @Transactional
    public boolean getPopularMovies() {
        int page = 1;
        var movies = movieApiClient.getPopularMoviesPage(page);

        log.debug("Received {} movies", movies.size());
        saveReceivedMovies(movies);
        return true;
    }

    private void saveReceivedMovies(List<MovieDto> movies) {
        try {
            var mappedMovies = movies.stream().map(movieMapping::toMovie).toList();
            attachGenresToMovies(mappedMovies);
            saveMoviesDB(mappedMovies);
            saveMoviesES(mappedMovies);

            log.debug("Saved {} movies", movies.size());
        } catch (ElasticsearchException | RestClientException e) {
            log.error("Error interacting with Elasticsearch", e);
            throw new ElasticsearchConnectionException("Failed to save movies in Elasticsearch", e);
        } catch (DataAccessException e) {
            log.error("Database access error: {}", e.getMessage(), e);
            throw new DatabaseConnectionException("Database connection error", e);
        } catch (Exception e) {
            log.error("Error saving movies", e);
            throw new MovieSaveException("Failed to save movies", e);
        }
    }

    @Override
    @Transactional
    @EventListener(MovieApiClient.MoviesReceivedEvent.class)
    public void populateMovies(MovieApiClient.MoviesReceivedEvent event) {
        log.debug("Saving recently released movies");
        List<MovieDto> movies = event.getMovies();
        saveReceivedMovies(movies);
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        if (movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
            movieSearchRepository.deleteById(id);
            return;
        }
        log.warn("Attempted to delete movie with id={} which dont exist", id);
        throw new MovieNotFoundException("Movie with id " + id + " does not exist");
    }

    @Override
    public void addMovie(Long id) {
        // implementation empty
    }

    private void saveMoviesDB(List<Movie> movies){
        log.debug("Saving movies in database");
        List<Movie> savedMovies = movieRepository.saveAll(movies);

        if (savedMovies.size() != movies.size()) {
            throw new IllegalStateException("Not all movies were saved. Transaction rollback.");
        }
    }

    private void saveMoviesES(List<Movie> movies){
        log.debug("Saving movies in Elasticsearch");
        var savedMovies = movieSearchRepository.saveAll(movies
                .stream()
                .map(movieMapping::toMovieDocument)
                .toList());

        long savedCount = StreamSupport.stream(savedMovies.spliterator(), false).count();
        if (savedCount != movies.size()) {
            throw new IllegalStateException("Not all movies were saved in Elasticsearch");
        }
    }
}

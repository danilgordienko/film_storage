package ru.danilgordienko.film_storage.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieDetailsDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListDto;
import ru.danilgordienko.film_storage.DTO.PageDto;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.service.MovieService;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Slf4j
public class MovieController {

    private final MovieService movieService;

    /**
     * получение списка всех фильмов
     */
    @GetMapping("/all")
    public ResponseEntity<List<MovieListDto>> getAllMovies(){
        log.info("GET /api/movies/all - Fetching all movies");

        List<MovieListDto> movies = movieService.getAllMovies();
        if (movies.isEmpty()) {
            log.warn("GET /api/movies/all - No movies found");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        log.info("GET /api/movies/all - Returning {} movies", movies.size());
        return ResponseEntity.ok(movies);
    }

    /**
     * получение списка фильмов по страницам
     */
    @GetMapping
    public ResponseEntity<PageDto> getMoviesPage(@RequestParam("page") int page){
        log.info("GET /api/movies - Fetching page {}", page);

        PageDto movies = movieService.getMoviesPage(page);

        if (movies.getContent().isEmpty()) {
            log.warn("GET /api/movies - No movies found for page {}", page);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        log.info("GET /api/movies - Returning {} movies for page {}", movies.getContent().size(), page);
        return ResponseEntity.ok(movies);
    }

    /**
     * Получение фильма по id
     * @param id
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailsDto> getMovie(@PathVariable Long id){
        log.info("GET /api/movies/{} - Fetching movie details", id);
        MovieDetailsDto movie = movieService.getMovie(id);
        log.info("GET /api/movies/{} - Successfully fetched movie details", id);
        return ResponseEntity.ok(movie);
    }

    // получение постера к фильму
    @GetMapping("/{id}/poster")
    public ResponseEntity<byte[]> getMoviePoster(@PathVariable Long id) {
        log.info("GET /api/movies/{}/poster - Fetching movie poster", id);
        byte[] poster = movieService.getPoster(id);
        if (poster.length != 0) {
            log.info("GET /api/movies/{}/poster - Poster retrieved successfully", id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(poster);
        }
        log.warn("GET /api/movies/{}/poster - Poster not found", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // поиск всех фильмов по запросу query
    @GetMapping("/search/all")
    public ResponseEntity<List<MovieListDto>> searchMovies(@RequestParam("query") String query) {
        log.info("GET /api/movies/search/all - Searching movies with query '{}'", query);

        List<MovieListDto> movies = movieService.searchMoviesByTitle(query);
        if (movies.isEmpty()) {
            log.warn("GET /api/movies/search/all - No movies found for query '{}'", query);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        log.info("GET /api/movies/search/all - Found {} movies for query '{}'", movies.size(), query);
        return ResponseEntity.ok(movies);
    }

    // поиск фильмов по запросу query по страницам
    @GetMapping("/search")
    public ResponseEntity<PageDto> searchMovies(@RequestParam("query") String query,
                                                @RequestParam("page") int page) {
        log.info("GET /api/movies/search - Searching movies with query '{}' on page {}", query, page);

        PageDto movies = movieService.searchMoviesPageByTitle(query, page);
        if (movies.getContent().isEmpty()) {
            log.warn("GET /api/movies/search - No movies found for query '{}' on page {}", query, page);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        log.info("GET /api/movies/search - Found {} movies for query '{}' on page {}", movies.getContent().size(), query, page);
        return ResponseEntity.ok(movies);
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteMovie(@RequestParam("id") Long id){
        log.info("POST /api/movies/{id} - Deleting movie with id {}", id);
        movieService.deleteMovie(id);
        log.info("POST /api/movies/{id} - Movie with id {} deleted successfully", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/init")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> initMovies(){
        log.info("POST /api/movies/init - Initializing movies");
        if (!movieService.getPopularMovies()) {
            log.warn("POST /api/movies/init - Failed to add movies");
            return ResponseEntity.badRequest().build();
        }
        log.info("POST /api/movies/init - Movies added successfully");
        return  ResponseEntity.ok().build();
    }

}

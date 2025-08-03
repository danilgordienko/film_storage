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
        log.info("GET /api/movies/all - fetching all movies");

        List<MovieListDto> movies = movieService.getAllMovies();

        if (movies.isEmpty()) {
            log.warn("No movies found");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        log.info("Returning {} movies", movies.size());
        return ResponseEntity.ok(movies);
    }

    /**
     * получение списка фильмов по страницам
     */
    @GetMapping
    public ResponseEntity<PageDto> getMoviesPage(@RequestParam("page") int page){
        log.info("GET /api/movies - fetching page of movies");

        PageDto movies = movieService.getMoviesPage(page);

        if (movies.getContent().isEmpty()) {
            log.warn("No movies found");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        log.info("Returning {} movies", movies.getContent().size());
        return ResponseEntity.ok(movies);
    }

    /**
     * Получение фильма по id
     * @param id
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailsDto> getMovie(@PathVariable Long id){
        log.info("GET /api/movies/{} - запрос фильм с id", id);

        MovieDetailsDto movie = movieService.getMovie(id);
        return ResponseEntity.ok(movie);
    }

    // получение постера к фильму
    @GetMapping("/{id}/poster")
    public ResponseEntity<byte[]> getMoviePoster(@PathVariable Long id) {
        log.info("GET /api/movies/{id}/poster - запрос постера фильма с id: {}",id);
        byte[] poster = movieService.getPoster(id);
        if (poster.length != 0) {
            log.info("Постер фильма с id {} успешно получен", id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(poster);
        }
        log.info("Не удалсь получить постер фильма с id {}", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // поиск всех фильмов по запросу query
    @GetMapping("/search/all")
    public ResponseEntity<List<MovieListDto>> searchMovies(@RequestParam("query") String query) {
        log.info("GET /api/movies/search/all - поиск всех фильмов по запросу: {}", query);

        List<MovieListDto> movies = movieService.searchMoviesByTitle(query);

        if (movies.isEmpty()) {
            log.warn("По запросу '{}' фильмы не найдены", query);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        log.info("По запросу '{}' найдено {} фильмов", query, movies.size());
        return ResponseEntity.ok(movies);
    }

    // поиск фильмов по запросу query по страницам
    @GetMapping("/search")
    public ResponseEntity<PageDto> searchMovies(@RequestParam("query") String query,
                                                           @RequestParam("page") int page) {
        log.info("GET /api/movies/search - поиск фильмов по запросу: {}", query);

        PageDto movies = movieService.searchMoviesPageByTitle(query, page);

        if (movies.getContent().isEmpty()) {
            log.warn("По запросу '{}' фильмы не найдены", query);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        log.info("По запросу '{}' найдено {} фильмов", query, movies.getContent().size());
        return ResponseEntity.ok(movies);
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteMovie(@RequestParam("id") Long id){
        log.info("Запрос на удаление фильма с id: {}", id);
        movieService.deleteMovie(id);
        log.info("Фильм с id: {} успешно удален", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/init")
    public ResponseEntity<String> initMovies(){
        log.info("POST /api/movies/init - начальное заполнение фильмов");
        if (!movieService.getPopularMovies()) {
            log.warn("Не удалось добавить фильмы");
            return ResponseEntity.badRequest().build();
        }
        log.info("Фильмы успешно добавлены");
        return  ResponseEntity.ok().build();
    }

}

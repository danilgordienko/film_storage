package ru.danilgordienko.film_fetcher.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.danilgordienko.film_fetcher.config.RabbitConfig;
import ru.danilgordienko.film_fetcher.model.dto.response.MovieDto;
import ru.danilgordienko.film_fetcher.model.dto.response.TmdbResponse;
import ru.danilgordienko.film_fetcher.model.dto.request.Genre;
import ru.danilgordienko.film_fetcher.model.enums.RetryableTaskType;
import ru.danilgordienko.film_fetcher.service.MovieApiService;
import ru.danilgordienko.film_fetcher.service.RetryableTaskService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmdbApiService implements MovieApiService {

    private final RetryableTaskService retryableTaskService;

    @Value("${app.key}")
    private String API_KEY;
    private final WebClient webClient;
    private Map<Long, Genre> genres = new HashMap<>();

    private String getGenreUrl() {
        return "https://api.themoviedb.org/3/genre/movie/list?api_key=" + API_KEY + "&language=ru-RU";
    }

    private String getMovieUrl(int page) {
        return "https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY +
                "&language=ru-RU&page=" + page;
    }

    private String getImageUrl() {
        return "https://image.tmdb.org/t/p/w500/";
    }

    private String getRecentMovieUrl() {
        return "https://api.themoviedb.org/3/discover/movie?api_key=" +
                API_KEY +
                "&language=ru-RU" +
                "&sort_by=popularity.desc" +
                "&release_date.gte=%s" +
                "&release_date.lte=%s" +
                "&vote_count.gte=50" +
                "&vote_average.gte=6";
    }

    @PostConstruct
    public void init() {
        loadGenres();
    }


    @Scheduled(cron = "0 0 3 * * MON")
    //@Scheduled(cron = "0 * * * * *")
    public void populateMovies(){
        getRecentlyReleasedMovies(7)
                .doOnNext(movies -> {
                    retryableTaskService.createRetryableTask(movies, RetryableTaskType.SEND_MOVIE_REQUEST);
                    log.debug("Task for sending {} movies received", movies.size());
                })
                .subscribe();
    }

    @RabbitListener(queues = RabbitConfig.POSTER_QUEUE)
    public byte[] handlePosterRequest(String posterPath) {
        return downloadPoster(posterPath).block();
    }

    @RabbitListener(queues = RabbitConfig.MOVIES_PAGE_QUEUE)
    public List<MovieDto> handleMoviesRequest(int page) {
        var res = getPopularMovies(page).block();
        return res;
    }

    public List<Genre> getGenres(){
        log.debug("Attempting to fetch genres");
        //loadGenres();
        return genres.values().stream().toList();
    }

    @Override
    public Mono<List<MovieDto>> getPopularMovies(int page) {
        log.debug("Attempting to fetch popular movies via TMDB API");
        //loadGenres();
        return webClient.get()
                .uri(getMovieUrl(page))
                .retrieve()
                .bodyToMono(TmdbResponse.class)
                .map(r -> {
                    var results = r.getResults();
                    results.forEach(rm -> {
                        var gs = rm.getGenreIds().stream().map(gm -> genres.get(gm)).toList();
                        rm.setGenres(gs);
                    });
                    return results;
                })
                .onErrorResume(e -> {
                    log.error("TMDb API connection error", e);
                    return Mono.just(List.of());
                });
    }

    @Override
    public Mono<byte[]> downloadPoster(String posterPath) {
        log.debug("Attempting to fetch poster via TMDB API");
        if (posterPath == null || posterPath.isBlank()) {
            return Mono.just(new byte[0]);
        }

        String imageUrl = getImageUrl() + posterPath;
        log.debug("Attempting to fetch poster from: {}", imageUrl);
        return webClient.get()
                .uri(imageUrl)
                .accept(MediaType.IMAGE_JPEG)
                .retrieve()
                .bodyToMono(byte[].class)
                .doOnNext(bytes -> log.debug("Poster received, {} bytes", bytes.length))
                .onErrorResume(e -> {
                    log.warn("Error downloading poster from {}: {}", imageUrl, e.getMessage());
                    return Mono.just(new byte[0]);
                });
    }

    @Getter
    @Setter
    public static class GenreResponse {
        private List<Genre> genres;
    }

    public void loadGenres() {
        webClient.get()
                .uri(getGenreUrl())
                .retrieve()
                .bodyToMono(GenreResponse.class)
                .map(genreResponse -> {
                    if (genreResponse.getGenres() != null) {
                        return genreResponse.getGenres().stream()
                                .map(genre -> {
                                    Genre g = new Genre();
                                    g.setName(genre.getName());
                                    g.setTmdbId(genre.getTmdbId());
                                    genres.put(genre.getTmdbId(), genre);
                                    return g;
                                })
                                .toList();
                    }
                    return List.<Genre>of();
                })
                .onErrorResume(e -> {
                    log.error("TMDb API connection error", e);
                    return Mono.just(List.of());
                })
                .subscribe();
    }

    @Override
    public Mono<List<MovieDto>> getRecentlyReleasedMovies(int days) {
        log.debug("Attempting to fetch recently released movies via TMDB API");
        //loadGenres();

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String url = String.format(
                getRecentMovieUrl(),
                formatter.format(startDate),
                formatter.format(today)
        );

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(TmdbResponse.class)
                .map(r -> {
                    var results = r.getResults();
                    results.forEach(rm -> {
                        var gs = rm.getGenreIds().stream()
                                .map(genres::get)
                                .toList();
                        rm.setGenres(gs);
                    });
                    return results;
                })
                .onErrorResume(e -> {
                    log.error("TMDb API connection error", e);
                    return Mono.just(List.of());
                });
    }
}

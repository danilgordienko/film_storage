package ru.danilgordienko.film_fetcher.service.impl;

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
import ru.danilgordienko.film_fetcher.model.dto.request.TmdbMovie;
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

    private String getMovieUrl() {
        return "https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY + "&language=ru-RU";
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

    //@Scheduled(cron = "0 0 3 * * MON")
    @Scheduled(cron = "0 * * * * *")
    public void populateMovies(){
        getRecentlyReleasedMovies(7)
                .doOnNext(movies -> {
                        retryableTaskService.createRetryableTask(movies, RetryableTaskType.SEND_MOVIE_REQUEST);
                        log.debug("Задача на отправку {} фильмов получена", movies.size());
                })
                .subscribe();
    }

    @RabbitListener(queues = RabbitConfig.POSTER_QUEUE)
    public byte[] handlePosterRequest(String posterPath) {
        return downloadPoster(posterPath).block();
    }

    @Override
    public Mono<List<TmdbMovie>> getPopularMovies() {
        log.info("Getting popular movies");
        loadGenres();
        return webClient.get()
                .uri(getMovieUrl())
                .retrieve()
//                .onStatus(HttpStatus::isError, clientResponse -> {
//                    log.error("TMDb API error: {}", clientResponse.statusCode());
//                    return Mono.error(new RuntimeException("TMDb API error"));
//                })
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
                    log.error("Error accessing TMDb API", e);
                    return Mono.just(List.of());
                });
    }

    @Override
    public Mono<byte[]> downloadPoster(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return Mono.just(new byte[0]);
        }

        String imageUrl = getImageUrl() + posterPath;
        log.info("Fetching poster from: {}", imageUrl);
        return webClient.get()
                .uri(imageUrl)
                .accept(MediaType.IMAGE_JPEG)
                .retrieve()
//                .onStatus(HttpStatus::isError, response -> {
//                    log.error("Failed to fetch poster, status: {}", response.statusCode());
//                    return Mono.error(new RuntimeException("Failed to fetch poster"));
//                })
                .bodyToMono(byte[].class)
                .doOnNext(bytes -> log.debug("Received {} bytes", bytes.length))
                .onErrorResume(e -> {
                    log.warn("Error loading poster from {}: {}", imageUrl, e.getMessage());
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
                                    //log.info(genre.getTmdbId().toString());
                                    g.setTmdbId(genre.getTmdbId());
                                    genres.put(genre.getTmdbId(), genre); // ключ берется из оригинального объекта
                                    return g;
                                })
                                .toList();
                    }
                    return List.<Genre>of();
                })
                .onErrorResume(e -> {
                    log.error("Error accessing Your API", e);
                    return Mono.just(List.of());
                })
                .subscribe(); // Триггерим выполнение запроса
    }

    @Override
    public Mono<List<TmdbMovie>> getRecentlyReleasedMovies(int days) {
        loadGenres();

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
                    log.error("Error fetching recently released movies from TMDb", e);
                    return Mono.just(List.of());
                });
    }


}
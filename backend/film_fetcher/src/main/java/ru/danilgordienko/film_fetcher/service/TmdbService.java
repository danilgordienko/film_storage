package ru.danilgordienko.film_fetcher.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.danilgordienko.film_fetcher.config.RabbitConfig;
import ru.danilgordienko.film_fetcher.model.TmdbMovie;
import ru.danilgordienko.film_fetcher.model.TmdbResponse;
import ru.danilgordienko.film_fetcher.model.Genre;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmdbService {

    @Value("${app.key}")
    private String API_KEY;
    private final WebClient webClient;
    private Map<Long, Genre> genres = new HashMap<>();

    private final RabbitTemplate rabbitTemplate;

    private String getGenreUrl() {
        return "https://api.themoviedb.org/3/genre/movie/list?api_key=" + API_KEY + "&language=ru-RU";
    }

    private String getMovieUrl() {
        return "https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY + "&language=ru-RU";
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

    private void sendMovies(List<TmdbMovie> movies) {
        log.info("Sending movies");
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                movies
        );
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void populateMovies(){
        getPopularMovies()
                .doOnNext(this::sendMovies)
                .subscribe();
    }

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

    public Mono<byte[]> downloadPoster(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return Mono.just(new byte[0]);
        }

        String imageUrl = "https://image.tmdb.org/t/p/w500/" + posterPath;
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

    public Mono<List<TmdbMovie>> getRecentlyReleasedMovies(int days) {
        loadGenres(); // важно для сопоставления жанров

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
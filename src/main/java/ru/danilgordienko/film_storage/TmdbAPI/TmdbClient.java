package ru.danilgordienko.film_storage.TmdbAPI;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import ru.danilgordienko.film_storage.model.Genre;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TmdbClient {

    //api ключ для допступа к tmdb api
    @Value("${app.key}")
    private String API_KEY;
    private final RestTemplate restTemplate;

    //url для получения всех жанров
    private String getGenreUrl() {
        return "https://api.themoviedb.org/3/genre/movie/list?api_key=" + API_KEY + "&language=ru-RU";
    }

    //url для получения популярных фильмов(первые 60)
    private String getMovieUrl() {
        return "https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY + "&language=ru-RU";
    }

    public List<TmdbMovie> getPopularMovies() {
        try {
            //получение списка фильмов и преобразование в TmdbResponse
            ResponseEntity<TmdbResponse> response = restTemplate.getForEntity(getMovieUrl(), TmdbResponse.class);
            TmdbResponse tmdbResponse = response.getBody();
            if (tmdbResponse == null || tmdbResponse.getResults() == null) {
                return List.of();
            }
            return tmdbResponse.getResults();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            //log.error("TMDb API error: {}", e.getMessage());
        } catch (ResourceAccessException e) {
            //log.error("Network error while accessing TMDb API: {}", e.getMessage());
        } catch (RestClientException e) {
            //log.error("Unknown error while accessing TMDb API", e);
        }
        return List.of();
    }

    //класс для хранения ответа с api
    @Getter
    @Setter
    public static class GenreResponse {
        private List<Genre> genres;
    }

    //Загрузка жанров
    public List<Genre> loadGenres() {
        try {
            //получение списка жанров и преобразование в GenreResponse
            ResponseEntity<TmdbClient.GenreResponse> response = restTemplate.getForEntity(getGenreUrl(), TmdbClient.GenreResponse.class);
            TmdbClient.GenreResponse genreResponse = response.getBody();

            if (genreResponse != null) {
                //устанавливаем всем жанрам TmdbId,чтобы в дальнейшем получать названия жанров
                // (в getPopularMovies возвращются те самые TmdbId)
                var genres = Optional.ofNullable(genreResponse.getGenres())
                        .orElse(List.of()).stream().map(genre -> {
                            var g = new Genre();
                            g.setName(genre.getName());
                            g.setTmdbId(genre.getId());
                            return g;
                        }).toList();
                genreResponse.setGenres(genres);
            }
            return genreResponse.getGenres();
        }catch (HttpClientErrorException | HttpServerErrorException e) {
            //log.error("TMDb API error: {}", e.getMessage());
        } catch (ResourceAccessException e) {
            //log.error("Network error while accessing TMDb API: {}", e.getMessage());
        } catch (RestClientException e) {
            //log.error("Unknown error while accessing TMDb API", e);
        }
        return List.of();
    }
}

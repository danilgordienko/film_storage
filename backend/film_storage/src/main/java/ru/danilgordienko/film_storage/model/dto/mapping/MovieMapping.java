package ru.danilgordienko.film_storage.model.dto.mapping;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import ru.danilgordienko.film_storage.model.dto.MoviesDto.*;
import ru.danilgordienko.film_storage.model.dto.PageDto;
import ru.danilgordienko.film_storage.model.entity.Genre;
import ru.danilgordienko.film_storage.model.entity.Movie;
import ru.danilgordienko.film_storage.model.entity.MovieDocument;
import ru.danilgordienko.film_storage.model.entity.Rating;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = RatingMapping.class)
public interface MovieMapping {

    // из JPA Movie
    @Mapping(target = "genres", expression = "java(mapGenres(movie.getGenres()))")
    @Mapping(target = "rating", expression = "java(calculateAverageRating(movie.getRatings()))")
    //@Mapping(target = "poster", expression = "java(getPosterUrl(movie.getId()))")
    @Mapping(target = "id", expression = "java(movie.getId())")
    MovieListDto toMovieListDto(Movie movie);

    @Mapping(target = "genres", expression = "java(mapGenres(movie.getGenres()))")
    @Mapping(target = "rating", expression = "java(calculateAverageRating(movie.getRatings()))")
    @Mapping(target = "id", expression = "java(movie.getId())")
    MovieListCacheDto toMovieListCacheDto(Movie movie);

    // из Elasticsearch MovieDocument
    //@Mapping(target = "posterUrl", expression = "java(getPosterUrl(movie.getId()))")
    @Mapping(target = "rating", source = "averageRating")
    MovieListDto toMovieListDto(MovieDocument movie);

    @Mapping(target = "id", expression = "java(movie.getId())")
    MovieNameDto toMovieNameDto(Movie movie);

    @Mapping(target = "genres", expression = "java(mapGenres(movie.getGenres()))")
    //@Mapping(target = "posterUrl", expression = "java(getPosterUrl(movie.getId()))")
    MovieDetailsDto toMovieDetailsDto(Movie movie);

    @Mapping(target = "genres", expression = "java(mapGenres(movie.getGenres()))")
    @Mapping(target = "averageRating", expression = "java(calculateAverageRating(movie.getRatings()))")
    MovieDocument toMovieDocument(Movie movie);

    MovieDto toMovieDto(Movie movie);

    Movie toMovie(MovieDto movie);

    PageDto<MovieListCacheDto> toPageDto(Page<MovieListCacheDto> movie);

    PageDto<MovieListDto> toMovieListPageDto(Page<MovieDocument> movie);

    PageDto<MovieListCacheDto> toMovieListCachePageDto(Page<Movie> movie);


    //преобразует жанры в список с названиями жанров
    default List<String> mapGenres(Set<Genre> genres) {
        return genres.stream()
                .map(Genre::getName)
                .toList();
    }

    // url для получения постера к фильмы из базы данных
    default String getPosterUrl(Long id) {
        return "http://localhost:8080/api/movies/" + id + "/poster";
    }

    //считает средний рейтинг
    default double calculateAverageRating(List<Rating> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            return 0;
        }
        double avg = ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0);
        return Math.round(avg * 10.0) / 10.0;
    }
}

package ru.danilgordienko.film_storage.DTO.mapping;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.danilgordienko.film_storage.DTO.MovieDetailsDto;
import ru.danilgordienko.film_storage.DTO.MovieListDto;
import ru.danilgordienko.film_storage.model.Genre;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.model.MovieDocument;
import ru.danilgordienko.film_storage.model.Rating;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = RatingMapping.class)
public interface MovieMapping {

    // из JPA Movie
    @Mapping(target = "genres", expression = "java(mapGenres(movie.getGenres()))")
    @Mapping(target = "rating", expression = "java(calculateAverageRating(movie.getRatings()))")
    @Mapping(target = "posterUrl", expression = "java(getPosterUrl(movie.getId()))")
    MovieListDto toMovieListDto(Movie movie);

    // из Elasticsearch MovieDocument
    @Mapping(target = "posterUrl", expression = "java(getPosterUrl(movie.getId()))")
    @Mapping(target = "rating", source = "averageRating")
    MovieListDto toMovieListDto(MovieDocument movie);

    @Mapping(target = "genres", expression = "java(mapGenres(movie.getGenres()))")
    @Mapping(target = "posterUrl", expression = "java(getPosterUrl(movie.getId()))")
    MovieDetailsDto toMovieDetailsDto(Movie movie);

    @Mapping(target = "genres", expression = "java(mapGenres(movie.getGenres()))")
    @Mapping(target = "averageRating", expression = "java(calculateAverageRating(movie.getRatings()))")
    MovieDocument toMovieDocument(Movie movie);



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
    default int calculateAverageRating(List<Rating> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            return 0;
        }
        double avg = ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0);
        return (int) Math.round(avg);
    }
}

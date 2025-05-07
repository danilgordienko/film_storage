package ru.danilgordienko.film_storage.DTO.mapping;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.danilgordienko.film_storage.DTO.MovieDetailsDto;
import ru.danilgordienko.film_storage.DTO.MovieListDto;
import ru.danilgordienko.film_storage.model.Genre;
import ru.danilgordienko.film_storage.model.Movie;
import ru.danilgordienko.film_storage.model.Rating;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = RatingMapping.class)
public interface MovieMapping {


    @Mapping(target = "genres", expression = "java(mapGenres(movie.getGenres()))")
    @Mapping(target = "rating", expression = "java(calculateAverageRating(movie.getRatings()))")
    MovieListDto toMovieListDto(Movie movie);

    @Mapping(target = "genres", expression = "java(mapGenres(movie.getGenres()))")
    MovieDetailsDto toMovieDetailsDto(Movie movie);

    //преобразует жанры в список с названиями жанров
    default List<String> mapGenres(Set<Genre> genres) {
        return genres.stream()
                .map(Genre::getName)
                .toList();
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

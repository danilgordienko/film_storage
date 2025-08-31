package ru.danilgordienko.film_storage.model.dto;

import lombok.Data;
import ru.danilgordienko.film_storage.model.dto.MoviesDto.MovieListDto;

@Data
public class FavoriteDto {
    MovieListDto movie;
}

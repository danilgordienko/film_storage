package ru.danilgordienko.film_storage.DTO;

import lombok.Data;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListDto;

@Data
public class FavoriteDto {
    MovieListDto movie;
}

package ru.danilgordienko.film_storage.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListCacheDto;
import ru.danilgordienko.film_storage.DTO.MoviesDto.MovieListDto;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<MovieListCacheDto> content;
    private int number;
    private long totalElements;
}

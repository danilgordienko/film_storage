package ru.danilgordienko.film_fetcher.model.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.danilgordienko.film_fetcher.model.dto.response.MovieDto;
import ru.danilgordienko.film_fetcher.model.dto.response.TmdbMovieResponse;
import ru.danilgordienko.film_fetcher.model.entity.RetryableTask;
import ru.danilgordienko.film_fetcher.model.enums.RetryableTaskType;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RetryableTaskMapping {

    @Mapping(source = "movies", target = "payload", qualifiedByName = "tmdbMoviesToJson")
    RetryableTask toSendMovieRetryableTask(List<MovieDto> movies, RetryableTaskType type);

    default List<TmdbMovieResponse> jsonToTmdbMovies(RetryableTask retryableTask){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(
                    retryableTask.getPayload(),
                    new TypeReference<List<TmdbMovieResponse>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Named("tmdbMoviesToJson")
    default String tmdbMoviesToJson(List<MovieDto> movies) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(movies);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}

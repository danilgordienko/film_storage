package ru.danilgordienko.film_fetcher.model.dto.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {
    @Getter(onMethod_ = {@JsonGetter("tmdbId")})
    @Setter(onMethod_ = {@JsonSetter("id")})
    private Long tmdbId;

    private String name;
}

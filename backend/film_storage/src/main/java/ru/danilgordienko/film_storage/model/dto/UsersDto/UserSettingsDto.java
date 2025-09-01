package ru.danilgordienko.film_storage.model.dto.UsersDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.danilgordienko.film_storage.model.enums.RatingVisibility;

@Getter
@Setter
@AllArgsConstructor
public class UserSettingsDto {
    @JsonProperty(value = "rating_visibility")
    private RatingVisibility ratingVisibility;
}

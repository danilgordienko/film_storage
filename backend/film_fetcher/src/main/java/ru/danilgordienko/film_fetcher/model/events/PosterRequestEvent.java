package ru.danilgordienko.film_fetcher.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class PosterRequestEvent extends ApplicationEvent {

    @Getter
    private String posterPath;

    public PosterRequestEvent(Object source, String posterPath) {
        super(source);
    }
}

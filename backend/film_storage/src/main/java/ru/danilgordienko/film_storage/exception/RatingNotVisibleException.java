package ru.danilgordienko.film_storage.exception;

public class RatingNotVisibleException extends RuntimeException {
    public RatingNotVisibleException(String message) {
        super(message);
    }
}

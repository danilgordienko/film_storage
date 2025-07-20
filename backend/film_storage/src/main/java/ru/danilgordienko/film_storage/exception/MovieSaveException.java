package ru.danilgordienko.film_storage.exception;

public class MovieSaveException extends RuntimeException {
    public MovieSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}

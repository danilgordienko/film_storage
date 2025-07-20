package ru.danilgordienko.film_storage.exception;

public class ElasticsearchConnectionException extends RuntimeException {
    public ElasticsearchConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

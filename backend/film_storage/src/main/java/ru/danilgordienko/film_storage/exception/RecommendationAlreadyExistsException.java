package ru.danilgordienko.film_storage.exception;

public class RecommendationAlreadyExistsException extends RuntimeException {
    public RecommendationAlreadyExistsException(String message) {
        super(message);
    }
}
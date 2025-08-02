package ru.danilgordienko.film_storage.exception;

public class TokenNotFoundException extends TokenException {
    public TokenNotFoundException(String message) {
        super(message);
    }
}

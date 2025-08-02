package ru.danilgordienko.film_storage.exception;

public class TokenException extends RuntimeException {
    public TokenException(String message){
        super(message);
    }
}

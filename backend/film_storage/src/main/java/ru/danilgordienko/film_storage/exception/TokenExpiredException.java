package ru.danilgordienko.film_storage.exception;

public class TokenExpiredException extends TokenException {
    public TokenExpiredException(String message){
        super(message);
    }
}

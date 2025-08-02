package ru.danilgordienko.film_storage.exception;

public class TokenAlreadyRevokedException extends TokenException{
    public TokenAlreadyRevokedException(String message){
        super(message);
    }
}

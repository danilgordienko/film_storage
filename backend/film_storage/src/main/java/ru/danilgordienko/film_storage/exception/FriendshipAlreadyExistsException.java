package ru.danilgordienko.film_storage.exception;

public class FriendshipAlreadyExistsException extends RuntimeException {
    public FriendshipAlreadyExistsException(String message) {
        super(message);
    }
}

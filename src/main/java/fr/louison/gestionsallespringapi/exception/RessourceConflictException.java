package fr.louison.gestionsallespringapi.exception;

public class RessourceConflictException extends RuntimeException {
    public RessourceConflictException(String message) {
        super(message);
    }
}

package net.cyweb.exception;

public class EmailNotExistException extends RuntimeException {
    public EmailNotExistException(String message){
        super(message);
    }
}

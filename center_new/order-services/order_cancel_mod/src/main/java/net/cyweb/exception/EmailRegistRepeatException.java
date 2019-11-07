package net.cyweb.exception;

public class EmailRegistRepeatException extends RuntimeException {
    public EmailRegistRepeatException(String message){
        super(message);
    }
}

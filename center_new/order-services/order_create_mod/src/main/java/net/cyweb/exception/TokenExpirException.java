package net.cyweb.exception;

public class TokenExpirException extends RuntimeException {
    public TokenExpirException(String message){
        super(message);
    }
}

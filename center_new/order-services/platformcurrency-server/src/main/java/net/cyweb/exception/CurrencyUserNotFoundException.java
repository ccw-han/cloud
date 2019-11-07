package net.cyweb.exception;

public class CurrencyUserNotFoundException extends RuntimeException {
    public CurrencyUserNotFoundException(String message){
        super(message);
    }
}

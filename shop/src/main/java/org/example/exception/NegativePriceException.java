package org.example.exception;

public class NegativePriceException extends RuntimeException {

    public NegativePriceException(String message) {
        super(message);
    }
}

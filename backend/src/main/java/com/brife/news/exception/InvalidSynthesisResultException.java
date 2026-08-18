package com.brife.news.exception;

public class InvalidSynthesisResultException extends RuntimeException {
    public InvalidSynthesisResultException(String message) {
        super(message);
    }

    public InvalidSynthesisResultException(String message, Throwable cause) {
        super(message, cause);
    }
}

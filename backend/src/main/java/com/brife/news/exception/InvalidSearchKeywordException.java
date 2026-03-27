package com.brife.news.exception;

public class InvalidSearchKeywordException extends RuntimeException{
    public InvalidSearchKeywordException(String message) {
        super(message);
    }
}
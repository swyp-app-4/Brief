package com.brife.exception;

import com.brife.news.exception.InvalidSearchKeywordException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidSearchKeywordException.class)
    public ResponseEntity<String> handle( InvalidSearchKeywordException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
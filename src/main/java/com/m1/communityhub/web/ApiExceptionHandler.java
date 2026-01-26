package com.m1.communityhub.web;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
            .body(Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", ex.getStatus().value(),
                "error", ex.getMessage()
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage()
            ));
    }
}

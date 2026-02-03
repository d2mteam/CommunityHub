package com.m1.communityhub.web;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolationException;
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
        String message = ex.getBindingResult().getAllErrors().isEmpty()
            ? "Validation failed"
            : ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", message
            ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
            .collect(Collectors.joining(", "));
        if (message.isBlank()) {
            message = "Validation failed";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", message
            ));
    }
}

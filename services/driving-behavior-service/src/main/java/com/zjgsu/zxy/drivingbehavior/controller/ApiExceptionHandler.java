package com.zjgsu.zxy.drivingbehavior.controller;

import com.zjgsu.zxy.drivingbehavior.exception.ResourceNotFoundException;
import com.zjgsu.zxy.drivingbehavior.exception.ServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleUnavailable(
            ServiceUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", exception.getMessage()));
    }
}

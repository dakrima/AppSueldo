package com.appsueldo.exception;

import java.util.Map;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", "La solicitud contiene datos invalidos."));
    }

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<Map<String, String>> handleConflict(ConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class, BadCredentialsException.class})
    ResponseEntity<Map<String, String>> handleUnauthorized(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", exception.getMessage()));
    }
}

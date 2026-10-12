package com.crypton.controller.api;

import com.crypton.exception.AccessDeniedException;
import com.crypton.exception.InsufficientBalanceException;
import com.crypton.exception.InvalidAmountException;
import com.crypton.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Friendly, consistent JSON errors for the React frontend.
 * Applies only to /api controllers; the original pages keep GlobalExceptionHandler.
 * Unexpected errors are logged on the server but never shown to the user.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.crypton.controller.api")
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "We couldn't find that. It may have been deleted.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> forbidden(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "You don't have access to this.");
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> insufficient(InsufficientBalanceException ex) {
        return error(HttpStatus.BAD_REQUEST,
                "There isn't enough in this wallet for that withdrawal. Try a smaller amount.");
    }

    @ExceptionHandler({InvalidAmountException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> badInput(RuntimeException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ResponseEntity<Map<String, Object>> response =
                error(HttpStatus.BAD_REQUEST, "Please check the highlighted fields.");
        response.getBody().put("errors", errors);
        return response;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> unreadable(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, "That request wasn't in the expected format.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> unexpected(Exception ex) {
        log.error("Unexpected API error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on our side. Please try again.");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}

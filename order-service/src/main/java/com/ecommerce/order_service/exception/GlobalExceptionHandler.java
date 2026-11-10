package com.ecommerce.order_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> validationErrors) {

        ApiErrorResponse response = new ApiErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            validationErrors
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        return buildError(
            status,
            ex.getReason() != null ? ex.getReason() : status.getReasonPhrase(),
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiErrorResponse> handleServiceClientError(
            HttpClientErrorException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        return buildError(
            status,
            "A downstream service rejected the request: " + ex.getStatusText(),
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return buildError(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            request.getRequestURI(),
            Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                error -> error.getField(),
                error -> error.getDefaultMessage() != null
                    ? error.getDefaultMessage()
                    : "Invalid value",
                (first, second) -> first
            ));

        return buildError(
            HttpStatus.BAD_REQUEST,
            "Request validation failed",
            request.getRequestURI(),
            errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        return buildError(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            request.getRequestURI(),
            Map.of()
        );
    }
}

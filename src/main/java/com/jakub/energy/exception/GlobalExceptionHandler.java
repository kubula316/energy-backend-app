package com.jakub.energy.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(HandlerMethodValidationException e) {
        log.warn("Validation error: {}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", "Bad Request");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Invalid argument: ", e);
        return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), "Bad Request");
    }

    @ExceptionHandler(CarbonIntensityApiException.class)
    public ResponseEntity<Map<String, Object>> handleCarbonIntensityApiException(CarbonIntensityApiException e) {
        log.error("External API error: ", e);
        return createErrorResponse(HttpStatus.BAD_GATEWAY, e.getMessage(), "Bad Gateway");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime error: ", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing your request", "Internal Server Error");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Unexpected error: ", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "Internal Server Error");
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message, String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);

        return new ResponseEntity<>(response, status);
    }
}

package com.example.dynamicform.controller;

import com.example.dynamicform.dto.ValidationError;
import com.example.dynamicform.exception.DynamicValidationException;
import com.example.dynamicform.exception.FormNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler for REST controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FormNotFoundException.class)
    public ResponseEntity<?> handleFormNotFound(FormNotFoundException ex) {
        logger.error("Form not found: {}", ex.getFormName(), ex);
        ValidationError error = ValidationError.builder()
                .fieldPath("form")
                .message("Form not found: " + ex.getFormName())
                .validationType("system")
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of(error));
    }

    @ExceptionHandler(DynamicValidationException.class)
    public ResponseEntity<?> handleValidationException(DynamicValidationException ex) {
        logger.warn("Validation failed: {}", ex.getMessage());
        if (ex.getErrors() != null && !ex.getErrors().isEmpty()) {
            return ResponseEntity.badRequest().body(ex.getErrors());
        } else {
            ValidationError error = ValidationError.builder()
                    .fieldPath(ex.getFieldName() != null ? ex.getFieldName() : "general")
                    .message(ex.getMessage())
                    .validationType(ex.getValidationType() != null ? ex.getValidationType() : "general")
                    .build();
            return ResponseEntity.badRequest().body(List.of(error));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        logger.error("Unexpected error", ex);
        ValidationError error = ValidationError.builder()
                .fieldPath("system")
                .message("An unexpected error occurred")
                .validationType("system")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(error));
    }
}

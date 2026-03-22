package com.example.dynamicform.platform.exception;

import com.example.dynamicform.platform.dto.ValidationError;
import lombok.Getter;

import java.util.List;

/**
 * Exception thrown when form validation fails against dynamic rules.
 */
public class DynamicValidationException extends RuntimeException {
    @Getter
    private final String fieldName;
    @Getter
    private final String validationType;
    @Getter
    private final List<ValidationError> errors;

    public DynamicValidationException(String message, String fieldName, String validationType) {
        super(message);
        this.fieldName = fieldName;
        this.validationType = validationType;
        this.errors = null;
    }

    public DynamicValidationException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
        this.validationType = null;
        this.errors = null;
    }

    public DynamicValidationException(String message, List<ValidationError> errors) {
        super(message);
        this.errors = errors;
        this.fieldName = null;
        this.validationType = null;
    }
}

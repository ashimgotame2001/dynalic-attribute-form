package com.example.dynamicform.platform.validation;

import java.util.Map;

/**
 * Represents a validation error.
 */
public class ValidationError {

    private String fieldPath;
    private String errorCode;
    private String message;
    private String validationType;
    private Map<String, Object> context;

    public ValidationError() {}

    public ValidationError(String fieldPath, String errorCode, String message) {
        this.fieldPath = fieldPath;
        this.errorCode = errorCode;
        this.message = message;
        this.validationType = "constraint";
    }

    public ValidationError(String fieldPath, String errorCode, String message, String validationType) {
        this.fieldPath = fieldPath;
        this.errorCode = errorCode;
        this.message = message;
        this.validationType = validationType;
    }

    // Getters and setters
    public String getFieldPath() { return fieldPath; }
    public void setFieldPath(String fieldPath) { this.fieldPath = fieldPath; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getValidationType() { return validationType; }
    public void setValidationType(String validationType) { this.validationType = validationType; }

    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }

    @Override
    public String toString() {
        return String.format("ValidationError{field='%s', code='%s', message='%s'}",
                           fieldPath, errorCode, message);
    }
}

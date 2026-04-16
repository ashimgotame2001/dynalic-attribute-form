package com.example.dynamicform.platform.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a validation error for a specific field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationError {
    /**
     * Dot-separated path to the field (e.g., "individual.address.postalCode").
     */
    private String fieldPath;

    /**
     * Human-readable error message.
     */
    private String message;

    /**
     * Type of validation that failed.
     */
    private String validationType;

    /**
     * The invalid value (optional, for debugging).
     */
    private Object invalidValue;
}

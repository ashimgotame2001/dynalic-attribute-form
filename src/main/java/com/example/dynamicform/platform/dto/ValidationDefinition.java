package com.example.dynamicform.platform.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Internal representation of a validation rule.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationDefinition {
    /**
     * Type of validation.
     */
    private ValidationType type;

    /**
     * Parameters for the validation.
     */
    private Map<String, Object> parameters;

    /**
     * Custom error message (if provided).
     */
    private String message;

    /**
     * Construct from raw validation map.
     */
    public static ValidationDefinition fromRaw(Map<String, Object> raw) {
        String type = raw.keySet().iterator().next();
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) raw.get(type);
        return ValidationDefinition.builder()
                .type(ValidationType.fromString(type))
                .parameters(params)
                .message((String) params.get("message"))
                .build();
    }
}

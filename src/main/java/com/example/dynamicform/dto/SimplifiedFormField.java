package com.example.dynamicform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Simplified field definition as sent by the client.
 * Matches the user's requested format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimplifiedFormField {
    /**
     * Data type/model name (e.g., "String", "LocalDate", "AlphaTwoCode").
     * This will be used to derive the attribute name.
     */
    private String modelName;

    /**
     * Whether the field is visible in the UI. Defaults to true if null.
     */
    private Boolean visible;

    /**
     * Short display label.
     */
    private String shortLabel;

    /**
     * Longer description/placeholder.
     */
    private String longLabel;

    /**
     * For reference types (nested objects), the nested fields.
     */
    private List<SimplifiedFormField> nestedFields;

    /**
     * For collection types, whether this is a list. Defaults to false if null.
     */
    private Boolean isCollection;

    /**
     * Validation rules in simplified format.
     * Map where key is validation type and value is the validation config object.
     * Example: { "required": { "value": true, "message": "..." } }
     */
    private List<Map<String, ValidationParam>> validations;

    /**
     * Represents a validation parameter object.
     * Matches the nested structure: { "value": true, "message": "..." }
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationParam {
        private Boolean value;
        private String pattern;
        private String message;
    }
}

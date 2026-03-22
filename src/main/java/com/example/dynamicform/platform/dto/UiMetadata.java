package com.example.dynamicform.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UI-specific metadata for a field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UiMetadata {
    /**
     * Human-readable label for the field.
     */
    private String label;

    /**
     * Placeholder text for input fields.
     */
    private String placeholder;

    /**
     * Help text to display below the field.
     */
    private String helpText;

    /**
     * Hint text shown as tooltip.
     */
    private String tooltip;

    /**
     * Input component type (text, select, date, etc.).
     * Can be inferred from dataType but can be overridden here.
     */
    private String componentType;

    /**
     * CSS class name(s) for the field.
     */
    private String cssClass;

    /**
     * Whether the field is required (derived from validations but can be overridden).
     */
    private Boolean required;

    /**
     * Additional custom properties.
     */
    private java.util.Map<String, Object> properties;
}

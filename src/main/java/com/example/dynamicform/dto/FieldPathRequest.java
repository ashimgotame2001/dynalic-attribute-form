package com.example.dynamicform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating or updating a form configuration using field paths.
 * The system introspects the target DTO to build the complete domain model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldPathRequest {
    /**
     * Name of the form (must be unique).
     */
    private String formName;

    /**
     * List of field paths in dot notation, e.g., "user.secret", "individual.firstName".
     * Only the leaf fields that should be included in the form need to be specified.
     * The system will auto-generate the intermediate nested structure by introspecting
     * the target DTO class.
     */
    private List<String> fields;

    /**
     * Human-readable description.
     */
    private String description;

    /**
     * Fully qualified class name of the target DTO this form maps to.
     * This DTO will be introspected to determine field types, collections, and nested objects.
     */
    private String targetDtoClassName;
}

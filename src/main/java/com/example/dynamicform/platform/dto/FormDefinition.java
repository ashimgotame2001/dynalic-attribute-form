package com.example.dynamicform.platform.dto;

import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Internal representation of a form definition used by the DynamicFormEngine.
 * This is derived from RawFormMetadata after interpretation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FormDefinition {
    /**
     * Name of the form (root model name).
     */
    private String formName;

    /**
     * List of top-level fields in the form.
     */
    private List<FieldDefinition> fields;

    /**
     * Version of the form definition (for caching).
     */
    private int version;

    /**
     * Original raw metadata (optional, for reference).
     */
    private RawFormMetadata rawMetadata;

    /**
     * Human-readable description of the form.
     */
    private String description;

    /**
     * Fully qualified class name of the target DTO to which submitted data should be mapped.
     */
    private String targetClassName;

    /**
     * Module name.
     */
    private String moduleName;

    /**
     * Artifact name.
     */
    private String artifactName;
}

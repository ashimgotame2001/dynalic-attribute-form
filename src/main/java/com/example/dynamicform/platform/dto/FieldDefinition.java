package com.example.dynamicform.platform.dto;

import com.example.dynamicform.platform.dto.metadata.RawDomainAttribute;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Definition of a field in the form.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FieldDefinition {
    /**
     * Field name (attribute name in the DTO).
     */
    private String fieldName;

    /**
     * Display label for UI.
     */
    private String label;

    /**
     * Short label for UI.
     */
    private String shortLabel;
    private Map<String, String> shortLabelI18n;

    /**
     * Long label for UI.
     */
    private String longLabel;
    private Map<String, String> longLabelI18n;

    /**
     * Data type of the field (inferred from modelName).
     */
    private String dataType;

    /**
     * Whether this field is a nested object (reference = true).
     */
    private boolean isNestedObject;

    /**
     * Whether this field is a collection (List).
     */
    private boolean isCollection;

    /**
     * For nested objects, the list of child fields.
     */
    private List<FieldDefinition> nestedFields;

    /**
     * For collections, the element type (e.g., the modelName of the items).
     */
    private String elementType;

    /**
     * Whether the field should be visible in the UI.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean visible;

    /**
     * Validation rules for this field.
     */
    private List<ValidationDefinition> validations;

    /**
     * UI metadata like placeholder, help text, etc.
     */
    private UiMetadata uiMetadata;

    /**
     * The original raw attribute for reference.
     */
    private RawDomainAttribute rawAttribute;
}

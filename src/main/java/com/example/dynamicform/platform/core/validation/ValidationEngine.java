package com.example.dynamicform.platform.core.validation;

import com.example.dynamicform.platform.api.dto.FormDefinition;
import com.example.dynamicform.platform.api.dto.ValidationError;

import java.util.List;

/**
 * Service interface for performing dynamic validation of form data against metadata.
 */
public interface ValidationEngine {

    /**
     * Validates the provided data against the form definition.
     *
     * @param formDefinition the form definition containing validation rules
     * @param data the data to validate (nested map structure)
     * @return list of validation errors; empty list if valid
     */
    List<ValidationError> validate(FormDefinition formDefinition, java.util.Map<String, Object> data);

    /**
     * Validates a single field value against its validation rules.
     *
     * @param fieldName the name of the field (for error messages)
     * @param value the value to validate
     * @param validations list of validation definitions
     * @return list of validation errors for this field
     */
    List<ValidationError> validateField(String fieldName, Object value, List<com.example.dynamicform.platform.api.dto.ValidationDefinition> validations);
}

package com.example.dynamicform.platform.engine;

import com.example.dynamicform.platform.dto.FormDefinition;
import com.example.dynamicform.platform.dto.ValidationError;
import com.example.dynamicform.platform.exception.DynamicValidationException;
import com.example.dynamicform.platform.exception.FormNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * Core engine that manages dynamic form generation, validation, and mapping.
 */
public interface DynamicFormEngine {

    /**
     * Retrieves the form definition for the given form name.
     * Uses cache if available.
     *
     * @param formName name of the form
     * @return FormDefinition
     * @throws FormNotFoundException if form configuration does not exist
     */
    FormDefinition getFormDefinition(String formName) throws FormNotFoundException;

    /**
     * Retrieves a specific version of the form definition.
     *
     * @param formName form name
     * @param version version number
     * @return FormDefinition
     * @throws FormNotFoundException if not found
     */
    FormDefinition getFormDefinition(String formName, int version) throws FormNotFoundException;

    /**
     * Validates submitted form data against the form definition.
     *
     * @param formName name of the form
     * @param data raw data to validate (nested map)
     * @return list of validation errors; empty if valid
     */
    List<ValidationError> validate(String formName, Map<String, Object> data);

    /**
     * Validates and maps submitted data to the target DTO class.
     *
     * @param formName name of the form
     * @param data raw data to validate and map
     * @param <T> expected DTO type
     * @return mapped DTO instance
     * @throws DynamicValidationException if validation fails
     * @throws FormNotFoundException if form not found
     */
    <T> T validateAndMap(String formName, Map<String, Object> data, Class<T> targetClass) throws DynamicValidationException, FormNotFoundException;

    /**
     * Validates and maps submitted data to the target DTO class defined in the form configuration.
     *
     * @param formName name of the form
     * @param data raw data
     * @return mapped DTO instance
     * @throws DynamicValidationException if validation fails
     * @throws FormNotFoundException if form not found or target class not configured
     */
    Object validateAndMap(String formName, Map<String, Object> data) throws DynamicValidationException, FormNotFoundException;

    /**
     * Checks if a form exists.
     *
     * @param formName form name
     * @return true if exists
     */
    boolean formExists(String formName);
}

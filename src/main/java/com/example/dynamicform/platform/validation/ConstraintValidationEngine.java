package com.example.dynamicform.platform.validation;

import java.util.List;
import java.util.Map;

/**
 * Interface for constraint and validation engine.
 * Enforces runtime rules on data from existing domain models.
 */
public interface ConstraintValidationEngine {

    /**
     * Validate data against domain model constraints.
     *
     * @param entityType the type of entity being validated
     * @param entityId the ID of the entity (null for new entities)
     * @param data the data to validate
     * @return list of validation errors (empty if valid)
     */
    List<ValidationError> validate(String entityType, String entityId, Map<String, Object> data);

    /**
     * Validate relationship constraints.
     *
     * @param relationshipName the relationship being validated
     * @param sourceEntityId the source entity ID
     * @param targetEntityId the target entity ID
     * @return list of validation errors (empty if valid)
     */
    List<ValidationError> validateRelationship(String relationshipName, String sourceEntityId, String targetEntityId);

    /**
     * Validate data against a specific schema version.
     *
     * @param entityType the entity type
     * @param schemaVersion the schema version to validate against
     * @param data the data to validate
     * @return list of validation errors (empty if valid)
     */
    List<ValidationError> validateAgainstSchema(String entityType, int schemaVersion, Map<String, Object> data);

    /**
     * Add a custom validation rule.
     *
     * @param rule the validation rule to add
     */
    void addValidationRule(ValidationRule rule);

    /**
     * Remove a validation rule.
     *
     * @param ruleName the name of the rule to remove
     */
    void removeValidationRule(String ruleName);

    /**
     * Get all validation rules for an entity type.
     *
     * @param entityType the entity type
     * @return list of validation rules
     */
    List<ValidationRule> getValidationRules(String entityType);

    /**
     * Enable/disable strict validation mode.
     *
     * @param strict if true, all validation errors are fatal
     */
    void setStrictMode(boolean strict);

    /**
     * Check if strict mode is enabled.
     *
     * @return true if strict mode is enabled
     */
    boolean isStrictMode();
}

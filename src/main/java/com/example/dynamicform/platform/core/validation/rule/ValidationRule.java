package com.example.dynamicform.platform.core.validation.rule;

import com.example.dynamicform.platform.core.validation.ValidationError;

import java.util.List;
import java.util.Map;

/**
 * Interface for validation rules that can be applied to domain model data.
 */
public interface ValidationRule {

    /**
     * Get the name of this validation rule.
     *
     * @return the rule name
     */
    String getName();

    /**
     * Get the entity types this rule applies to.
     *
     * @return list of entity type names
     */
    List<String> getApplicableEntityTypes();

    /**
     * Validate data against this rule.
     *
     * @param entityType the entity type being validated
     * @param entityId the entity ID (null for new entities)
     * @param data the data to validate
     * @return list of validation errors (empty if valid)
     */
    List<ValidationError> validate(String entityType, String entityId, Map<String, Object> data);

    /**
     * Get the priority of this rule (lower numbers execute first).
     *
     * @return the priority
     */
    default int getPriority() {
        return 100;
    }

    /**
     * Check if this rule should be applied.
     *
     * @param entityType the entity type
     * @param entityId the entity ID
     * @param data the data
     * @return true if the rule should be applied
     */
    default boolean shouldApply(String entityType, String entityId, Map<String, Object> data) {
        return getApplicableEntityTypes().contains(entityType) ||
               getApplicableEntityTypes().contains("*");
    }
}

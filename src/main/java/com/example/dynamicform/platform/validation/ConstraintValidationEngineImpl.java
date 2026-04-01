package com.example.dynamicform.platform.validation;

import com.example.dynamicform.product.dto.ValidationRuleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Constraint and Validation Engine that uses validations provided by products.
 * Products define and register their own validation rules and constraints.
 */
@Service
public class ConstraintValidationEngineImpl implements ConstraintValidationEngine {

    private static final Logger logger = LoggerFactory.getLogger(ConstraintValidationEngineImpl.class);

    // Registry for product-provided validation rules
    private final Map<String, List<ValidationRule>> productValidationRules = new ConcurrentHashMap<>();

    // Product-provided validation configurations
    private final Map<String, List<ValidationRuleDTO>> productValidationConfigs = new ConcurrentHashMap<>();

    private boolean strictMode = false;

    @Override
    public List<ValidationError> validate(String entityType, String entityId, Map<String, Object> data) {
        if (data == null) {
            return Collections.singletonList(
                new ValidationError("root", "NULL_DATA", "Data cannot be null", "constraint")
            );
        }

        List<ValidationError> errors = new ArrayList<>();

        // Apply product-provided validation rules
        List<ValidationRule> rules = productValidationRules.getOrDefault(entityType, Collections.emptyList());
        for (ValidationRule rule : rules) {
            if (rule.shouldApply(entityType, entityId, data)) {
                try {
                    List<ValidationError> ruleErrors = rule.validate(entityType, entityId, data);
                    errors.addAll(ruleErrors);
                } catch (Exception e) {
                    logger.error("Error executing product validation rule {}: {}", rule.getName(), e.getMessage());
                    if (strictMode) {
                        errors.add(new ValidationError("system", "VALIDATION_ERROR",
                            "Product validation rule execution failed: " + rule.getName(), "system"));
                    }
                }
            }
        }

        // Apply product-provided validation configurations
        List<ValidationRuleDTO> configs = productValidationConfigs.getOrDefault(entityType, Collections.emptyList());
        for (ValidationRuleDTO config : configs) {
            try {
                List<ValidationError> configErrors = validateAgainstConfig(config, entityType, entityId, data);
                errors.addAll(configErrors);
            } catch (Exception e) {
                logger.error("Error executing product validation config {}: {}", config.getRuleName(), e.getMessage());
                if (strictMode) {
                    errors.add(new ValidationError("system", "CONFIG_VALIDATION_ERROR",
                        "Product validation config execution failed: " + config.getRuleName(), "system"));
                }
            }
        }

        logger.debug("Validated entity {}/{} with {} errors using product validations", entityType, entityId, errors.size());
        return errors;
    }

    @Override
    public List<ValidationError> validateRelationship(String relationshipName, String sourceEntityId, String targetEntityId) {
        List<ValidationError> errors = new ArrayList<>();

        // Basic relationship validation - products can extend this
        if (relationshipName == null || relationshipName.trim().isEmpty()) {
            errors.add(new ValidationError("relationshipName", "REQUIRED",
                "Relationship name is required", "relationship"));
        }

        if (sourceEntityId == null || sourceEntityId.trim().isEmpty()) {
            errors.add(new ValidationError("sourceEntityId", "REQUIRED",
                "Source entity ID is required", "relationship"));
        }

        if (targetEntityId == null || targetEntityId.trim().isEmpty()) {
            errors.add(new ValidationError("targetEntityId", "REQUIRED",
                "Target entity ID is required", "relationship"));
        }

        // Check for self-references
        if (sourceEntityId != null && sourceEntityId.equals(targetEntityId)) {
            errors.add(new ValidationError("relationship", "SELF_REFERENCE",
                "Entity cannot reference itself", "relationship"));
        }

        logger.debug("Validated relationship {}: {} -> {} with {} errors",
                    relationshipName, sourceEntityId, targetEntityId, errors.size());

        return errors;
    }

    @Override
    public List<ValidationError> validateAgainstSchema(String entityType, int schemaVersion, Map<String, Object> data) {
        // This would use product-provided schema validations
        List<ValidationError> errors = new ArrayList<>();
        // Implementation would validate against product-defined schemas
        logger.debug("Schema validation not yet implemented - products should provide schema validations");
        return errors;
    }

    @Override
    public void addValidationRule(ValidationRule rule) {
        // Products can register their validation rules
        for (String entityType : rule.getApplicableEntityTypes()) {
            productValidationRules.computeIfAbsent(entityType, k -> new ArrayList<>()).add(rule);
            // Sort by priority
            productValidationRules.get(entityType).sort(Comparator.comparingInt(ValidationRule::getPriority));
        }
        logger.info("Product registered validation rule: {}", rule.getName());
    }

    @Override
    public void removeValidationRule(String ruleName) {
        for (List<ValidationRule> rules : productValidationRules.values()) {
            rules.removeIf(rule -> rule.getName().equals(ruleName));
        }
        logger.info("Removed product validation rule: {}", ruleName);
    }

    @Override
    public List<ValidationRule> getValidationRules(String entityType) {
        return new ArrayList<>(productValidationRules.getOrDefault(entityType, Collections.emptyList()));
    }

    @Override
    public void setStrictMode(boolean strict) {
        this.strictMode = strict;
        logger.info("Strict validation mode set to: {}", strict);
    }

    @Override
    public boolean isStrictMode() {
        return strictMode;
    }

    /**
     * Allow products to register validation configurations.
     */
    public void registerValidationConfig(String entityType, ValidationRuleDTO config) {
        productValidationConfigs.computeIfAbsent(entityType, k -> new ArrayList<>()).add(config);
        logger.info("Product registered validation config: {} for entity: {}", config.getRuleName(), entityType);
    }

    /**
     * Allow products to register multiple validation configurations.
     */
    public void registerValidationConfigs(String entityType, List<ValidationRuleDTO> configs) {
        productValidationConfigs.computeIfAbsent(entityType, k -> new ArrayList<>()).addAll(configs);
        logger.info("Product registered {} validation configs for entity: {}", configs.size(), entityType);
    }

    /**
     * Remove validation configurations for an entity type.
     */
    public void removeValidationConfigs(String entityType) {
        productValidationConfigs.remove(entityType);
        logger.info("Removed all validation configs for entity: {}", entityType);
    }

    /**
     * Get all validation configurations for an entity type.
     */
    public List<ValidationRuleDTO> getValidationConfigs(String entityType) {
        return new ArrayList<>(productValidationConfigs.getOrDefault(entityType, Collections.emptyList()));
    }

    // Helper method to validate against configuration
    private List<ValidationError> validateAgainstConfig(ValidationRuleDTO config, String entityType, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        String fieldName = config.getFieldName();
        String ruleType = config.getRuleType();
        Object fieldValue = data.get(fieldName);

        // Apply validation based on rule type
        switch (ruleType.toLowerCase()) {
            case "required":
                if (fieldValue == null || (fieldValue instanceof String && ((String) fieldValue).trim().isEmpty())) {
                    errors.add(new ValidationError(fieldName, "REQUIRED",
                        config.getMessage() != null ? config.getMessage() : fieldName + " is required", "constraint"));
                }
                break;

            case "minlength":
                if (fieldValue instanceof String && config.getMinValue() != null) {
                    String strValue = (String) fieldValue;
                    if (strValue.length() < config.getMinValue().intValue()) {
                        errors.add(new ValidationError(fieldName, "TOO_SHORT",
                            config.getMessage() != null ? config.getMessage() :
                            fieldName + " must be at least " + config.getMinValue() + " characters", "constraint"));
                    }
                }
                break;

            case "maxlength":
                if (fieldValue instanceof String && config.getMaxValue() != null) {
                    String strValue = (String) fieldValue;
                    if (strValue.length() > config.getMaxValue().intValue()) {
                        errors.add(new ValidationError(fieldName, "TOO_LONG",
                            config.getMessage() != null ? config.getMessage() :
                            fieldName + " cannot exceed " + config.getMaxValue() + " characters", "constraint"));
                    }
                }
                break;

            case "pattern":
                if (fieldValue instanceof String && config.getPattern() != null) {
                    String strValue = (String) fieldValue;
                    if (!strValue.matches(config.getPattern())) {
                        errors.add(new ValidationError(fieldName, "INVALID_PATTERN",
                            config.getMessage() != null ? config.getMessage() :
                            fieldName + " format is invalid", "constraint"));
                    }
                }
                break;

            case "range":
                if (fieldValue instanceof Number && config.getMinValue() != null && config.getMaxValue() != null) {
                    double numValue = ((Number) fieldValue).doubleValue();
                    if (numValue < config.getMinValue().doubleValue() || numValue > config.getMaxValue().doubleValue()) {
                        errors.add(new ValidationError(fieldName, "OUT_OF_RANGE",
                            config.getMessage() != null ? config.getMessage() :
                            fieldName + " must be between " + config.getMinValue() + " and " + config.getMaxValue(), "constraint"));
                    }
                }
                break;

            default:
                logger.warn("Unknown validation rule type: {}", ruleType);
                break;
        }

        return errors;
    }
}

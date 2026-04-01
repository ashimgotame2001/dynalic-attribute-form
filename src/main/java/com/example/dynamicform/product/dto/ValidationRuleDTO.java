package com.example.dynamicform.product.dto;

import java.math.BigDecimal;

/**
 * DTO for product-provided validation rules and constraints.
 * Products use this to define validation configurations that the platform applies.
 */
public class ValidationRuleDTO {

    private String ruleName;
    private String entityType;
    private String fieldName;
    private String ruleType; // required, minlength, maxlength, pattern, range, etc.
    private String message; // Custom error message
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String pattern; // For regex patterns
    private String condition; // Optional condition for when to apply
    private Integer priority = 100; // Execution priority

    // Constructors
    public ValidationRuleDTO() {}

    public ValidationRuleDTO(String ruleName, String entityType, String fieldName, String ruleType) {
        this.ruleName = ruleName;
        this.entityType = entityType;
        this.fieldName = fieldName;
        this.ruleType = ruleType;
    }

    // Getters and setters
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public BigDecimal getMinValue() { return minValue; }
    public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }

    public BigDecimal getMaxValue() { return maxValue; }
    public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    @Override
    public String toString() {
        return String.format("ValidationRuleDTO{ruleName='%s', entityType='%s', fieldName='%s', ruleType='%s'}",
                           ruleName, entityType, fieldName, ruleType);
    }
}

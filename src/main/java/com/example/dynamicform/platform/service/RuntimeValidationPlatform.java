package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.validation.ValidationRule;
import com.example.dynamicform.product.dto.ValidationRuleDTO;

import java.util.List;

/**
 * Platform-facing port for runtime validation registration and lookup.
 */
public interface RuntimeValidationPlatform {

    void registerValidationRule(String entityType, ValidationRule rule);

    void registerValidationConfig(String entityType, ValidationRuleDTO config);

    void registerValidationConfigs(String entityType, List<ValidationRuleDTO> configs);

    List<ValidationRule> getValidationRules(String entityType);

    List<ValidationRuleDTO> getValidationConfigs(String entityType);

    void removeValidationRule(String entityType, String ruleName);

    void removeValidationConfigs(String entityType);
}

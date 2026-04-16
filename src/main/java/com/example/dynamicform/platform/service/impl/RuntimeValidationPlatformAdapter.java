package com.example.dynamicform.platform.service.impl;

import com.example.dynamicform.platform.core.validation.ConstraintValidationEngine;
import com.example.dynamicform.platform.core.validation.ConstraintValidationEngineImpl;
import com.example.dynamicform.platform.core.validation.rule.ValidationRule;
import com.example.dynamicform.platform.service.RuntimeValidationPlatform;
import com.example.dynamicform.product.dto.ValidationRuleDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Adapter exposing runtime validation registration through a platform port.
 */
@Service
public class RuntimeValidationPlatformAdapter implements RuntimeValidationPlatform {

    private final ConstraintValidationEngine validationEngine;

    public RuntimeValidationPlatformAdapter(ConstraintValidationEngine validationEngine) {
        this.validationEngine = validationEngine;
    }

    @Override
    public void registerValidationRule(String entityType, ValidationRule rule) {
        validationEngine.addValidationRule(rule);
    }

    @Override
    public void registerValidationConfig(String entityType, ValidationRuleDTO config) {
        if (validationEngine instanceof ConstraintValidationEngineImpl configurableEngine) {
            configurableEngine.registerValidationConfig(entityType, config);
        }
    }

    @Override
    public void registerValidationConfigs(String entityType, List<ValidationRuleDTO> configs) {
        if (validationEngine instanceof ConstraintValidationEngineImpl configurableEngine) {
            configurableEngine.registerValidationConfigs(entityType, configs);
        }
    }

    @Override
    public List<ValidationRule> getValidationRules(String entityType) {
        return validationEngine.getValidationRules(entityType);
    }

    @Override
    public List<ValidationRuleDTO> getValidationConfigs(String entityType) {
        if (validationEngine instanceof ConstraintValidationEngineImpl configurableEngine) {
            return configurableEngine.getValidationConfigs(entityType);
        }
        return List.of();
    }

    @Override
    public void removeValidationRule(String entityType, String ruleName) {
        validationEngine.removeValidationRule(ruleName);
    }

    @Override
    public void removeValidationConfigs(String entityType) {
        if (validationEngine instanceof ConstraintValidationEngineImpl configurableEngine) {
            configurableEngine.removeValidationConfigs(entityType);
        }
    }
}

package com.example.dynamicform.platform.engine;

import com.example.dynamicform.platform.dto.FormDefinition;
import com.example.dynamicform.platform.dto.ValidationError;
import com.example.dynamicform.platform.exception.DynamicValidationException;
import com.example.dynamicform.platform.exception.FormNotFoundException;
import com.example.dynamicform.platform.interpreter.MetadataInterpreter;
import com.example.dynamicform.product.service.FormConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation of the DynamicFormEngine.
 */
@Service
public class DynamicFormEngineImpl implements DynamicFormEngine {

    private static final Logger logger = LoggerFactory.getLogger(DynamicFormEngineImpl.class);

    private final FormConfigService formConfigService;
    private final MetadataInterpreter metadataInterpreter;
    private final ObjectMapper objectMapper;
    private final com.example.dynamicform.platform.validation.ValidationEngine validationEngine;

    public DynamicFormEngineImpl(FormConfigService formConfigService,
                                 MetadataInterpreter metadataInterpreter,
                                 ObjectMapper objectMapper,
                                 com.example.dynamicform.platform.validation.ValidationEngine validationEngine) {
        this.formConfigService = formConfigService;
        this.metadataInterpreter = metadataInterpreter;
        this.objectMapper = objectMapper;
        this.validationEngine = validationEngine;
    }

    @Override
    @Cacheable(value = "formDefinitions", key = "#formName")
    public FormDefinition getFormDefinition(String formName) throws FormNotFoundException {
        return getFormDefinition(formName, -1);
    }

    @Override
    public FormDefinition getFormDefinition(String formName, int version) throws FormNotFoundException {
        logger.debug("Fetching form definition for: {}, version: {}", formName, version);
        var config = version > 0 
                ? formConfigService.findActiveConfiguration(formName, version)
                : formConfigService.findActiveConfiguration(formName);
        
        if (config == null) {
            throw new FormNotFoundException(formName);
        }

        try {
            var rawMetadata = formConfigService.resolveMetadata(config);
            if (rawMetadata.getModuleName() == null || rawMetadata.getModuleName().isBlank()) {
                rawMetadata.setModuleName("Dynamic Form");
            }
            if (rawMetadata.getArtifactName() == null || rawMetadata.getArtifactName().isBlank()) {
                rawMetadata.setArtifactName(config.getFormName());
            }
            rawMetadata.setVersion(String.valueOf(config.getVersion()));
            FormDefinition formDefinition = metadataInterpreter.interpret(rawMetadata, config.getFormName(), config.getVersion());

            // Add additional metadata from entity
            FormDefinition updated = FormDefinition.builder()
                    .formName(formDefinition.getFormName())
                    .fields(formDefinition.getFields())
                    .version(formDefinition.getVersion())
                    .rawMetadata(rawMetadata)
                    .description(config.getDescription())
                    .targetClassName(rawMetadata.getTargetClassName())
                    .moduleName(rawMetadata.getModuleName())
                    .artifactName(rawMetadata.getArtifactName())
                    .build();
    
            return updated;
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Override
    public List<ValidationError> validate(String formName, Map<String, Object> data) {
        try {
            FormDefinition formDef = getFormDefinition(formName);
            return validationEngine.validate(formDef, data);
        } catch (FormNotFoundException e) {
            logger.error("Form not found for validation: {}", formName);
            return List.of(ValidationError.builder()
                    .fieldPath("form")
                    .message("Form definition not found")
                    .validationType("system")
                    .build());
        }
    }

    @Override
    public <T> T validateAndMap(String formName, Map<String, Object> data, Class<T> targetClass) 
            throws DynamicValidationException, FormNotFoundException {
        List<ValidationError> errors = validate(formName, data);
        if (!errors.isEmpty()) {
            throw new DynamicValidationException("Validation failed with " + errors.size() + " errors", errors);
        }

        try {
            return objectMapper.convertValue(data, targetClass);
        } catch (Exception e) {
            logger.error("Failed to map data to DTO class {}: {}", targetClass.getName(), e.getMessage(), e);
            throw new DynamicValidationException("Failed to map data to target DTO: " + e.getMessage(), "mapping");
        }
    }

    @Override
    public Object validateAndMap(String formName, Map<String, Object> data) 
            throws DynamicValidationException, FormNotFoundException {
        FormDefinition formDef = getFormDefinition(formName);
        if (formDef.getTargetClassName() == null) {
            throw new FormNotFoundException("Target DTO class not configured for form: " + formName);
        }
        try {
            Class<?> targetClass = Class.forName(formDef.getTargetClassName());
            return validateAndMap(formName, data, targetClass);
        } catch (ClassNotFoundException e) {
            logger.error("Target DTO class not found: {}", formDef.getTargetClassName(), e);
            throw new FormNotFoundException("Target DTO class not found: " + formDef.getTargetClassName());
        }
    }

    @Override
    public boolean formExists(String formName) {
        return formConfigService.existsByFormName(formName);
    }
}

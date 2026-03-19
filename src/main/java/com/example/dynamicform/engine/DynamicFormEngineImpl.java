package com.example.dynamicform.engine;

import com.example.dynamicform.dto.FormDefinition;
import com.example.dynamicform.dto.ValidationError;
import com.example.dynamicform.exception.DynamicValidationException;
import com.example.dynamicform.exception.FormNotFoundException;
import com.example.dynamicform.interpreter.MetadataInterpreter;
import com.example.dynamicform.repository.FormConfigurationRepository;
import com.example.dynamicform.service.FormConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
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
    private final com.example.dynamicform.validation.ValidationEngine validationEngine;

    public DynamicFormEngineImpl(FormConfigService formConfigService,
                                 MetadataInterpreter metadataInterpreter,
                                 ObjectMapper objectMapper,
                                 com.example.dynamicform.validation.ValidationEngine validationEngine) {
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
            // Interpret the metadata JSON into FormDefinition
            var rawMetadata = objectMapper.readValue(config.getMetadataJson(), com.example.dynamicform.dto.metadata.RawFormMetadata.class);
            rawMetadata.setModuleName("Customer Management");
            rawMetadata.setArtifactName(config.getFormName());
            rawMetadata.setVersion(String.valueOf(config.getVersion()));
            FormDefinition formDefinition = metadataInterpreter.interpret(rawMetadata, config.getFormName(), config.getVersion());

            // Add additional metadata from entity
            FormDefinition updated = FormDefinition.builder()
                    .formName(formDefinition.getFormName())
                    .fields(formDefinition.getFields())
                    .version(formDefinition.getVersion())
                    .rawMetadata(rawMetadata)
                    .description(config.getDescription())
                    .targetClassName(config.getTargetDtoClassName())
                    .moduleName("Customer Management")
                    .artifactName(config.getFormName())
                    .build();
    
            return updated;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse metadata JSON for form: " + formName, e);
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
            // Convert map to target DTO
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

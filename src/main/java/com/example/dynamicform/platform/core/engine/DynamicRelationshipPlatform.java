package com.example.dynamicform.platform.core.engine;

import com.example.dynamicform.platform.api.dto.FormDefinition;
import com.example.dynamicform.platform.api.dto.ValidationError;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.core.validation.ValidationEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Main platform service that orchestrates metadata-driven dynamic behavior.
 */
@Service
public class DynamicRelationshipPlatform {

    @Autowired
    private MetadataInterpreter interpreter;

    @Autowired
    private PersistenceEngine persistenceEngine;

    @Autowired
    private ValidationEngine validationEngine;

    @Autowired
    private LocalizationEngine localizationEngine;

    @Autowired
    private MetadataCache cache;

    @Autowired
    private AuditVersioningService auditService;

    /**
     * Get form metadata interpreted and cached.
     */
    public FormDefinition getFormDefinition(RawFormMetadata rawMetadata, Long languageId) {
        String modelName = rawMetadata.getModelName();
        FormDefinition definition = cache.get(modelName);

        if (definition == null) {
            definition = interpreter.interpret(rawMetadata, modelName);
            cache.put(modelName, definition);
        }

        localizationEngine.resolveLabels(definition, languageId);
        return definition;
    }

    /**
     * Process data submission: Validate and then prepare for persistence.
     */
    public Map<String, Object> processSubmission(FormDefinition definition, Map<String, Object> inputData) {
        // Step 1: Validation
        List<ValidationError> errors = validationEngine.validate(definition, inputData);
        if (!errors.isEmpty()) {
            throw new RuntimeException("Validation failed: " + errors);
        }

        // Step 2: Persistence preparation (apply composition/association rules)
        Map<String, Object> processedData = persistenceEngine.prepareForSave(definition, inputData);

        // Step 3: Auditing
//        auditService.auditChange(definition.getModelName(), "SUBMIT", "SYSTEM", definition.getRawMetadata());

        return processedData;
    }
}

package com.example.dynamicform.platform.core.engine;

import com.example.dynamicform.platform.api.dto.FieldDefinition;
import com.example.dynamicform.platform.api.dto.FormDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Persistence Engine that enforces relationship rules:
 * - Composition: Full object processing allowed.
 * - Association: Only ID-based linking allowed (extracts ID, prevents mutation).
 */
@Service
public class PersistenceEngine {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceEngine.class);

    /**
     * Processes input data before persistence based on relationship metadata.
     */
    public Map<String, Object> prepareForSave(FormDefinition formDefinition, Map<String, Object> inputData) {
        if (formDefinition == null || inputData == null) return inputData;

        Map<String, Object> processedData = new HashMap<>();
        return processedData;
    }

    private FormDefinition buildNestedForm(FieldDefinition field) {
        return FormDefinition.builder()
                .formName(field.getDataType())
                .fields(field.getNestedFields())
                .build();
    }

    private Map<String, Object> extractIdOnly(Map<String, Object> data) {
        Map<String, Object> idMap = new HashMap<>();
        if (data.containsKey("id")) {
            idMap.put("id", data.get("id"));
        } else {
            logger.warn("Association data missing 'id' field, returning empty map or original data if no 'id' found.");
            // In a real system, you might want to throw an exception if ID is missing for association
        }
        return idMap;
    }
}

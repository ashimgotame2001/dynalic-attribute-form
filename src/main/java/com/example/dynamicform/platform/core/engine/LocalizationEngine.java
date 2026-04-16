package com.example.dynamicform.platform.core.engine;

import com.example.dynamicform.platform.api.dto.FieldDefinition;
import com.example.dynamicform.platform.api.dto.FormDefinition;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Simplified Localization Engine that resolves labels dynamically.
 */
@Service
public class LocalizationEngine {

    /**
     * Resolves labels for all fields in the form definition.
     */
    public void resolveLabels(FormDefinition formDefinition, Long languageId) {
        if (formDefinition == null || formDefinition.getFields() == null) return;
        resolveFields(formDefinition.getFields(), languageId);
    }

    private void resolveFields(List<FieldDefinition> fields, Long languageId) {
        for (FieldDefinition field : fields) {
            // Priority: Explicit shortLabel -> Inferred label
            if (field.getShortLabel() != null) {
                field.setLabel(field.getShortLabel());
            }

            // Recursive resolution for nested fields
            if (field.getNestedFields() != null) {
                resolveFields(field.getNestedFields(), languageId);
            }
        }
    }
}

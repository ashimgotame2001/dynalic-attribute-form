package com.example.dynamicform.platform.interpreter;

import com.example.dynamicform.platform.dto.FieldDefinition;
import com.example.dynamicform.platform.dto.FormDefinition;
import com.example.dynamicform.platform.dto.UiMetadata;
import com.example.dynamicform.platform.dto.ValidationDefinition;
import com.example.dynamicform.platform.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.exception.MetadataInterpretationException;
import com.example.dynamicform.platform.util.MetadataUtils;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of MetadataInterpreter that converts raw JSON metadata into internal FormDefinition.
 */
@Component
public class MetadataInterpreterImpl implements MetadataInterpreter {

    private static final Logger logger = LoggerFactory.getLogger(MetadataInterpreterImpl.class);

    @Override
    public FormDefinition interpret(RawFormMetadata rawMetadata, String formName) {
        return interpret(rawMetadata, formName, 1);
    }

    @Override
    public FormDefinition interpret(RawFormMetadata rawMetadata, String formName, int version) {
        if (rawMetadata == null) {
            throw new MetadataInterpretationException("Raw metadata cannot be null");
        }

        logger.debug("Interpreting metadata for form: {}, version: {}", formName, version);

        List<FieldDefinition> fields = new ArrayList<>();
        List<RawDomainAttribute> domainModel = rawMetadata.getDomainModel() != null ? rawMetadata.getDomainModel().getAttributes() : null;

        if (domainModel != null) {
            for (RawDomainAttribute attribute : domainModel) {
                FieldDefinition field = interpretAttribute(attribute);
                if (field != null) {
                    fields.add(field);
                }
            }
        }

        FormDefinition formDefinition = FormDefinition.builder()
                .formName(formName)
                .fields(fields)
                .version(version)
                .rawMetadata(rawMetadata)
                .build();

        logger.info("Successfully interpreted form definition for: {} with {} top-level fields", formName, fields.size());
        return formDefinition;
    }

    /**
     * Recursively interprets a RawDomainAttribute into a FieldDefinition.
     */
    private FieldDefinition interpretAttribute(RawDomainAttribute raw) {
        if (raw == null) {
            return null;
        }

        String attributeName = raw.getAttributeName();
        if (attributeName == null) {
            logger.warn("Skipping attribute with null attributeName in model: {}", raw.getModelName());
            return null;
        }

        String modelName = raw.getModelName();
        Boolean isReference = raw.getReference() != null ? raw.getReference() : false;
        Boolean isCollection = raw.getCollection() != null ? raw.getCollection() : false;
        Boolean visible = isReference ? null : (raw.getVisible() != null ? raw.getVisible() : true);

        // Build UI metadata
        UiMetadata uiMetadata = UiMetadata.builder()
                .label(MetadataUtils.toLabel(attributeName))
                .componentType(MetadataUtils.inferComponentType(isReference ? "object" : modelName))
                .required(raw.getValidations() != null && raw.getValidations().stream()
                        .anyMatch(v -> "required".equalsIgnoreCase(v.keySet().iterator().next())))
                .build();

        // Build ValidationDefinitions
        List<ValidationDefinition> validations = new ArrayList<>();
        if (raw.getValidations() != null) {
            for (var rawValidation : raw.getValidations()) {
                try {
                    ValidationDefinition validation = ValidationDefinition.fromRaw((Map<String, Object>) rawValidation);
                    validations.add(validation);
                } catch (Exception e) {
                    logger.warn("Failed to parse validation rule for attribute {}: {}", attributeName, e.getMessage());
                }
            }
        }

        // Build nested fields if this is a reference (nested object)
        List<FieldDefinition> nestedFields = new ArrayList<>();
        String elementType = null;

        if (isReference) {
            List<RawDomainAttribute> nestedDomain = raw.getDomainModel() != null ? raw.getDomainModel().getAttributes() : null;
            if (nestedDomain != null) {
                for (RawDomainAttribute nestedAttr : nestedDomain) {
                    FieldDefinition nestedField = interpretAttribute(nestedAttr);
                    if (nestedField != null) {
                        nestedFields.add(nestedField);
                    }
                }
            }
        }

        // If it's a collection, set elementType
        if (isCollection) {
            elementType = modelName;
        }

        return FieldDefinition.builder()
                .fieldName(attributeName)
                .label(uiMetadata.getLabel())
                .shortLabel(raw.getShortLabel())
                .longLabel(raw.getLongLabel())
                .dataType(modelName)
                .isNestedObject(isReference)
                .isCollection(isCollection)
                .nestedFields(nestedFields.isEmpty() ? null : nestedFields)
                .elementType(elementType)
                .visible(visible)
                .validations(validations.isEmpty() ? null : validations)
                .uiMetadata(uiMetadata)
                .rawAttribute(raw)
                .build();
    }
}

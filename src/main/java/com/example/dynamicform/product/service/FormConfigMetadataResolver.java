package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.dto.FieldSpecRequest;
import com.example.dynamicform.platform.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.product.entity.CustomerFormConfigurationEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class FormConfigMetadataResolver {

    private final ObjectMapper objectMapper;
    private final FormConfigFieldAdapter formConfigFieldAdapter;

    public FormConfigMetadataResolver(ObjectMapper objectMapper, FormConfigFieldAdapter formConfigFieldAdapter) {
        this.objectMapper = objectMapper;
        this.formConfigFieldAdapter = formConfigFieldAdapter;
    }

    public RawFormMetadata resolve(CustomerFormConfigurationEntity entity) {
        if (entity == null || entity.getMetadataJson() == null || entity.getMetadataJson().isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(entity.getMetadataJson(), RawFormMetadata.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse metadata JSON for form: " + entity.getFormName(), e);
        }
    }

    public RawFormMetadata resolveWithTranslations(CustomerFormConfigurationEntity entity) {
        if (entity == null || entity.getMetadataJson() == null || entity.getMetadataJson().isBlank()) {
            return null;
        }
        try {
            RawFormMetadata metadata = objectMapper.readValue(entity.getMetadataJson(), RawFormMetadata.class);
            overlay(metadata, formConfigFieldAdapter.toFieldSpecs(entity));
            return metadata;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse metadata JSON for form: " + entity.getFormName(), e);
        }
    }

    public String toMetadataJson(CustomerFormConfigurationEntity entity) {
        RawFormMetadata metadata = resolveWithTranslations(entity);
        if (metadata == null) {
            return entity.getMetadataJson();
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize metadata JSON for form: " + entity.getFormName(), e);
        }
    }

    private void overlay(RawFormMetadata metadata, List<FieldSpecRequest.FieldSpec> fieldSpecs) {
        if (metadata == null || metadata.getDomainModel() == null || metadata.getDomainModel().getAttributes() == null || fieldSpecs == null) {
            return;
        }
        Map<String, FieldSpecRequest.FieldSpec> fieldMap = new LinkedHashMap<>();
        for (FieldSpecRequest.FieldSpec fieldSpec : fieldSpecs) {
            if (fieldSpec != null && fieldSpec.getReferenceModel() != null) {
                fieldMap.put(fieldSpec.getReferenceModel(), fieldSpec);
            }
        }
        for (RawDomainAttribute attribute : metadata.getDomainModel().getAttributes()) {
            overlayAttribute(attribute, fieldMap);
        }
    }

    private void overlayAttribute(RawDomainAttribute attribute, Map<String, FieldSpecRequest.FieldSpec> fieldMap) {
        if (attribute == null) {
            return;
        }
        FieldSpecRequest.FieldSpec spec = fieldMap.get(attribute.getReferenceModel());
        if (spec != null) {
            attribute.setVisible(spec.getVisible());
            attribute.setShortLabel(spec.getShortLabel());
            attribute.setLongLabel(spec.getLongLabel());
            attribute.setShortLabelI18n(spec.getShortLabelI18n());
            attribute.setLongLabelI18n(spec.getLongLabelI18n());
            mergeValidationTranslations(attribute, spec.getValidations());
        }
        if (attribute.getDomainModel() != null && attribute.getDomainModel().getAttributes() != null) {
            for (RawDomainAttribute child : attribute.getDomainModel().getAttributes()) {
                overlayAttribute(child, fieldMap);
            }
        }
    }

    private void mergeValidationTranslations(RawDomainAttribute attribute, Object validations) {
        if (attribute.getValidations() == null || attribute.getValidations().isEmpty() || !(validations instanceof Map<?, ?> sourceMap)) {
            return;
        }
        Map<String, Object> normalizedSource = new LinkedHashMap<>();
        sourceMap.forEach((key, value) -> normalizedSource.put(String.valueOf(key), value));

        for (Map<String, Object> rawValidation : attribute.getValidations()) {
            for (Map.Entry<String, Object> entry : rawValidation.entrySet()) {
                Object sourceValue = normalizedSource.get(entry.getKey());
                if (sourceValue instanceof Map<?, ?> sourceParams && entry.getValue() instanceof Map<?, ?> targetParams) {
                    Map<String, Object> merged = new LinkedHashMap<>();
                    targetParams.forEach((key, value) -> merged.put(String.valueOf(key), value));
                    sourceParams.forEach((key, value) -> merged.put(String.valueOf(key), value));
                    entry.setValue(merged);
                }
            }
        }
    }
}

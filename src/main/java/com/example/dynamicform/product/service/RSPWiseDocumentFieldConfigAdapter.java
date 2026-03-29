package com.example.dynamicform.product.service;

import com.example.dynamicform.product.dto.RSPWiseDocumentSetupRequest;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldConfigEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldTranslationEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldValidationEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldValidationType;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldValidationTranslationEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentSetupEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Component
public class RSPWiseDocumentFieldConfigAdapter {

    private static final String DEFAULT_REQUIRED_MESSAGE = "Field is required";
    private final ObjectMapper objectMapper;

    public RSPWiseDocumentFieldConfigAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<RSPWiseDocumentFieldConfigEntity> toEntities(RSPWiseDocumentSetupEntity setup,
                                                             List<RSPWiseDocumentSetupRequest.FieldSpec> requestFields) {
        List<RSPWiseDocumentSetupRequest.FieldSpec> fields = requestFields == null ? Collections.emptyList() : requestFields;
        List<RSPWiseDocumentFieldConfigEntity> entities = new ArrayList<>(fields.size());
        for (int index = 0; index < fields.size(); index++) {
            RSPWiseDocumentSetupRequest.FieldSpec requestField = fields.get(index);
            if (requestField == null || requestField.getReferenceModel() == null || requestField.getReferenceModel().isBlank()) {
                continue;
            }

            RSPWiseDocumentFieldConfigEntity fieldEntity = RSPWiseDocumentFieldConfigEntity.builder()
                    .setup(setup)
                    .referenceModel(requestField.getReferenceModel())
                    .displayOrder(index)
                    .visible(requestField.getVisible())
                    .shortLabel(requestField.getShortLabel())
                    .longLabel(requestField.getLongLabel())
                    .translations(new LinkedHashSet<>())
                    .validations(new LinkedHashSet<>())
                    .build();

            fieldEntity.getTranslations().addAll(toFieldTranslations(
                    fieldEntity,
                    requestField.getShortLabelI18n(),
                    requestField.getLongLabelI18n()));
            RSPWiseDocumentFieldValidationEntity requiredValidation = toRequiredValidation(fieldEntity, requestField);
            if (requiredValidation != null) {
                fieldEntity.getValidations().add(requiredValidation);
            }
            entities.add(fieldEntity);
        }
        return entities;
    }

    public List<RSPWiseDocumentSetupRequest.FieldSpec> toFieldSpecs(RSPWiseDocumentSetupEntity setup) {
        if (setup.getFieldConfigs() != null && !setup.getFieldConfigs().isEmpty()) {
            return setup.getFieldConfigs().stream()
                    .sorted(Comparator.comparing(config -> config.getDisplayOrder() != null ? config.getDisplayOrder() : Integer.MAX_VALUE))
                    .map(this::toFieldSpec)
                    .toList();
        }

        List<RSPWiseDocumentSetupRequest.FieldSpec> legacyFields = readLegacyMetadata(setup.getMetadataJson());
        if (!legacyFields.isEmpty()) {
            return legacyFields;
        }

        return buildLegacyFallbackFields(setup);
    }

    public Map<String, RSPWiseDocumentSetupRequest.FieldSpec> toFieldMap(RSPWiseDocumentSetupEntity setup) {
        List<RSPWiseDocumentSetupRequest.FieldSpec> fieldSpecs = toFieldSpecs(setup);
        if (fieldSpecs.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldMap = new LinkedHashMap<>();
        for (RSPWiseDocumentSetupRequest.FieldSpec field : fieldSpecs) {
            if (field != null && field.getReferenceModel() != null) {
                fieldMap.put(field.getReferenceModel(), field);
            }
        }
        return fieldMap;
    }

    private RSPWiseDocumentFieldValidationEntity toRequiredValidation(RSPWiseDocumentFieldConfigEntity fieldEntity,
                                                                      RSPWiseDocumentSetupRequest.FieldSpec requestField) {
        if (requestField.getValidations() == null || requestField.getValidations().getRequired() == null) {
            return null;
        }
        RSPWiseDocumentFieldValidationEntity validationEntity = RSPWiseDocumentFieldValidationEntity.builder()
                .fieldConfig(fieldEntity)
                .validationType(RSPWiseDocumentFieldValidationType.REQUIRED)
                .enabled(Boolean.TRUE.equals(requestField.getValidations().getRequired().getValue()))
                .message(requestField.getValidations().getRequired().getMessage())
                .translations(new LinkedHashSet<>())
                .build();
        validationEntity.getTranslations().addAll(toValidationTranslations(
                validationEntity,
                requestField.getValidations().getRequired().getMessageI18n()));
        return validationEntity;
    }

    private RSPWiseDocumentSetupRequest.FieldSpec toFieldSpec(RSPWiseDocumentFieldConfigEntity entity) {
        return RSPWiseDocumentSetupRequest.FieldSpec.builder()
                .referenceModel(entity.getReferenceModel())
                .visible(entity.getVisible())
                .shortLabel(entity.getShortLabel())
                .shortLabelI18n(toFieldShortLabelI18n(entity.getTranslations()))
                .longLabel(entity.getLongLabel())
                .longLabelI18n(toFieldLongLabelI18n(entity.getTranslations()))
                .validations(toValidationDefinition(entity.getValidations()))
                .build();
    }

    private RSPWiseDocumentSetupRequest.ValidationDefinition toValidationDefinition(Collection<RSPWiseDocumentFieldValidationEntity> validations) {
        if (validations == null || validations.isEmpty()) {
            return null;
        }
        RSPWiseDocumentFieldValidationEntity required = validations.stream()
                .filter(validation -> validation.getValidationType() == RSPWiseDocumentFieldValidationType.REQUIRED)
                .findFirst()
                .orElse(null);
        if (required == null) {
            return null;
        }
        return RSPWiseDocumentSetupRequest.ValidationDefinition.builder()
                .required(RSPWiseDocumentSetupRequest.RequiredValidation.builder()
                        .value(Boolean.TRUE.equals(required.getEnabled()))
                        .message(required.getMessage())
                        .messageI18n(toValidationMessageI18n(required.getTranslations()))
                        .build())
                .build();
    }

    private List<RSPWiseDocumentFieldTranslationEntity> toFieldTranslations(RSPWiseDocumentFieldConfigEntity fieldEntity,
                                                                            Map<String, String> shortLabelI18n,
                                                                            Map<String, String> longLabelI18n) {
        TreeSet<String> locales = new TreeSet<>();
        if (shortLabelI18n != null) {
            locales.addAll(shortLabelI18n.keySet());
        }
        if (longLabelI18n != null) {
            locales.addAll(longLabelI18n.keySet());
        }

        List<RSPWiseDocumentFieldTranslationEntity> translations = new ArrayList<>(locales.size());
        for (String locale : locales) {
            if (locale == null || locale.isBlank()) {
                continue;
            }
            translations.add(RSPWiseDocumentFieldTranslationEntity.builder()
                    .fieldConfig(fieldEntity)
                    .locale(locale)
                    .shortLabel(shortLabelI18n != null ? shortLabelI18n.get(locale) : null)
                    .longLabel(longLabelI18n != null ? longLabelI18n.get(locale) : null)
                    .build());
        }
        return translations;
    }

    private List<RSPWiseDocumentFieldValidationTranslationEntity> toValidationTranslations(
            RSPWiseDocumentFieldValidationEntity validationEntity,
            Map<String, String> messageI18n) {
        if (messageI18n == null || messageI18n.isEmpty()) {
            return new ArrayList<>();
        }
        List<RSPWiseDocumentFieldValidationTranslationEntity> translations = new ArrayList<>(messageI18n.size());
        for (Map.Entry<String, String> entry : messageI18n.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }
            translations.add(RSPWiseDocumentFieldValidationTranslationEntity.builder()
                    .validation(validationEntity)
                    .locale(entry.getKey())
                    .message(entry.getValue())
                    .build());
        }
        return translations;
    }

    private Map<String, String> toFieldShortLabelI18n(Collection<RSPWiseDocumentFieldTranslationEntity> translations) {
        Map<String, String> labels = new LinkedHashMap<>();
        if (translations == null) {
            return labels;
        }
        for (RSPWiseDocumentFieldTranslationEntity translation : translations) {
            if (translation.getLocale() != null && translation.getShortLabel() != null) {
                labels.put(translation.getLocale(), translation.getShortLabel());
            }
        }
        return labels;
    }

    private Map<String, String> toFieldLongLabelI18n(Collection<RSPWiseDocumentFieldTranslationEntity> translations) {
        Map<String, String> labels = new LinkedHashMap<>();
        if (translations == null) {
            return labels;
        }
        for (RSPWiseDocumentFieldTranslationEntity translation : translations) {
            if (translation.getLocale() != null && translation.getLongLabel() != null) {
                labels.put(translation.getLocale(), translation.getLongLabel());
            }
        }
        return labels;
    }

    private Map<String, String> toValidationMessageI18n(Collection<RSPWiseDocumentFieldValidationTranslationEntity> translations) {
        Map<String, String> messages = new LinkedHashMap<>();
        if (translations == null) {
            return messages;
        }
        for (RSPWiseDocumentFieldValidationTranslationEntity translation : translations) {
            if (translation.getLocale() != null && translation.getMessage() != null) {
                messages.put(translation.getLocale(), translation.getMessage());
            }
        }
        return messages;
    }

    private List<RSPWiseDocumentSetupRequest.FieldSpec> readLegacyMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<List<RSPWiseDocumentSetupRequest.FieldSpec>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<RSPWiseDocumentSetupRequest.FieldSpec> buildLegacyFallbackFields(RSPWiseDocumentSetupEntity setup) {
        List<RSPWiseDocumentSetupRequest.FieldSpec> fields = new ArrayList<>();
        fields.add(buildLegacyField("isDocumentNumberRequired", setup.isDocumentNumberRequired()));
        fields.add(buildLegacyField("isBackRequired", setup.isBackRequired()));
        fields.add(buildLegacyField("isIssuedCountryRequired", setup.isIssuedCountryRequired()));
        fields.add(buildLegacyField("isExpiryDateRequired", setup.isExpiryDateRequired()));
        fields.add(buildLegacyField("isPrimaryContentRequired", setup.isPrimaryContentRequired()));
        fields.add(buildLegacyField("isSecondaryContentRequired", setup.isSecondaryContentRequired()));
        return fields;
    }

    private RSPWiseDocumentSetupRequest.FieldSpec buildLegacyField(String referenceModel, boolean required) {
        return RSPWiseDocumentSetupRequest.FieldSpec.builder()
                .referenceModel(referenceModel)
                .visible(Boolean.TRUE)
                .validations(RSPWiseDocumentSetupRequest.ValidationDefinition.builder()
                        .required(RSPWiseDocumentSetupRequest.RequiredValidation.builder()
                                .value(required)
                                .message(DEFAULT_REQUIRED_MESSAGE)
                                .build())
                        .build())
                .build();
    }
}

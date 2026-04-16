package com.example.dynamicform.product.service;

import com.example.dynamicform.product.dto.DocumentSetupRequest;
import com.example.dynamicform.product.entity.DocumentFieldConfigEntity;
import com.example.dynamicform.product.entity.DocumentFieldTranslationEntity;
import com.example.dynamicform.product.entity.DocumentFieldValidationEntity;
import com.example.dynamicform.product.entity.DocumentFieldValidationTranslationEntity;
import com.example.dynamicform.product.entity.DocumentSetupEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class DocumentFieldConfigAdapter {

    private static final String DEFAULT_REQUIRED_MESSAGE = "Field is required";
    private final ObjectMapper objectMapper;

    public DocumentFieldConfigAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<DocumentFieldConfigEntity> toEntities(DocumentSetupEntity setup,
                                                      List<DocumentSetupRequest.FieldSpec> requestFields) {
        List<DocumentSetupRequest.FieldSpec> fields = requestFields == null ? Collections.emptyList() : requestFields;
        List<DocumentFieldConfigEntity> entities = new ArrayList<>(fields.size());
        for (int index = 0; index < fields.size(); index++) {
            DocumentSetupRequest.FieldSpec requestField = fields.get(index);
            if (requestField == null || requestField.getReferenceModel() == null || requestField.getReferenceModel().isBlank()) {
                continue;
            }

            DocumentFieldConfigEntity fieldEntity = DocumentFieldConfigEntity.builder()
                    .setup(setup)
                    .referenceModel(DocumentReferenceModels.normalize(requestField.getReferenceModel()))
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
            fieldEntity.getValidations().addAll(toValidationEntities(fieldEntity, requestField.getValidations()));
            entities.add(fieldEntity);
        }
        return entities;
    }

    public List<DocumentSetupRequest.FieldSpec> toFieldSpecs(DocumentSetupEntity setup) {
        if (setup.getFieldConfigs() != null && !setup.getFieldConfigs().isEmpty()) {
            return setup.getFieldConfigs().stream()
                    .sorted(Comparator.comparing(config -> config.getDisplayOrder() != null ? config.getDisplayOrder() : Integer.MAX_VALUE))
                    .map(this::toFieldSpec)
                    .toList();
        }

        List<DocumentSetupRequest.FieldSpec> legacyFields = readLegacyMetadata(setup.getMetadataJson());
        if (!legacyFields.isEmpty()) {
            return legacyFields;
        }

        return buildLegacyFallbackFields(setup);
    }

    public Map<String, DocumentSetupRequest.FieldSpec> toFieldMap(DocumentSetupEntity setup) {
        List<DocumentSetupRequest.FieldSpec> fieldSpecs = toFieldSpecs(setup);
        if (fieldSpecs.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, DocumentSetupRequest.FieldSpec> fieldMap = new LinkedHashMap<>();
        for (DocumentSetupRequest.FieldSpec field : fieldSpecs) {
            if (field != null && field.getReferenceModel() != null) {
                fieldMap.put(DocumentReferenceModels.normalize(field.getReferenceModel()), field);
            }
        }
        return fieldMap;
    }

    private List<DocumentFieldValidationEntity> toValidationEntities(DocumentFieldConfigEntity fieldEntity,
                                                                     Object validations) {
        List<Map<String, Object>> normalized = normalizeValidations(validations);
        List<DocumentFieldValidationEntity> entities = new ArrayList<>();
        int index = 0;
        for (Map<String, Object> validation : normalized) {
            for (Map.Entry<String, Object> entry : validation.entrySet()) {
                Map<String, Object> params = entry.getValue() instanceof Map<?, ?> map
                        ? castMap(map)
                        : Collections.emptyMap();
                DocumentFieldValidationEntity validationEntity = DocumentFieldValidationEntity.builder()
                        .fieldConfig(fieldEntity)
                        .displayOrder(index++)
                        .validationType(entry.getKey())
                        .valueJson(writeJson(params.get("value")))
                        .pattern(params.get("pattern") != null ? params.get("pattern").toString() : null)
                        .message(params.get("message") != null ? params.get("message").toString() : null)
                        .translations(new LinkedHashSet<>())
                        .build();
                validationEntity.getTranslations().addAll(toValidationTranslations(validationEntity, params.get("messageI18n")));
                entities.add(validationEntity);
            }
        }
        return entities;
    }

    private DocumentSetupRequest.FieldSpec toFieldSpec(DocumentFieldConfigEntity entity) {
        return DocumentSetupRequest.FieldSpec.builder()
                .referenceModel(DocumentReferenceModels.normalize(entity.getReferenceModel()))
                .visible(entity.getVisible())
                .shortLabel(entity.getShortLabel())
                .shortLabelI18n(toFieldShortLabelI18n(entity.getTranslations()))
                .longLabel(entity.getLongLabel())
                .longLabelI18n(toFieldLongLabelI18n(entity.getTranslations()))
                .validations(toValidationPayload(entity.getValidations()))
                .build();
    }

    private Object toValidationPayload(Collection<DocumentFieldValidationEntity> validations) {
        if (validations == null || validations.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        validations.stream()
                .sorted(Comparator.comparing(v -> v.getDisplayOrder() != null ? v.getDisplayOrder() : Integer.MAX_VALUE))
                .forEach(validation -> {
                    Map<String, Object> params = new LinkedHashMap<>();
                    Object value = readJson(validation.getValueJson());
                    if (value != null) {
                        params.put("value", value);
                    }
                    if (validation.getPattern() != null) {
                        params.put("pattern", validation.getPattern());
                    }
                    if (validation.getMessage() != null) {
                        params.put("message", validation.getMessage());
                    }
                    Map<Long, String> messageI18n = toValidationMessageI18n(validation.getTranslations());
                    if (!messageI18n.isEmpty()) {
                        params.put("messageI18n", messageI18n);
                    }
                    payload.put(validation.getValidationType(), params);
                });
        return payload;
    }

    private List<DocumentFieldTranslationEntity> toFieldTranslations(DocumentFieldConfigEntity fieldEntity,
                                                                     Map<Long, String> shortLabelI18n,
                                                                     Map<Long, String> longLabelI18n) {
        TreeSet<Long> languageIds = new TreeSet<>();
        if (shortLabelI18n != null) {
            languageIds.addAll(shortLabelI18n.keySet());
        }
        if (longLabelI18n != null) {
            languageIds.addAll(longLabelI18n.keySet());
        }

        List<DocumentFieldTranslationEntity> translations = new ArrayList<>(languageIds.size());
        for (Long languageId : languageIds) {
            if (languageId == null) {
                continue;
            }
            translations.add(DocumentFieldTranslationEntity.builder()
                    .fieldConfig(fieldEntity)
                    .languageId(languageId)
                    .shortLabel(shortLabelI18n != null ? shortLabelI18n.get(languageId) : null)
                    .longLabel(longLabelI18n != null ? longLabelI18n.get(languageId) : null)
                    .build());
        }
        return translations;
    }

    private List<DocumentFieldValidationTranslationEntity> toValidationTranslations(
            DocumentFieldValidationEntity validationEntity,
            Object messageI18n) {
        if (!(messageI18n instanceof Map<?, ?> map) || map.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, String> values = objectMapper.convertValue(map, new TypeReference<Map<Long, String>>() {});
        List<DocumentFieldValidationTranslationEntity> translations = new ArrayList<>(values.size());
        for (Map.Entry<Long, String> entry : values.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            translations.add(DocumentFieldValidationTranslationEntity.builder()
                    .validation(validationEntity)
                    .languageId(entry.getKey())
                    .message(entry.getValue())
                    .build());
        }
        return translations;
    }

    private Map<Long, String> toFieldShortLabelI18n(Collection<DocumentFieldTranslationEntity> translations) {
        Map<Long, String> labels = new LinkedHashMap<>();
        if (translations == null) {
            return labels;
        }
        for (DocumentFieldTranslationEntity translation : translations) {
            if (translation.getLanguageId() != null && translation.getShortLabel() != null) {
                labels.put(translation.getLanguageId(), translation.getShortLabel());
            }
        }
        return labels;
    }

    private Map<Long, String> toFieldLongLabelI18n(Collection<DocumentFieldTranslationEntity> translations) {
        Map<Long, String> labels = new LinkedHashMap<>();
        if (translations == null) {
            return labels;
        }
        for (DocumentFieldTranslationEntity translation : translations) {
            if (translation.getLanguageId() != null && translation.getLongLabel() != null) {
                labels.put(translation.getLanguageId(), translation.getLongLabel());
            }
        }
        return labels;
    }

    private Map<Long, String> toValidationMessageI18n(Collection<DocumentFieldValidationTranslationEntity> translations) {
        Map<Long, String> messages = new LinkedHashMap<>();
        if (translations == null) {
            return messages;
        }
        for (DocumentFieldValidationTranslationEntity translation : translations) {
            if (translation.getLanguageId() != null && translation.getMessage() != null) {
                messages.put(translation.getLanguageId(), translation.getMessage());
            }
        }
        return messages;
    }

    private List<Map<String, Object>> normalizeValidations(Object validations) {
        if (validations == null) {
            return Collections.emptyList();
        }
        if (validations instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    Map<String, Object> normalized = normalizeValidationEntry(castMap(map));
                    if (!normalized.isEmpty()) {
                        result.add(normalized);
                    }
                }
            }
            return result;
        }
        if (validations instanceof Map<?, ?> map) {
            Map<String, Object> normalized = normalizeValidationEntry(castMap(map));
            return normalized.isEmpty() ? Collections.emptyList() : List.of(normalized);
        }
        if (validations instanceof String str && !str.isBlank()) {
            try {
                Object parsed = objectMapper.readValue(str, Object.class);
                return normalizeValidations(parsed);
            } catch (JsonProcessingException e) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private Map<String, Object> normalizeValidationEntry(Map<String, Object> validation) {
        if (validation == null || validation.isEmpty()) {
            return Collections.emptyMap();
        }
        if (!validation.containsKey("type")) {
            return validation;
        }
        Object rawType = validation.get("type");
        if (rawType == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> params = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : validation.entrySet()) {
            if (!"type".equals(entry.getKey()) && entry.getValue() != null) {
                params.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.singletonMap(rawType.toString(), params);
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize validation value", e);
        }
    }

    private Object readJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            return json;
        }
    }

    private Map<String, Object> castMap(Map<?, ?> map) {
        return objectMapper.convertValue(map, new TypeReference<Map<String, Object>>() {});
    }

    private List<DocumentSetupRequest.FieldSpec> readLegacyMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<List<DocumentSetupRequest.FieldSpec>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<DocumentSetupRequest.FieldSpec> buildLegacyFallbackFields(DocumentSetupEntity setup) {
        List<DocumentSetupRequest.FieldSpec> fields = new ArrayList<>();
        fields.add(buildLegacyField(DocumentReferenceModels.DOCUMENT_NUMBER_REQUIRED, setup.isDocumentNumberRequired()));
        fields.add(buildLegacyField(DocumentReferenceModels.ISSUED_COUNTRY_REQUIRED, setup.isIssuedCountryRequired()));
        fields.add(buildLegacyField(DocumentReferenceModels.EXPIRY_DATE_REQUIRED, setup.isExpiryDateRequired()));
        fields.add(buildLegacyField(DocumentReferenceModels.PRIMARY_CONTENT_REQUIRED, setup.isPrimaryContentRequired()));
        fields.add(buildLegacyField(DocumentReferenceModels.SECONDARY_CONTENT_REQUIRED,
                setup.isSecondaryContentRequired() || setup.isBackRequired()));
        return fields;
    }

    private DocumentSetupRequest.FieldSpec buildLegacyField(String referenceModel, boolean required) {
        return DocumentSetupRequest.FieldSpec.builder()
                .referenceModel(referenceModel)
                .visible(Boolean.TRUE)
                .validations(Collections.singletonMap("required", new LinkedHashMap<>(Map.of(
                        "value", required,
                        "message", DEFAULT_REQUIRED_MESSAGE
                ))))
                .build();
    }
}

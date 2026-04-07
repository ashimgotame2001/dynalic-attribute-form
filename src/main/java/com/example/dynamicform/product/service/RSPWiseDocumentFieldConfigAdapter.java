package com.example.dynamicform.product.service;

import com.example.dynamicform.product.dto.RSPWiseDocumentSetupRequest;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldConfigEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldTranslationEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldValidationEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldValidationTranslationEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentSetupEntity;
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
                    .referenceModel(RSPWiseDocumentReferenceModels.normalize(requestField.getReferenceModel()))
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
                fieldMap.put(RSPWiseDocumentReferenceModels.normalize(field.getReferenceModel()), field);
            }
        }
        return fieldMap;
    }

    private List<RSPWiseDocumentFieldValidationEntity> toValidationEntities(RSPWiseDocumentFieldConfigEntity fieldEntity,
                                                                            Object validations) {
        List<Map<String, Object>> normalized = normalizeValidations(validations);
        List<RSPWiseDocumentFieldValidationEntity> entities = new ArrayList<>();
        int index = 0;
        for (Map<String, Object> validation : normalized) {
            for (Map.Entry<String, Object> entry : validation.entrySet()) {
                Map<String, Object> params = entry.getValue() instanceof Map<?, ?> map
                        ? castMap(map)
                        : Collections.emptyMap();
                RSPWiseDocumentFieldValidationEntity validationEntity = RSPWiseDocumentFieldValidationEntity.builder()
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

    private RSPWiseDocumentSetupRequest.FieldSpec toFieldSpec(RSPWiseDocumentFieldConfigEntity entity) {
        return RSPWiseDocumentSetupRequest.FieldSpec.builder()
                .referenceModel(RSPWiseDocumentReferenceModels.normalize(entity.getReferenceModel()))
                .visible(entity.getVisible())
                .shortLabel(entity.getShortLabel())
                .shortLabelI18n(toFieldShortLabelI18n(entity.getTranslations()))
                .longLabel(entity.getLongLabel())
                .longLabelI18n(toFieldLongLabelI18n(entity.getTranslations()))
                .validations(toValidationPayload(entity.getValidations()))
                .build();
    }

    private Object toValidationPayload(Collection<RSPWiseDocumentFieldValidationEntity> validations) {
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
                    Map<String, String> messageI18n = toValidationMessageI18n(validation.getTranslations());
                    if (!messageI18n.isEmpty()) {
                        params.put("messageI18n", messageI18n);
                    }
                    payload.put(validation.getValidationType(), params);
                });
        return payload;
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
            Object messageI18n) {
        if (!(messageI18n instanceof Map<?, ?> map) || map.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, String> values = objectMapper.convertValue(map, new TypeReference<Map<String, String>>() {});
        List<RSPWiseDocumentFieldValidationTranslationEntity> translations = new ArrayList<>(values.size());
        for (Map.Entry<String, String> entry : values.entrySet()) {
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
        fields.add(buildLegacyField(RSPWiseDocumentReferenceModels.DOCUMENT_NUMBER_REQUIRED, setup.isDocumentNumberRequired()));
        fields.add(buildLegacyField(RSPWiseDocumentReferenceModels.ISSUED_COUNTRY_REQUIRED, setup.isIssuedCountryRequired()));
        fields.add(buildLegacyField(RSPWiseDocumentReferenceModels.EXPIRY_DATE_REQUIRED, setup.isExpiryDateRequired()));
        fields.add(buildLegacyField(RSPWiseDocumentReferenceModels.PRIMARY_CONTENT_REQUIRED, setup.isPrimaryContentRequired()));
        fields.add(buildLegacyField(RSPWiseDocumentReferenceModels.SECONDARY_CONTENT_REQUIRED,
                setup.isSecondaryContentRequired() || setup.isBackRequired()));
        return fields;
    }

    private RSPWiseDocumentSetupRequest.FieldSpec buildLegacyField(String referenceModel, boolean required) {
        return RSPWiseDocumentSetupRequest.FieldSpec.builder()
                .referenceModel(referenceModel)
                .visible(Boolean.TRUE)
                .validations(Collections.singletonMap("required", new LinkedHashMap<>(Map.of(
                        "value", required,
                        "message", DEFAULT_REQUIRED_MESSAGE
                ))))
                .build();
    }
}

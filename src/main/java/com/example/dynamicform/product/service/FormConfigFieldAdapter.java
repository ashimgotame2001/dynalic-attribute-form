package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.dto.FieldSpecRequest;
import com.example.dynamicform.product.entity.CustomerFormConfigurationEntity;
import com.example.dynamicform.product.entity.FormConfigFieldEntity;
import com.example.dynamicform.product.entity.FormConfigFieldTranslationEntity;
import com.example.dynamicform.product.entity.FormConfigFieldValidationEntity;
import com.example.dynamicform.product.entity.FormConfigFieldValidationTranslationEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Component
public class FormConfigFieldAdapter {

    private final ObjectMapper objectMapper;

    public FormConfigFieldAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<FormConfigFieldEntity> toEntities(CustomerFormConfigurationEntity formConfiguration,
                                                  List<FieldSpecRequest.FieldSpec> fields) {
        List<FieldSpecRequest.FieldSpec> requestFields = fields == null ? Collections.emptyList() : fields;
        List<FormConfigFieldEntity> entities = new ArrayList<>(requestFields.size());
        for (int index = 0; index < requestFields.size(); index++) {
            FieldSpecRequest.FieldSpec requestField = requestFields.get(index);
            if (requestField == null || requestField.getReferenceModel() == null || requestField.getReferenceModel().isBlank()) {
                continue;
            }
            FormConfigFieldEntity entity = FormConfigFieldEntity.builder()
                    .formConfiguration(formConfiguration)
                    .referenceModel(requestField.getReferenceModel())
                    .displayOrder(index)
                    .visible(requestField.getVisible())
                    .shortLabel(requestField.getShortLabel())
                    .longLabel(requestField.getLongLabel())
                    .translations(new LinkedHashSet<>())
                    .validations(new LinkedHashSet<>())
                    .build();

            entity.getTranslations().addAll(toFieldTranslations(entity, requestField.getShortLabelI18n(), requestField.getLongLabelI18n()));
            entity.getValidations().addAll(toValidationEntities(entity, requestField.getValidations()));
            entities.add(entity);
        }
        return entities;
    }

    public List<FieldSpecRequest.FieldSpec> toFieldSpecs(CustomerFormConfigurationEntity entity) {
        if (entity.getFieldConfigs() == null || entity.getFieldConfigs().isEmpty()) {
            return Collections.emptyList();
        }
        return entity.getFieldConfigs().stream()
                .map(this::toFieldSpec)
                .toList();
    }

    private FieldSpecRequest.FieldSpec toFieldSpec(FormConfigFieldEntity entity) {
        return FieldSpecRequest.FieldSpec.builder()
                .referenceModel(entity.getReferenceModel())
                .visible(entity.getVisible())
                .shortLabel(entity.getShortLabel())
                .shortLabelI18n(toFieldShortLabelI18n(entity.getTranslations()))
                .longLabel(entity.getLongLabel())
                .longLabelI18n(toFieldLongLabelI18n(entity.getTranslations()))
                .validations(toValidationPayload(entity.getValidations()))
                .build();
    }

    private List<FormConfigFieldValidationEntity> toValidationEntities(FormConfigFieldEntity field, Object validations) {
        List<Map<String, Object>> normalized = normalizeValidations(validations);
        List<FormConfigFieldValidationEntity> entities = new ArrayList<>();
        int index = 0;
        for (Map<String, Object> validation : normalized) {
            for (Map.Entry<String, Object> entry : validation.entrySet()) {
                Map<String, Object> params = entry.getValue() instanceof Map<?, ?> map
                        ? castMap((Map<?, ?>) map)
                        : Collections.emptyMap();
                FormConfigFieldValidationEntity validationEntity = FormConfigFieldValidationEntity.builder()
                        .field(field)
                        .displayOrder(index++)
                        .validationType(entry.getKey())
                        .valueJson(writeJson(params.get("value")))
                        .pattern(params.get("pattern") != null ? params.get("pattern").toString() : null)
                        .message(params.get("message") != null ? params.get("message").toString() : null)
                        .translations(new LinkedHashSet<>())
                        .build();
                validationEntity.getTranslations().addAll(toValidationTranslations(params.get("messageI18n"), validationEntity));
                entities.add(validationEntity);
            }
        }
        return entities;
    }

    private Object toValidationPayload(Collection<FormConfigFieldValidationEntity> validations) {
        if (validations == null || validations.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        validations.stream()
                .sorted(java.util.Comparator.comparing(v -> v.getDisplayOrder() != null ? v.getDisplayOrder() : Integer.MAX_VALUE))
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

    private List<FormConfigFieldTranslationEntity> toFieldTranslations(FormConfigFieldEntity field,
                                                                       Map<String, String> shortLabelI18n,
                                                                       Map<String, String> longLabelI18n) {
        TreeSet<String> locales = new TreeSet<>();
        if (shortLabelI18n != null) {
            locales.addAll(shortLabelI18n.keySet());
        }
        if (longLabelI18n != null) {
            locales.addAll(longLabelI18n.keySet());
        }

        List<FormConfigFieldTranslationEntity> translations = new ArrayList<>(locales.size());
        for (String locale : locales) {
            if (locale == null || locale.isBlank()) {
                continue;
            }
            translations.add(FormConfigFieldTranslationEntity.builder()
                    .field(field)
                    .locale(locale)
                    .shortLabel(shortLabelI18n != null ? shortLabelI18n.get(locale) : null)
                    .longLabel(longLabelI18n != null ? longLabelI18n.get(locale) : null)
                    .build());
        }
        return translations;
    }

    private List<FormConfigFieldValidationTranslationEntity> toValidationTranslations(Object messageI18n, FormConfigFieldValidationEntity validation) {
        if (!(messageI18n instanceof Map<?, ?> map) || map.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, String> values = objectMapper.convertValue(map, new TypeReference<Map<String, String>>() {});
        List<FormConfigFieldValidationTranslationEntity> translations = new ArrayList<>(values.size());
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }
            translations.add(FormConfigFieldValidationTranslationEntity.builder()
                    .validation(validation)
                    .locale(entry.getKey())
                    .message(entry.getValue())
                    .build());
        }
        return translations;
    }

    private Map<String, String> toFieldShortLabelI18n(Collection<FormConfigFieldTranslationEntity> translations) {
        Map<String, String> labels = new LinkedHashMap<>();
        if (translations == null) {
            return labels;
        }
        for (FormConfigFieldTranslationEntity translation : translations) {
            if (translation.getLocale() != null && translation.getShortLabel() != null) {
                labels.put(translation.getLocale(), translation.getShortLabel());
            }
        }
        return labels;
    }

    private Map<String, String> toFieldLongLabelI18n(Collection<FormConfigFieldTranslationEntity> translations) {
        Map<String, String> labels = new LinkedHashMap<>();
        if (translations == null) {
            return labels;
        }
        for (FormConfigFieldTranslationEntity translation : translations) {
            if (translation.getLocale() != null && translation.getLongLabel() != null) {
                labels.put(translation.getLocale(), translation.getLongLabel());
            }
        }
        return labels;
    }

    private Map<String, String> toValidationMessageI18n(Collection<FormConfigFieldValidationTranslationEntity> translations) {
        Map<String, String> messages = new LinkedHashMap<>();
        if (translations == null) {
            return messages;
        }
        for (FormConfigFieldValidationTranslationEntity translation : translations) {
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
}

package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.dto.FieldDefinition;
import com.example.dynamicform.platform.dto.FormDefinition;
import com.example.dynamicform.platform.dto.UiMetadata;
import com.example.dynamicform.platform.dto.ValidationDefinition;
import com.example.dynamicform.platform.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.dto.metadata.RawDomainModel;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ResponseLocalizationService {

    private final ObjectMapper objectMapper;

    public ResponseLocalizationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String resolveLanguage(String languageHeader, String acceptLanguageHeader) {
        String direct = normalizeLanguage(languageHeader);
        if (direct != null) {
            return direct;
        }
        if (acceptLanguageHeader == null || acceptLanguageHeader.isBlank()) {
            return null;
        }
        String candidate = acceptLanguageHeader.split(",")[0].trim();
        return normalizeLanguage(candidate);
    }

    public RawFormMetadata localizeRawMetadata(RawFormMetadata metadata, String language) {
        if (metadata == null || language == null || language.isBlank()) {
            return metadata;
        }
        RawFormMetadata copy = objectMapper.convertValue(metadata, RawFormMetadata.class);
        localizeDomainModel(copy.getDomainModel(), language);
        return copy;
    }

    public RawFormMetadata prepareRawMetadataResponse(RawFormMetadata metadata, String language) {
        RawFormMetadata prepared = (language == null || language.isBlank())
                ? objectMapper.convertValue(metadata, RawFormMetadata.class)
                : localizeRawMetadata(metadata, language);
        stripI18n(prepared != null ? prepared.getDomainModel() : null);
        return prepared;
    }

    public FormDefinition localizeFormDefinition(FormDefinition formDefinition, String language) {
        if (formDefinition == null || language == null || language.isBlank()) {
            return formDefinition;
        }
        FormDefinition copy = objectMapper.convertValue(formDefinition, FormDefinition.class);
        if (copy.getRawMetadata() != null) {
            copy.setRawMetadata(localizeRawMetadata(copy.getRawMetadata(), language));
        }
        if (copy.getFields() != null) {
            copy.setFields(localizeFields(copy.getFields(), language));
        }
        return copy;
    }

    public FormDefinition prepareFormDefinitionResponse(FormDefinition formDefinition, String language) {
        FormDefinition prepared = (language == null || language.isBlank())
                ? objectMapper.convertValue(formDefinition, FormDefinition.class)
                : localizeFormDefinition(formDefinition, language);
        stripFieldI18n(prepared != null ? prepared.getFields() : null);
        if (prepared != null && prepared.getRawMetadata() != null) {
            stripI18n(prepared.getRawMetadata().getDomainModel());
        }
        return prepared;
    }

    private List<FieldDefinition> localizeFields(List<FieldDefinition> fields, String language) {
        List<FieldDefinition> localized = new ArrayList<>(fields.size());
        for (FieldDefinition field : fields) {
            if (field == null) {
                continue;
            }
            FieldDefinition copy = objectMapper.convertValue(field, FieldDefinition.class);
            String shortLabel = resolveLocalized(copy.getShortLabel(), copy.getShortLabelI18n(), language);
            String longLabel = resolveLocalized(copy.getLongLabel(), copy.getLongLabelI18n(), language);
            copy.setShortLabel(shortLabel);
            copy.setLongLabel(longLabel);

            if (copy.getUiMetadata() != null) {
                UiMetadata uiMetadata = objectMapper.convertValue(copy.getUiMetadata(), UiMetadata.class);
                String localizedLabel = firstNonBlank(longLabel, shortLabel, uiMetadata.getLabel());
                uiMetadata.setLabel(localizedLabel);
                copy.setUiMetadata(uiMetadata);
            }

            if (copy.getValidations() != null) {
                List<ValidationDefinition> validations = new ArrayList<>(copy.getValidations().size());
                for (ValidationDefinition validation : copy.getValidations()) {
                    validations.add(localizeValidation(validation, language));
                }
                copy.setValidations(validations);
            }

            if (copy.getNestedFields() != null) {
                copy.setNestedFields(localizeFields(copy.getNestedFields(), language));
            }
            localized.add(copy);
        }
        return localized;
    }

    private ValidationDefinition localizeValidation(ValidationDefinition validation, String language) {
        ValidationDefinition copy = objectMapper.convertValue(validation, ValidationDefinition.class);
        Map<String, Object> params = copy.getParameters() != null
                ? new LinkedHashMap<>(copy.getParameters())
                : new LinkedHashMap<>();
        String localizedMessage = resolveLocalized(copy.getMessage(), extractStringMap(params.get("messageI18n")), language);
        copy.setMessage(localizedMessage);
        if (localizedMessage != null) {
            params.put("message", localizedMessage);
        }
        copy.setParameters(params);
        return copy;
    }

    private void stripFieldI18n(List<FieldDefinition> fields) {
        if (fields == null) {
            return;
        }
        for (FieldDefinition field : fields) {
            field.setShortLabelI18n(null);
            field.setLongLabelI18n(null);
            if (field.getValidations() != null) {
                for (ValidationDefinition validation : field.getValidations()) {
                    if (validation.getParameters() != null) {
                        validation.getParameters().remove("messageI18n");
                    }
                }
            }
            if (field.getNestedFields() != null) {
                stripFieldI18n(field.getNestedFields());
            }
        }
    }

    private void stripI18n(RawDomainModel domainModel) {
        if (domainModel == null || domainModel.getAttributes() == null) {
            return;
        }
        for (RawDomainAttribute attribute : domainModel.getAttributes()) {
            attribute.setShortLabelI18n(null);
            attribute.setLongLabelI18n(null);
            if (attribute.getValidations() != null) {
                for (Map<String, Object> validation : attribute.getValidations()) {
                    for (Map.Entry<String, Object> entry : validation.entrySet()) {
                        if (entry.getValue() instanceof Map<?, ?> rawParams) {
                            Map<String, Object> params = new LinkedHashMap<>();
                            rawParams.forEach((key, value) -> {
                                if (!"messageI18n".equals(String.valueOf(key))) {
                                    params.put(String.valueOf(key), value);
                                }
                            });
                            entry.setValue(params);
                        }
                    }
                }
            }
            stripI18n(attribute.getDomainModel());
        }
    }

    private void localizeDomainModel(RawDomainModel domainModel, String language) {
        if (domainModel == null || domainModel.getAttributes() == null) {
            return;
        }
        for (RawDomainAttribute attribute : domainModel.getAttributes()) {
            attribute.setShortLabel(resolveLocalized(attribute.getShortLabel(), attribute.getShortLabelI18n(), language));
            attribute.setLongLabel(resolveLocalized(attribute.getLongLabel(), attribute.getLongLabelI18n(), language));
            if (attribute.getValidations() != null) {
                for (Map<String, Object> validation : attribute.getValidations()) {
                    for (Map.Entry<String, Object> entry : validation.entrySet()) {
                        if (entry.getValue() instanceof Map<?, ?> rawParams) {
                            Map<String, Object> params = new LinkedHashMap<>();
                            rawParams.forEach((key, value) -> params.put(String.valueOf(key), value));
                            String localizedMessage = resolveLocalized(
                                    asString(params.get("message")),
                                    extractStringMap(params.get("messageI18n")),
                                    language);
                            if (localizedMessage != null) {
                                params.put("message", localizedMessage);
                            }
                            entry.setValue(params);
                        }
                    }
                }
            }
            localizeDomainModel(attribute.getDomainModel(), language);
        }
    }

    private String resolveLocalized(String defaultValue, Map<String, String> translations, String language) {
        if (translations == null || translations.isEmpty() || language == null || language.isBlank()) {
            return defaultValue;
        }
        String normalized = normalizeLanguage(language);
        if (normalized == null) {
            return defaultValue;
        }
        if (translations.containsKey(normalized) && translations.get(normalized) != null) {
            return translations.get(normalized);
        }
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            String key = normalizeLanguage(entry.getKey());
            if (key != null && key.equals(normalized) && entry.getValue() != null) {
                return entry.getValue();
            }
        }
        String baseLanguage = normalized.contains("-") ? normalized.substring(0, normalized.indexOf('-')) : normalized;
        if (translations.containsKey(baseLanguage) && translations.get(baseLanguage) != null) {
            return translations.get(baseLanguage);
        }
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            String key = normalizeLanguage(entry.getKey());
            if (key != null && key.startsWith(baseLanguage) && entry.getValue() != null) {
                return entry.getValue();
            }
        }
        return defaultValue;
    }

    private Map<String, String> extractStringMap(Object raw) {
        if (!(raw instanceof Map<?, ?> map) || map.isEmpty()) {
            return null;
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return result;
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return null;
        }
        String cleaned = language.trim().replace('_', '-');
        int qualityIndex = cleaned.indexOf(';');
        if (qualityIndex >= 0) {
            cleaned = cleaned.substring(0, qualityIndex);
        }
        cleaned = cleaned.trim();
        if (cleaned.isBlank()) {
            return null;
        }
        return Locale.forLanguageTag(cleaned).toLanguageTag().toLowerCase(Locale.ROOT);
    }

    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

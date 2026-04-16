package com.example.dynamicform.platform.service.impl;

import com.example.dynamicform.platform.api.dto.FieldDefinition;
import com.example.dynamicform.platform.api.dto.FormDefinition;
import com.example.dynamicform.platform.api.dto.UiMetadata;
import com.example.dynamicform.platform.api.dto.ValidationDefinition;
import com.example.dynamicform.platform.api.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.api.dto.metadata.RawDomainModel;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.service.ResponseLocalizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ResponseLocalizationServiceImpl implements ResponseLocalizationService {

    private final ObjectMapper objectMapper;

    public ResponseLocalizationServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
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

    @Override
    public RawFormMetadata localizeRawMetadata(RawFormMetadata metadata, Long languageId) {
        if (metadata == null || languageId == null) {
            return metadata;
        }
        RawFormMetadata copy = objectMapper.convertValue(metadata, RawFormMetadata.class);
        localizeDomainModel(copy.getDomainModel(), languageId);
        return copy;
    }

    @Override
    public RawFormMetadata prepareRawMetadataResponse(RawFormMetadata metadata, Long languageId) {
        RawFormMetadata prepared = (languageId == null)
                ? objectMapper.convertValue(metadata, RawFormMetadata.class)
                : localizeRawMetadata(metadata, languageId);
        stripI18n(prepared != null ? prepared.getDomainModel() : null);
        return prepared;
    }

    @Override
    public FormDefinition localizeFormDefinition(FormDefinition formDefinition, Long languageId) {
        if (formDefinition == null || languageId == null) {
            return formDefinition;
        }
        FormDefinition copy = objectMapper.convertValue(formDefinition, FormDefinition.class);
        if (copy.getRawMetadata() != null) {
            copy.setRawMetadata(localizeRawMetadata(copy.getRawMetadata(), languageId));
        }
        if (copy.getFields() != null) {
            copy.setFields(localizeFields(copy.getFields(), languageId));
        }
        return copy;
    }

    @Override
    public FormDefinition prepareFormDefinitionResponse(FormDefinition formDefinition, Long languageId) {
        FormDefinition prepared = (languageId == null)
                ? objectMapper.convertValue(formDefinition, FormDefinition.class)
                : localizeFormDefinition(formDefinition, languageId);
        stripFieldI18n(prepared != null ? prepared.getFields() : null);
        if (prepared != null && prepared.getRawMetadata() != null) {
            stripI18n(prepared.getRawMetadata().getDomainModel());
        }
        return prepared;
    }

    private List<FieldDefinition> localizeFields(List<FieldDefinition> fields, Long languageId) {
        List<FieldDefinition> localized = new ArrayList<>(fields.size());
        for (FieldDefinition field : fields) {
            if (field == null) {
                continue;
            }
            FieldDefinition copy = objectMapper.convertValue(field, FieldDefinition.class);
            String shortLabel = resolveLocalized(copy.getShortLabel(), extractLongMap(copy.getShortLabelI18n()), languageId);
            String longLabel = resolveLocalized(copy.getLongLabel(), extractLongMap(copy.getLongLabelI18n()), languageId);
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
                    validations.add(localizeValidation(validation, languageId));
                }
                copy.setValidations(validations);
            }

            if (copy.getNestedFields() != null) {
                copy.setNestedFields(localizeFields(copy.getNestedFields(), languageId));
            }
            localized.add(copy);
        }
        return localized;
    }

    private ValidationDefinition localizeValidation(ValidationDefinition validation, Long languageId) {
        ValidationDefinition copy = objectMapper.convertValue(validation, ValidationDefinition.class);
        Map<String, Object> params = copy.getParameters() != null
                ? new LinkedHashMap<>(copy.getParameters())
                : new LinkedHashMap<>();
        String localizedMessage = resolveLocalized(copy.getMessage(), extractLongMap(params.get("messageI18n")), languageId);
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

    private void localizeDomainModel(RawDomainModel domainModel, Long languageId) {
        if (domainModel == null || domainModel.getAttributes() == null) {
            return;
        }
        for (RawDomainAttribute attribute : domainModel.getAttributes()) {
            attribute.setShortLabel(resolveLocalized(attribute.getShortLabel(), extractLongMap(attribute.getShortLabelI18n()), languageId));
            attribute.setLongLabel(resolveLocalized(attribute.getLongLabel(), extractLongMap(attribute.getLongLabelI18n()), languageId));
            if (attribute.getValidations() != null) {
                for (Map<String, Object> validation : attribute.getValidations()) {
                    for (Map.Entry<String, Object> entry : validation.entrySet()) {
                        if (entry.getValue() instanceof Map<?, ?> rawParams) {
                            Map<String, Object> params = new LinkedHashMap<>();
                            rawParams.forEach((key, value) -> params.put(String.valueOf(key), value));
                            String localizedMessage = resolveLocalized(
                                    asString(params.get("message")),
                                    extractLongMap(params.get("messageI18n")),
                                    languageId);
                            if (localizedMessage != null) {
                                params.put("message", localizedMessage);
                            }
                            entry.setValue(params);
                        }
                    }
                }
            }
            localizeDomainModel(attribute.getDomainModel(), languageId);
        }
    }

    private String resolveLocalized(String defaultValue, Map<Long, String> translations, Long languageId) {
        if (translations == null || translations.isEmpty() || languageId == null) {
            return defaultValue;
        }
        if (translations.containsKey(languageId) && translations.get(languageId) != null) {
            return translations.get(languageId);
        }
        return defaultValue;
    }

    private Map<Long, String> extractLongMap(Object raw) {
        if (!(raw instanceof Map<?, ?> map) || map.isEmpty()) {
            return null;
        }
        Map<Long, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                try {
                    Long key = Long.valueOf(entry.getKey().toString());
                    result.put(key, entry.getValue().toString());
                } catch (NumberFormatException e) {
                    // ignore non-long keys
                }
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

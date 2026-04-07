package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.service.ResponseLocalizationService;
import com.example.dynamicform.product.dto.RSPWiseDocumentSetupRequest;
import com.example.dynamicform.product.dto.RSPWiseDocumentSetupResponse;
import com.example.dynamicform.product.dto.RSPWiseDocumentTranslationRequest;
import com.example.dynamicform.product.entity.DocumentEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldConfigEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldTranslationEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldValidationEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentFieldValidationTranslationEntity;
import com.example.dynamicform.product.entity.RSPWiseDocumentSetupEntity;
import com.example.dynamicform.product.repository.DocumentRepository;
import com.example.dynamicform.product.repository.RSPWiseDocumentSetupRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RSPWiseDocumentSetupServiceImpl implements RSPWiseDocumentSetupService {

    private final RSPWiseDocumentSetupRepository repository;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final RSPWiseDocumentFieldConfigAdapter fieldConfigAdapter;
    private final ResponseLocalizationService responseLocalizationService;

    @Override
    @Transactional
    public RSPWiseDocumentSetupResponse create(RSPWiseDocumentSetupRequest request, String language) {
        DocumentEntity document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + request.getDocumentId()));

        List<RSPWiseDocumentSetupRequest.FieldSpec> fields = defaultFields(request.getFields());
        Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldMap = toFieldMap(fields);
        boolean secondaryContentRequired = isRequired(fieldMap, RSPWiseDocumentReferenceModels.SECONDARY_CONTENT_REQUIRED);
        boolean backRequired = secondaryContentRequired;
        RSPWiseDocumentSetupEntity entity = RSPWiseDocumentSetupEntity.builder()
                .document(document)
                .isPrimary(request.getIsPrimary())
                .rspId(request.getRspId())
                .metadataJson(writeMetadata(fields))
                .isDocumentNumberRequired(isRequired(fieldMap, RSPWiseDocumentReferenceModels.DOCUMENT_NUMBER_REQUIRED))
                .isBackRequired(backRequired)
                .isIssuedCountryRequired(isRequired(fieldMap, RSPWiseDocumentReferenceModels.ISSUED_COUNTRY_REQUIRED))
                .isExpiryDateRequired(isRequired(fieldMap, RSPWiseDocumentReferenceModels.EXPIRY_DATE_REQUIRED))
                .isPrimaryContentRequired(isRequired(fieldMap, RSPWiseDocumentReferenceModels.PRIMARY_CONTENT_REQUIRED))
                .isSecondaryContentRequired(secondaryContentRequired || backRequired)
                .fieldConfigs(new java.util.LinkedHashSet<>())
                .build();
        entity.getFieldConfigs().addAll(fieldConfigAdapter.toEntities(entity, fields));

        RSPWiseDocumentSetupEntity saved = repository.save(entity);
        return mapToResponse(saved, language);
    }

    @Override
    @Transactional
    public RSPWiseDocumentSetupResponse update(UUID id, RSPWiseDocumentSetupRequest request, String language) {
        RSPWiseDocumentSetupEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found with id: " + id));

        DocumentEntity document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + request.getDocumentId()));

        List<RSPWiseDocumentSetupRequest.FieldSpec> fields = defaultFields(request.getFields());
        Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldMap = toFieldMap(fields);
        boolean secondaryContentRequired = isRequired(fieldMap, RSPWiseDocumentReferenceModels.SECONDARY_CONTENT_REQUIRED);
        boolean backRequired = secondaryContentRequired;
        entity.setDocument(document);
        entity.setIsPrimary(request.getIsPrimary());
        entity.setRspId(request.getRspId());
        entity.setMetadataJson(writeMetadata(fields));
        entity.setDocumentNumberRequired(isRequired(fieldMap, RSPWiseDocumentReferenceModels.DOCUMENT_NUMBER_REQUIRED));
        entity.setBackRequired(backRequired);
        entity.setIssuedCountryRequired(isRequired(fieldMap, RSPWiseDocumentReferenceModels.ISSUED_COUNTRY_REQUIRED));
        entity.setExpiryDateRequired(isRequired(fieldMap, RSPWiseDocumentReferenceModels.EXPIRY_DATE_REQUIRED));
        entity.setPrimaryContentRequired(isRequired(fieldMap, RSPWiseDocumentReferenceModels.PRIMARY_CONTENT_REQUIRED));
        entity.setSecondaryContentRequired(secondaryContentRequired || backRequired);
        entity.getFieldConfigs().clear();
        entity.getFieldConfigs().addAll(fieldConfigAdapter.toEntities(entity, fields));

        RSPWiseDocumentSetupEntity updated = repository.save(entity);
        return mapToResponse(updated, language);
    }

    @Override
    @Transactional
    public RSPWiseDocumentSetupResponse updateTranslations(RSPWiseDocumentTranslationRequest request, String language) {
        RSPWiseDocumentSetupEntity entity = resolveTranslationTarget(request);
        Map<String, RSPWiseDocumentFieldConfigEntity> fieldsByReferenceModel = new HashMap<>();
        for (RSPWiseDocumentFieldConfigEntity fieldConfig : entity.getFieldConfigs()) {
            if (fieldConfig.getReferenceModel() != null) {
                fieldsByReferenceModel.put(RSPWiseDocumentReferenceModels.normalize(fieldConfig.getReferenceModel()), fieldConfig);
            }
        }

        if (request.getFields() != null) {
            for (RSPWiseDocumentTranslationRequest.FieldTranslation fieldTranslation : request.getFields()) {
                if (fieldTranslation == null || fieldTranslation.getReferenceModel() == null) {
                    continue;
                }
                RSPWiseDocumentFieldConfigEntity fieldEntity = fieldsByReferenceModel.get(
                        RSPWiseDocumentReferenceModels.normalize(fieldTranslation.getReferenceModel()));
                if (fieldEntity == null) {
                    continue;
                }
                mergeFieldTranslations(fieldEntity, fieldTranslation);
            }
        }

        entity.setMetadataJson(writeMetadata(fieldConfigAdapter.toFieldSpecs(entity)));
        return mapToResponse(repository.save(entity), language);
    }

    @Override
    @Transactional(readOnly = true)
    public RSPWiseDocumentSetupResponse getById(UUID id, String language) {
        RSPWiseDocumentSetupEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found with id: " + id));
        return mapToResponse(entity, language);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RSPWiseDocumentSetupResponse> getAll(String language) {
        return repository.findAll().stream()
                .map(entity -> mapToResponse(entity, language))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("RSPWiseDocumentSetup not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private RSPWiseDocumentSetupResponse mapToResponse(RSPWiseDocumentSetupEntity entity, String language) {
        List<RSPWiseDocumentSetupRequest.FieldSpec> fields = fieldConfigAdapter.toFieldSpecs(entity);
        Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldMap = toFieldMap(fields);
        return RSPWiseDocumentSetupResponse.builder()
                .id(entity.getId())
                .documentId(entity.getDocument() != null ? entity.getDocument().getId() : null)
                .documentName(entity.getDocument() != null ? entity.getDocument().getDocumentName() : null)
                .isPrimary(entity.getIsPrimary())
                .rspId(entity.getRspId())
                .fields(toResponseFields(fields, language))
                .isDocumentNumberRequired(resolveRequired(fieldMap, RSPWiseDocumentReferenceModels.DOCUMENT_NUMBER_REQUIRED, entity.isDocumentNumberRequired()))
                .isBackRequired(resolveRequired(fieldMap, RSPWiseDocumentReferenceModels.SECONDARY_CONTENT_REQUIRED, entity.isBackRequired()))
                .isIssuedCountryRequired(resolveRequired(fieldMap, RSPWiseDocumentReferenceModels.ISSUED_COUNTRY_REQUIRED, entity.isIssuedCountryRequired()))
                .isExpiryDateRequired(resolveRequired(fieldMap, RSPWiseDocumentReferenceModels.EXPIRY_DATE_REQUIRED, entity.isExpiryDateRequired()))
                .isPrimaryContentRequired(resolveRequired(fieldMap, RSPWiseDocumentReferenceModels.PRIMARY_CONTENT_REQUIRED, entity.isPrimaryContentRequired()))
                .isSecondaryContentRequired(resolveRequired(fieldMap, RSPWiseDocumentReferenceModels.SECONDARY_CONTENT_REQUIRED, entity.isSecondaryContentRequired()))
                .build();
    }

    private List<RSPWiseDocumentSetupResponse.FieldSpec> toResponseFields(List<RSPWiseDocumentSetupRequest.FieldSpec> fields, String language) {
        return fields.stream()
                .map(field -> RSPWiseDocumentSetupResponse.FieldSpec.builder()
                        .referenceModel(field.getReferenceModel())
                        .visible(field.getVisible())
                        .shortLabel(resolveLocalized(field.getShortLabel(), field.getShortLabelI18n(), language))
                        .longLabel(resolveLocalized(field.getLongLabel(), field.getLongLabelI18n(), language))
                        .validations(localizeValidationsForResponse(field.getValidations(), language))
                        .build())
                .toList();
    }

    private boolean resolveRequired(Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldMap, String referenceModel, boolean fallback) {
        RSPWiseDocumentSetupRequest.FieldSpec field = fieldMap.get(referenceModel);
        return field != null ? isRequired(field) : fallback;
    }

    private boolean isRequired(Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldMap, String referenceModel) {
        return isRequired(fieldMap.get(referenceModel));
    }

    private Map<String, RSPWiseDocumentSetupRequest.FieldSpec> toFieldMap(List<RSPWiseDocumentSetupRequest.FieldSpec> fields) {
        if (fields.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldMap = new HashMap<>(fields.size());
        for (RSPWiseDocumentSetupRequest.FieldSpec field : fields) {
            if (field != null && field.getReferenceModel() != null) {
                fieldMap.put(RSPWiseDocumentReferenceModels.normalize(field.getReferenceModel()), field);
            }
        }
        return fieldMap;
    }

    private boolean isRequired(RSPWiseDocumentSetupRequest.FieldSpec field) {
        if (field == null || field.getValidations() == null) {
            return false;
        }
        Map<String, Object> validations = asMap(field.getValidations());
        Object required = validations.get("required");
        if (!(required instanceof Map<?, ?> requiredMap)) {
            return false;
        }
        return Boolean.TRUE.equals(requiredMap.get("value"));
    }

    private String writeMetadata(List<RSPWiseDocumentSetupRequest.FieldSpec> fields) {
        try {
            return objectMapper.writeValueAsString(fields);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize RSP wise document metadata", e);
        }
    }
    private List<RSPWiseDocumentSetupRequest.FieldSpec> defaultFields(List<RSPWiseDocumentSetupRequest.FieldSpec> fields) {
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }
        return fields.stream()
                .filter(java.util.Objects::nonNull)
                .map(field -> RSPWiseDocumentSetupRequest.FieldSpec.builder()
                        .referenceModel(RSPWiseDocumentReferenceModels.normalize(field.getReferenceModel()))
                        .visible(field.getVisible())
                        .shortLabel(field.getShortLabel())
                        .shortLabelI18n(field.getShortLabelI18n())
                        .longLabel(field.getLongLabel())
                        .longLabelI18n(field.getLongLabelI18n())
                        .validations(field.getValidations())
                        .build())
                .toList();
    }

    private RSPWiseDocumentSetupEntity resolveTranslationTarget(RSPWiseDocumentTranslationRequest request) {
        if (request.getSetupId() != null) {
            return repository.findDetailedById(request.getSetupId())
                    .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found with id: " + request.getSetupId()));
        }
        if (request.getRspId() != null && request.getDocumentId() != null) {
            return repository.findDetailedByRspIdAndDocument_Id(request.getRspId(), request.getDocumentId())
                    .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found for rspId "
                            + request.getRspId() + " and documentId " + request.getDocumentId()));
        }
        throw new IllegalArgumentException("Either setupId or rspId + documentId must be provided");
    }

    private void mergeFieldTranslations(RSPWiseDocumentFieldConfigEntity fieldEntity,
                                        RSPWiseDocumentTranslationRequest.FieldTranslation fieldTranslation) {
        Map<String, RSPWiseDocumentFieldTranslationEntity> translationsByLocale = new HashMap<>();
        for (RSPWiseDocumentFieldTranslationEntity translation : fieldEntity.getTranslations()) {
            if (translation.getLocale() != null) {
                translationsByLocale.put(translation.getLocale(), translation);
            }
        }
        mergeLabelTranslations(fieldEntity, translationsByLocale, fieldTranslation.getShortLabelI18n(), true);
        mergeLabelTranslations(fieldEntity, translationsByLocale, fieldTranslation.getLongLabelI18n(), false);

        Map<String, Object> validations = asMap(fieldTranslation.getValidations());
        if (validations.isEmpty()) {
            return;
        }
        Map<String, RSPWiseDocumentFieldValidationEntity> validationsByType = new LinkedHashMap<>();
        for (RSPWiseDocumentFieldValidationEntity validation : fieldEntity.getValidations()) {
            if (validation.getValidationType() != null) {
                validationsByType.put(validation.getValidationType(), validation);
            }
        }
        for (Map.Entry<String, Object> entry : validations.entrySet()) {
            RSPWiseDocumentFieldValidationEntity validationEntity = validationsByType.get(entry.getKey());
            if (validationEntity == null || !(entry.getValue() instanceof Map<?, ?> params)) {
                continue;
            }
            Object messageI18n = params.get("messageI18n");
            if (messageI18n instanceof Map<?, ?> messageMap) {
                mergeValidationTranslations(validationEntity, objectMapper.convertValue(messageMap, new TypeReference<Map<String, String>>() {}));
            }
        }
    }

    private void mergeLabelTranslations(RSPWiseDocumentFieldConfigEntity fieldEntity,
                                        Map<String, RSPWiseDocumentFieldTranslationEntity> translationsByLocale,
                                        Map<String, String> values,
                                        boolean shortLabel) {
        if (values == null || values.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }
            RSPWiseDocumentFieldTranslationEntity translation = translationsByLocale.computeIfAbsent(entry.getKey(), locale -> {
                RSPWiseDocumentFieldTranslationEntity created = RSPWiseDocumentFieldTranslationEntity.builder()
                        .fieldConfig(fieldEntity)
                        .locale(locale)
                        .build();
                fieldEntity.getTranslations().add(created);
                return created;
            });
            if (shortLabel) {
                translation.setShortLabel(entry.getValue());
            } else {
                translation.setLongLabel(entry.getValue());
            }
        }
    }

    private void mergeValidationTranslations(RSPWiseDocumentFieldValidationEntity validationEntity,
                                             Map<String, String> messageI18n) {
        if (messageI18n == null || messageI18n.isEmpty()) {
            return;
        }
        Map<String, RSPWiseDocumentFieldValidationTranslationEntity> translationsByLocale = new HashMap<>();
        for (RSPWiseDocumentFieldValidationTranslationEntity translation : validationEntity.getTranslations()) {
            if (translation.getLocale() != null) {
                translationsByLocale.put(translation.getLocale(), translation);
            }
        }
        for (Map.Entry<String, String> entry : messageI18n.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }
            RSPWiseDocumentFieldValidationTranslationEntity translation = translationsByLocale.computeIfAbsent(entry.getKey(), locale -> {
                RSPWiseDocumentFieldValidationTranslationEntity created = RSPWiseDocumentFieldValidationTranslationEntity.builder()
                        .validation(validationEntity)
                        .locale(locale)
                        .build();
                validationEntity.getTranslations().add(created);
                return created;
            });
            translation.setMessage(entry.getValue());
        }
    }

    private Map<String, Object> asMap(Object source) {
        if (!(source instanceof Map<?, ?> map) || map.isEmpty()) {
            return Collections.emptyMap();
        }
        return objectMapper.convertValue(map, new TypeReference<Map<String, Object>>() {});
    }

    private Object localizeValidationsForResponse(Object source, String language) {
        if (!(source instanceof Map<?, ?> rawMap) || rawMap.isEmpty()) {
            return source;
        }
        Map<String, Object> validations = objectMapper.convertValue(rawMap, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> response = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : validations.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> rawParams)) {
                response.put(entry.getKey(), entry.getValue());
                continue;
            }
            Map<String, Object> params = objectMapper.convertValue(rawParams, new TypeReference<Map<String, Object>>() {});
            String localizedMessage = resolveLocalized(
                    params.get("message") instanceof String str ? str : null,
                    extractStringMap(params.get("messageI18n")),
                    language);
            if (localizedMessage != null) {
                params.put("message", localizedMessage);
            }
            params.remove("messageI18n");
            response.put(entry.getKey(), params);
        }
        return response;
    }

    private Map<String, String> extractStringMap(Object source) {
        if (!(source instanceof Map<?, ?> map) || map.isEmpty()) {
            return Collections.emptyMap();
        }
        return objectMapper.convertValue(map, new TypeReference<Map<String, String>>() {});
    }

    private String resolveLocalized(String defaultValue, Map<String, String> translations, String language) {
        if (translations == null || translations.isEmpty() || language == null || language.isBlank()) {
            return defaultValue;
        }
        String normalized = responseLocalizationService.resolveLanguage(language, null);
        if (normalized == null) {
            return defaultValue;
        }
        if (translations.containsKey(normalized) && translations.get(normalized) != null) {
            return translations.get(normalized);
        }
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            String key = responseLocalizationService.resolveLanguage(entry.getKey(), null);
            if (normalized.equals(key) && entry.getValue() != null) {
                return entry.getValue();
            }
        }
        return defaultValue;
    }
}

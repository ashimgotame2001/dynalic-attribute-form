package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.service.ResponseLocalizationService;
import com.example.dynamicform.product.dto.DocumentSetupRequest;
import com.example.dynamicform.product.dto.DocumentSetupResponse;
import com.example.dynamicform.product.dto.DocumentTranslationRequest;
import com.example.dynamicform.product.entity.DocumentEntity;
import com.example.dynamicform.product.entity.DocumentFieldConfigEntity;
import com.example.dynamicform.product.entity.DocumentFieldTranslationEntity;
import com.example.dynamicform.product.entity.DocumentFieldValidationEntity;
import com.example.dynamicform.product.entity.DocumentFieldValidationTranslationEntity;
import com.example.dynamicform.product.entity.DocumentSetupEntity;
import com.example.dynamicform.product.repository.DocumentRepository;
import com.example.dynamicform.product.repository.DocumentSetupRepository;
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
public class DocumentSetupServiceImpl implements DocumentSetupService {

    private final DocumentSetupRepository repository;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;
    private final DocumentFieldConfigAdapter fieldConfigAdapter;
    private final ResponseLocalizationService responseLocalizationService;

    @Override
    @Transactional
    public DocumentSetupResponse create(DocumentSetupRequest request, Long languageId) {
        DocumentEntity document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + request.getDocumentId()));

        List<DocumentSetupRequest.FieldSpec> fields = defaultFields(request.getFields());
        Map<String, DocumentSetupRequest.FieldSpec> fieldMap = toFieldMap(fields);
        boolean secondaryContentRequired = isRequired(fieldMap, DocumentReferenceModels.SECONDARY_CONTENT_REQUIRED);
        boolean backRequired = secondaryContentRequired;
        DocumentSetupEntity entity = DocumentSetupEntity.builder()
                .document(document)
                .isPrimary(request.getIsPrimary())
                .metadataJson(writeMetadata(fields))
                .isDocumentNumberRequired(isRequired(fieldMap, DocumentReferenceModels.DOCUMENT_NUMBER_REQUIRED))
                .isBackRequired(backRequired)
                .isIssuedCountryRequired(isRequired(fieldMap, DocumentReferenceModels.ISSUED_COUNTRY_REQUIRED))
                .isExpiryDateRequired(isRequired(fieldMap, DocumentReferenceModels.EXPIRY_DATE_REQUIRED))
                .isPrimaryContentRequired(isRequired(fieldMap, DocumentReferenceModels.PRIMARY_CONTENT_REQUIRED))
                .isSecondaryContentRequired(secondaryContentRequired || backRequired)
                .fieldConfigs(new java.util.LinkedHashSet<>())
                .build();
        entity.getFieldConfigs().addAll(fieldConfigAdapter.toEntities(entity, fields));

        DocumentSetupEntity saved = repository.save(entity);
        return mapToResponse(saved, languageId);
    }

    @Override
    @Transactional
    public DocumentSetupResponse update(UUID id, DocumentSetupRequest request, Long languageId) {
        DocumentSetupEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found with id: " + id));

        DocumentEntity document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + request.getDocumentId()));

        List<DocumentSetupRequest.FieldSpec> fields = defaultFields(request.getFields());
        Map<String, DocumentSetupRequest.FieldSpec> fieldMap = toFieldMap(fields);
        boolean secondaryContentRequired = isRequired(fieldMap, DocumentReferenceModels.SECONDARY_CONTENT_REQUIRED);
        boolean backRequired = secondaryContentRequired;
        entity.setDocument(document);
        entity.setIsPrimary(request.getIsPrimary());
        entity.setMetadataJson(writeMetadata(fields));
        entity.setDocumentNumberRequired(isRequired(fieldMap, DocumentReferenceModels.DOCUMENT_NUMBER_REQUIRED));
        entity.setBackRequired(backRequired);
        entity.setIssuedCountryRequired(isRequired(fieldMap, DocumentReferenceModels.ISSUED_COUNTRY_REQUIRED));
        entity.setExpiryDateRequired(isRequired(fieldMap, DocumentReferenceModels.EXPIRY_DATE_REQUIRED));
        entity.setPrimaryContentRequired(isRequired(fieldMap, DocumentReferenceModels.PRIMARY_CONTENT_REQUIRED));
        entity.setSecondaryContentRequired(secondaryContentRequired || backRequired);
        entity.getFieldConfigs().clear();
        entity.getFieldConfigs().addAll(fieldConfigAdapter.toEntities(entity, fields));

        DocumentSetupEntity updated = repository.save(entity);
        return mapToResponse(updated, languageId);
    }

    @Override
    @Transactional
    public DocumentSetupResponse updateTranslations(DocumentTranslationRequest request, Long languageId) {
        DocumentSetupEntity entity = resolveTranslationTarget(request);
        Map<String, DocumentFieldConfigEntity> fieldsByReferenceModel = new HashMap<>();
        for (DocumentFieldConfigEntity fieldConfig : entity.getFieldConfigs()) {
            if (fieldConfig.getReferenceModel() != null) {
                fieldsByReferenceModel.put(DocumentReferenceModels.normalize(fieldConfig.getReferenceModel()), fieldConfig);
            }
        }

        if (request.getFields() != null) {
            for (DocumentTranslationRequest.FieldTranslation fieldTranslation : request.getFields()) {
                if (fieldTranslation == null || fieldTranslation.getReferenceModel() == null) {
                    continue;
                }
                DocumentFieldConfigEntity fieldEntity = fieldsByReferenceModel.get(
                        DocumentReferenceModels.normalize(fieldTranslation.getReferenceModel()));
                if (fieldEntity == null) {
                    continue;
                }
                mergeFieldTranslations(fieldEntity, fieldTranslation);
            }
        }

        entity.setMetadataJson(writeMetadata(fieldConfigAdapter.toFieldSpecs(entity)));
        return mapToResponse(repository.save(entity), languageId);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentSetupResponse getById(UUID id, Long languageId) {
        DocumentSetupEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found with id: " + id));
        return mapToResponse(entity, languageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentSetupResponse> getAll(Long languageId) {
        return repository.findAll().stream()
                .map(entity -> mapToResponse(entity, languageId))
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

    private DocumentSetupResponse mapToResponse(DocumentSetupEntity entity, Long languageId) {
        List<DocumentSetupRequest.FieldSpec> fields = fieldConfigAdapter.toFieldSpecs(entity);
        Map<String, DocumentSetupRequest.FieldSpec> fieldMap = toFieldMap(fields);
        return DocumentSetupResponse.builder()
                .id(entity.getId())
                .documentId(entity.getDocument() != null ? entity.getDocument().getId() : null)
                .documentName(entity.getDocument() != null ? entity.getDocument().getDocumentName() : null)
                .isPrimary(entity.getIsPrimary())
                .fields(toResponseFields(fields, languageId))
                .isDocumentNumberRequired(resolveRequired(fieldMap, DocumentReferenceModels.DOCUMENT_NUMBER_REQUIRED, entity.isDocumentNumberRequired()))
                .isBackRequired(resolveRequired(fieldMap, DocumentReferenceModels.SECONDARY_CONTENT_REQUIRED, entity.isBackRequired()))
                .isIssuedCountryRequired(resolveRequired(fieldMap, DocumentReferenceModels.ISSUED_COUNTRY_REQUIRED, entity.isIssuedCountryRequired()))
                .isExpiryDateRequired(resolveRequired(fieldMap, DocumentReferenceModels.EXPIRY_DATE_REQUIRED, entity.isExpiryDateRequired()))
                .isPrimaryContentRequired(resolveRequired(fieldMap, DocumentReferenceModels.PRIMARY_CONTENT_REQUIRED, entity.isPrimaryContentRequired()))
                .isSecondaryContentRequired(resolveRequired(fieldMap, DocumentReferenceModels.SECONDARY_CONTENT_REQUIRED, entity.isSecondaryContentRequired()))
                .build();
    }

    private List<DocumentSetupResponse.FieldSpec> toResponseFields(List<DocumentSetupRequest.FieldSpec> fields, Long languageId) {
        return fields.stream()
                .map(field -> DocumentSetupResponse.FieldSpec.builder()
                        .referenceModel(field.getReferenceModel())
                        .visible(field.getVisible())
                        .shortLabel(resolveLocalized(field.getShortLabel(), extractLongMap(field.getShortLabelI18n()), languageId))
                        .longLabel(resolveLocalized(field.getLongLabel(), extractLongMap(field.getLongLabelI18n()), languageId))
                        .validations(localizeValidationsForResponse(field.getValidations(), languageId))
                        .build())
                .toList();
    }

    private boolean resolveRequired(Map<String, DocumentSetupRequest.FieldSpec> fieldMap, String referenceModel, boolean fallback) {
        DocumentSetupRequest.FieldSpec field = fieldMap.get(referenceModel);
        return field != null ? isRequired(field) : fallback;
    }

    private boolean isRequired(Map<String, DocumentSetupRequest.FieldSpec> fieldMap, String referenceModel) {
        return isRequired(fieldMap.get(referenceModel));
    }

    private Map<String, DocumentSetupRequest.FieldSpec> toFieldMap(List<DocumentSetupRequest.FieldSpec> fields) {
        if (fields.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, DocumentSetupRequest.FieldSpec> fieldMap = new HashMap<>(fields.size());
        for (DocumentSetupRequest.FieldSpec field : fields) {
            if (field != null && field.getReferenceModel() != null) {
                fieldMap.put(DocumentReferenceModels.normalize(field.getReferenceModel()), field);
            }
        }
        return fieldMap;
    }

    private boolean isRequired(DocumentSetupRequest.FieldSpec field) {
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

    private String writeMetadata(List<DocumentSetupRequest.FieldSpec> fields) {
        try {
            return objectMapper.writeValueAsString(fields);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize RSP wise document metadata", e);
        }
    }
    private List<DocumentSetupRequest.FieldSpec> defaultFields(List<DocumentSetupRequest.FieldSpec> fields) {
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }
        return fields.stream()
                .filter(java.util.Objects::nonNull)
                .map(field -> DocumentSetupRequest.FieldSpec.builder()
                        .referenceModel(DocumentReferenceModels.normalize(field.getReferenceModel()))
                        .visible(field.getVisible())
                        .shortLabel(field.getShortLabel())
                        .shortLabelI18n(field.getShortLabelI18n())
                        .longLabel(field.getLongLabel())
                        .longLabelI18n(field.getLongLabelI18n())
                        .validations(field.getValidations())
                        .build())
                .toList();
    }

    private DocumentSetupEntity resolveTranslationTarget(DocumentTranslationRequest request) {
        if (request.getSetupId() != null) {
            return repository.findDetailedById(request.getSetupId())
                    .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found with id: " + request.getSetupId()));
        }
        if (request.getDocumentId() != null) {
            return repository.findDetailedByDocument_Id(request.getDocumentId())
                    .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found for"
                            + " documentId " + request.getDocumentId()));
        }
        throw new IllegalArgumentException("Either setupId or documentId must be provided");
    }

    private void mergeFieldTranslations(DocumentFieldConfigEntity fieldEntity,
                                        DocumentTranslationRequest.FieldTranslation fieldTranslation) {
        Map<Long, DocumentFieldTranslationEntity> translationsByLanguageId = new HashMap<>();
        for (DocumentFieldTranslationEntity translation : fieldEntity.getTranslations()) {
            if (translation.getLanguageId() != null) {
                translationsByLanguageId.put(translation.getLanguageId(), translation);
            }
        }
        mergeLabelTranslations(fieldEntity, translationsByLanguageId, fieldTranslation.getShortLabelI18n(), true);
        mergeLabelTranslations(fieldEntity, translationsByLanguageId, fieldTranslation.getLongLabelI18n(), false);

        Map<String, Object> validations = asMap(fieldTranslation.getValidations());
        if (validations.isEmpty()) {
            return;
        }
        Map<String, DocumentFieldValidationEntity> validationsByType = new LinkedHashMap<>();
        for (DocumentFieldValidationEntity validation : fieldEntity.getValidations()) {
            if (validation.getValidationType() != null) {
                validationsByType.put(validation.getValidationType(), validation);
            }
        }
        for (Map.Entry<String, Object> entry : validations.entrySet()) {
            DocumentFieldValidationEntity validationEntity = validationsByType.get(entry.getKey());
            if (validationEntity == null || !(entry.getValue() instanceof Map<?, ?> params)) {
                continue;
            }
            Object messageI18n = params.get("messageI18n");
            if (messageI18n instanceof Map<?, ?> messageMap) {
                mergeValidationTranslations(validationEntity, objectMapper.convertValue(messageMap, new TypeReference<Map<Long, String>>() {}));
            }
        }
    }

    private void mergeLabelTranslations(DocumentFieldConfigEntity fieldEntity,
                                        Map<Long, DocumentFieldTranslationEntity> translationsByLanguageId,
                                        Map<Long, String> values,
                                        boolean shortLabel) {
        if (values == null || values.isEmpty()) {
            return;
        }
        for (Map.Entry<Long, String> entry : values.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            DocumentFieldTranslationEntity translation = translationsByLanguageId.computeIfAbsent(entry.getKey(), languageId -> {
                DocumentFieldTranslationEntity created = DocumentFieldTranslationEntity.builder()
                        .fieldConfig(fieldEntity)
                        .languageId(languageId)
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

    private void mergeValidationTranslations(DocumentFieldValidationEntity validationEntity,
                                             Map<Long, String> messageI18n) {
        if (messageI18n == null || messageI18n.isEmpty()) {
            return;
        }
        Map<Long, DocumentFieldValidationTranslationEntity> translationsByLanguageId = new HashMap<>();
        for (DocumentFieldValidationTranslationEntity translation : validationEntity.getTranslations()) {
            if (translation.getLanguageId() != null) {
                translationsByLanguageId.put(translation.getLanguageId(), translation);
            }
        }
        for (Map.Entry<Long, String> entry : messageI18n.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            DocumentFieldValidationTranslationEntity translation = translationsByLanguageId.computeIfAbsent(entry.getKey(), languageId -> {
                DocumentFieldValidationTranslationEntity created = DocumentFieldValidationTranslationEntity.builder()
                        .validation(validationEntity)
                        .languageId(languageId)
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

    private Object localizeValidationsForResponse(Object source, Long languageId) {
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
                    extractLongMap(params.get("messageI18n")),
                    languageId);
            if (localizedMessage != null) {
                params.put("message", localizedMessage);
            }
            params.remove("messageI18n");
            response.put(entry.getKey(), params);
        }
        return response;
    }

    private Map<Long, String> extractLongMap(Object source) {
        if (!(source instanceof Map<?, ?> map) || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                try {
                    Long key = Long.valueOf(entry.getKey().toString());
                    result.put(key, entry.getValue().toString());
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        return result;
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
}

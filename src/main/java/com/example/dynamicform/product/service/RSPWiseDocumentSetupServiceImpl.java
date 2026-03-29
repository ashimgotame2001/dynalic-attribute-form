package com.example.dynamicform.product.service;

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

    private static final String DOCUMENT_NUMBER_REQUIRED = "isDocumentNumberRequired";
    private static final String BACK_REQUIRED = "isBackRequired";
    private static final String ISSUED_COUNTRY_REQUIRED = "isIssuedCountryRequired";
    private static final String EXPIRY_DATE_REQUIRED = "isExpiryDateRequired";
    private static final String PRIMARY_CONTENT_REQUIRED = "isPrimaryContentRequired";
    private static final String SECONDARY_CONTENT_REQUIRED = "isSecondaryContentRequired";

    @Override
    @Transactional
    public RSPWiseDocumentSetupResponse create(RSPWiseDocumentSetupRequest request) {
        DocumentEntity document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + request.getDocumentId()));

        List<RSPWiseDocumentSetupRequest.FieldSpec> fields = defaultFields(request.getFields());
        Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldMap = toFieldMap(fields);
        boolean secondaryContentRequired = isRequired(fieldMap, SECONDARY_CONTENT_REQUIRED);
        boolean backRequired = isRequired(fieldMap, BACK_REQUIRED) || secondaryContentRequired;
        RSPWiseDocumentSetupEntity entity = RSPWiseDocumentSetupEntity.builder()
                .document(document)
                .isPrimary(request.getIsPrimary())
                .rspId(request.getRspId())
                .metadataJson(writeMetadata(fields))
                .isDocumentNumberRequired(isRequired(fieldMap, DOCUMENT_NUMBER_REQUIRED))
                .isBackRequired(backRequired)
                .isIssuedCountryRequired(isRequired(fieldMap, ISSUED_COUNTRY_REQUIRED))
                .isExpiryDateRequired(isRequired(fieldMap, EXPIRY_DATE_REQUIRED))
                .isPrimaryContentRequired(isRequired(fieldMap, PRIMARY_CONTENT_REQUIRED))
                .isSecondaryContentRequired(secondaryContentRequired || backRequired)
                .fieldConfigs(new java.util.LinkedHashSet<>())
                .build();
        entity.getFieldConfigs().addAll(fieldConfigAdapter.toEntities(entity, fields));

        RSPWiseDocumentSetupEntity saved = repository.save(entity);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public RSPWiseDocumentSetupResponse update(UUID id, RSPWiseDocumentSetupRequest request) {
        RSPWiseDocumentSetupEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found with id: " + id));

        DocumentEntity document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + request.getDocumentId()));

        List<RSPWiseDocumentSetupRequest.FieldSpec> fields = defaultFields(request.getFields());
        Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldMap = toFieldMap(fields);
        boolean secondaryContentRequired = isRequired(fieldMap, SECONDARY_CONTENT_REQUIRED);
        boolean backRequired = isRequired(fieldMap, BACK_REQUIRED) || secondaryContentRequired;
        entity.setDocument(document);
        entity.setIsPrimary(request.getIsPrimary());
        entity.setRspId(request.getRspId());
        entity.setMetadataJson(writeMetadata(fields));
        entity.setDocumentNumberRequired(isRequired(fieldMap, DOCUMENT_NUMBER_REQUIRED));
        entity.setBackRequired(backRequired);
        entity.setIssuedCountryRequired(isRequired(fieldMap, ISSUED_COUNTRY_REQUIRED));
        entity.setExpiryDateRequired(isRequired(fieldMap, EXPIRY_DATE_REQUIRED));
        entity.setPrimaryContentRequired(isRequired(fieldMap, PRIMARY_CONTENT_REQUIRED));
        entity.setSecondaryContentRequired(secondaryContentRequired || backRequired);
        entity.getFieldConfigs().clear();
        entity.getFieldConfigs().addAll(fieldConfigAdapter.toEntities(entity, fields));

        RSPWiseDocumentSetupEntity updated = repository.save(entity);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public RSPWiseDocumentSetupResponse updateTranslations(RSPWiseDocumentTranslationRequest request) {
        RSPWiseDocumentSetupEntity entity = resolveTranslationTarget(request);
        Map<String, RSPWiseDocumentFieldConfigEntity> fieldsByReferenceModel = new HashMap<>();
        for (RSPWiseDocumentFieldConfigEntity fieldConfig : entity.getFieldConfigs()) {
            if (fieldConfig.getReferenceModel() != null) {
                fieldsByReferenceModel.put(fieldConfig.getReferenceModel(), fieldConfig);
            }
        }

        if (request.getFields() != null) {
            for (RSPWiseDocumentTranslationRequest.FieldTranslation fieldTranslation : request.getFields()) {
                if (fieldTranslation == null || fieldTranslation.getReferenceModel() == null) {
                    continue;
                }
                RSPWiseDocumentFieldConfigEntity fieldEntity = fieldsByReferenceModel.get(fieldTranslation.getReferenceModel());
                if (fieldEntity == null) {
                    continue;
                }
                mergeFieldTranslations(fieldEntity, fieldTranslation);
            }
        }

        entity.setMetadataJson(writeMetadata(fieldConfigAdapter.toFieldSpecs(entity)));
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public RSPWiseDocumentSetupResponse getById(UUID id) {
        RSPWiseDocumentSetupEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("RSPWiseDocumentSetup not found with id: " + id));
        return mapToResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RSPWiseDocumentSetupResponse> getAll() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
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

    private RSPWiseDocumentSetupResponse mapToResponse(RSPWiseDocumentSetupEntity entity) {
        Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldMap = fieldConfigAdapter.toFieldMap(entity);
        return RSPWiseDocumentSetupResponse.builder()
                .id(entity.getId())
                .documentId(entity.getDocument() != null ? entity.getDocument().getId() : null)
                .documentName(entity.getDocument() != null ? entity.getDocument().getDocumentName() : null)
                .isPrimary(entity.getIsPrimary())
                .rspId(entity.getRspId())
                .isDocumentNumberRequired(resolveRequired(fieldMap, DOCUMENT_NUMBER_REQUIRED, entity.isDocumentNumberRequired()))
                .isBackRequired(resolveRequired(fieldMap, BACK_REQUIRED,
                        resolveRequired(fieldMap, SECONDARY_CONTENT_REQUIRED, entity.isBackRequired())))
                .isIssuedCountryRequired(resolveRequired(fieldMap, ISSUED_COUNTRY_REQUIRED, entity.isIssuedCountryRequired()))
                .isExpiryDateRequired(resolveRequired(fieldMap, EXPIRY_DATE_REQUIRED, entity.isExpiryDateRequired()))
                .isPrimaryContentRequired(resolveRequired(fieldMap, PRIMARY_CONTENT_REQUIRED, entity.isPrimaryContentRequired()))
                .isSecondaryContentRequired(resolveRequired(fieldMap, SECONDARY_CONTENT_REQUIRED,
                        resolveRequired(fieldMap, BACK_REQUIRED, entity.isSecondaryContentRequired())))
                .build();
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
                fieldMap.put(field.getReferenceModel(), field);
            }
        }
        return fieldMap;
    }

    private boolean isRequired(RSPWiseDocumentSetupRequest.FieldSpec field) {
        if (field == null || field.getValidations() == null || field.getValidations().getRequired() == null) {
            return false;
        }
        return Boolean.TRUE.equals(field.getValidations().getRequired().getValue());
    }

    private String writeMetadata(List<RSPWiseDocumentSetupRequest.FieldSpec> fields) {
        try {
            return objectMapper.writeValueAsString(fields);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize RSP wise document metadata", e);
        }
    }
    private List<RSPWiseDocumentSetupRequest.FieldSpec> defaultFields(List<RSPWiseDocumentSetupRequest.FieldSpec> fields) {
        return fields == null ? Collections.emptyList() : fields;
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

        if (fieldTranslation.getValidations() == null || fieldTranslation.getValidations().getRequired() == null) {
            return;
        }
        RSPWiseDocumentFieldValidationEntity requiredValidation = fieldEntity.getValidations().stream()
                .filter(validation -> validation.getValidationType() == com.example.dynamicform.product.entity.RSPWiseDocumentFieldValidationType.REQUIRED)
                .findFirst()
                .orElse(null);
        if (requiredValidation == null) {
            return;
        }
        mergeValidationTranslations(requiredValidation, fieldTranslation.getValidations().getRequired().getMessageI18n());
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
}

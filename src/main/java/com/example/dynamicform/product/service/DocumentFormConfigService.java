package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.api.dto.FieldSpecRequest;
import com.example.dynamicform.platform.api.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.core.engine.DtoIntrospector;
import com.example.dynamicform.product.dto.DocumentSetupRequest;
import com.example.dynamicform.product.entity.DocumentSetupEntity;
import com.example.dynamicform.product.model.SupportingDocument;
import com.example.dynamicform.product.repository.DocumentSetupRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentFormConfigService {

    private final DocumentSetupRepository documentSetupRepository;
    private final ObjectMapper objectMapper;
    private final DtoIntrospector dtoIntrospector;
    private final DocumentFieldConfigAdapter fieldConfigAdapter;
    private static final String DOCUMENT_METADATA_PATH = "document_static_metadata.json";
    private List<Map<String, Object>> cachedDocumentMetadata;
    private Map<String, String> cachedLabelMap;
    private static final String DEFAULT_REQUIRED_MESSAGE = "Field is required";

    private List<Map<String, Object>> getDocumentMetadata() {
        if (cachedDocumentMetadata == null) {
            try (InputStream is = new ClassPathResource(DOCUMENT_METADATA_PATH).getInputStream()) {
                cachedDocumentMetadata = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            } catch (IOException e) {
                cachedDocumentMetadata = Collections.emptyList();
            }
        }
        return cachedDocumentMetadata;
    }

    private Map<String, String> getLabelMap() {
        if (cachedLabelMap == null) {
            Map<String, String> labels = new HashMap<>();
            for (Map<String, Object> item : getDocumentMetadata()) {
                Object referenceModel = item.get("value");
                if (referenceModel == null) {
                    referenceModel = item.get("referenceModel");
                }
                Object label = item.get("label");
                if (referenceModel != null && label != null) {
                    labels.put(referenceModel.toString(), label.toString());
                }
            }
            cachedLabelMap = labels;
        }
        return cachedLabelMap;
    }

    public RawFormMetadata generateDocumentFormMetadata( java.util.UUID documentId, String service) {
        DocumentSetupEntity setup = documentSetupRepository.findByDocument_Id(documentId)
                .orElseThrow(() -> new RuntimeException("RSP Document Setup not found for  Document ID " + documentId));

        Map<String, DocumentSetupRequest.FieldSpec> fieldConfig = readFieldConfig(setup);
        List<FieldSpecRequest.FieldSpec> fieldSpecs = buildSupportingDocumentSpecs(fieldConfig);
        RawFormMetadata metadata = dtoIntrospector.buildMetadata(
                fieldSpecs,
                "SupportingDocument",
                SupportingDocument.class,
                null,
                null
        );
        applyReferenceLabels(metadata.getDomainModel().getAttributes(), "document");
        metadata.setModuleName("DynamicForm");
        metadata.setArtifactName("SupportingDocumentForm");
        metadata.setVersion("1.0.0");
        metadata.setTargetClassName(SupportingDocument.class.getName());
        return metadata;
    }

    private String resolveLabel(String referenceModel) {
        return getLabelMap().getOrDefault(referenceModel, referenceModel);
    }

    private Map<String, DocumentSetupRequest.FieldSpec> readFieldConfig(DocumentSetupEntity setup) {
        if (setup.getMetadataJson() == null || setup.getMetadataJson().isBlank()) {
            return Collections.emptyMap();
        }
        try {
            List<DocumentSetupRequest.FieldSpec> fields = objectMapper.readValue(
                    setup.getMetadataJson(),
                    new TypeReference<List<DocumentSetupRequest.FieldSpec>>() {});
            Map<String, DocumentSetupRequest.FieldSpec> fieldMap = new LinkedHashMap<>();
            for (DocumentSetupRequest.FieldSpec field : fields) {
                if (field != null && field.getReferenceModel() != null) {
                    fieldMap.put(DocumentReferenceModels.normalize(field.getReferenceModel()), field);
                }
            }
            return fieldMap;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private void applyBooleanFallback(Map<String, DocumentSetupRequest.FieldSpec> mappedFields, String referenceModel, boolean required) {
        mappedFields.computeIfAbsent(referenceModel, key -> DocumentSetupRequest.FieldSpec.builder()
                .referenceModel(referenceModel)
                .visible(Boolean.TRUE)
                .validations(requiredValidationPayload(DEFAULT_REQUIRED_MESSAGE))
                .build());
    }

    private List<FieldSpecRequest.FieldSpec> buildSupportingDocumentSpecs(Map<String, DocumentSetupRequest.FieldSpec> fieldConfig) {
        List<FieldSpecRequest.FieldSpec> specs = new ArrayList<>();
        specs.add(alwaysRequiredSpec("documentType/id", "document/documentType/id"));
        specs.add(toFieldSpec("issueCountry/alphaTwoCode", "document/issueCountry/alphaTwoCode",
                fieldConfig.get(DocumentReferenceModels.ISSUED_COUNTRY_REQUIRED), true));
        specs.add(toFieldSpec("documentNumber", "document/documentNumber",
                fieldConfig.get(DocumentReferenceModels.DOCUMENT_NUMBER_REQUIRED), true));
        specs.add(alwaysRequiredSpec("issueDate", "document/issueDate"));
        specs.add(toFieldSpec("expiryDate", "document/expiryDate",
                fieldConfig.get(DocumentReferenceModels.EXPIRY_DATE_REQUIRED), true));
        specs.add(toFieldSpec("primaryContent", "document/primaryContent",
                fieldConfig.get(DocumentReferenceModels.PRIMARY_CONTENT_REQUIRED), true));
        specs.add(toFieldSpec(
                "secondaryContent",
                "document/secondaryContent",
                fieldConfig.get(DocumentReferenceModels.SECONDARY_CONTENT_REQUIRED),
                true));
        return specs;
    }

    private FieldSpecRequest.FieldSpec alwaysRequiredSpec(String targetReferenceModel, String labelReferenceModel) {
        return FieldSpecRequest.FieldSpec.builder()
                .referenceModel(targetReferenceModel)
                .visible(true)
                .shortLabel(resolveLabel(labelReferenceModel))
                .longLabel(resolveLabel(labelReferenceModel))
                .validations(requiredValidationPayload("Field is required"))
                .build();
    }

    private FieldSpecRequest.FieldSpec toFieldSpec(String targetReferenceModel,
                                                   String labelReferenceModel,
                                                   DocumentSetupRequest.FieldSpec source,
                                                   boolean defaultVisible) {
        String defaultLabel = resolveLabel(labelReferenceModel);
        return FieldSpecRequest.FieldSpec.builder()
                .referenceModel(targetReferenceModel)
                .visible(source != null && source.getVisible() != null ? source.getVisible() : defaultVisible)
                .shortLabel(source != null && source.getShortLabel() != null ? source.getShortLabel() : defaultLabel)
                .shortLabelI18n(source != null ? source.getShortLabelI18n() : null)
                .longLabel(source != null && source.getLongLabel() != null ? source.getLongLabel() : defaultLabel)
                .longLabelI18n(source != null ? source.getLongLabelI18n() : null)
                .validations(source != null ? normalizeValidations(source.getValidations()) : null)
                .build();
    }

    private Map<String, Object> requiredValidationPayload(String message) {
        return requiredValidationPayload(message, null);
    }

    private Map<String, Object> requiredValidationPayload(String message, Map<String, String> messageI18n) {
        Map<String, Object> params = new HashMap<>();
        params.put("value", true);
        params.put("message", message);
        if (messageI18n != null && !messageI18n.isEmpty()) {
            params.put("messageI18n", messageI18n);
        }

        Map<String, Object> rule = new HashMap<>();
        rule.put("required", params);
        return rule;
    }

    private String requiredMessage(DocumentSetupRequest.FieldSpec fieldSpec) {
        Map<String, Object> validations = normalizeValidations(fieldSpec != null ? fieldSpec.getValidations() : null);
        Object required = validations.get("required");
        if (!(required instanceof Map<?, ?> requiredMap)) {
            return DEFAULT_REQUIRED_MESSAGE;
        }
        Object message = requiredMap.get("message");
        return message instanceof String str && !str.isBlank() ? str : DEFAULT_REQUIRED_MESSAGE;
    }

    private Map<String, String> requiredMessageI18n(DocumentSetupRequest.FieldSpec fieldSpec) {
        Map<String, Object> validations = normalizeValidations(fieldSpec != null ? fieldSpec.getValidations() : null);
        Object required = validations.get("required");
        if (!(required instanceof Map<?, ?> requiredMap) || !(requiredMap.get("messageI18n") instanceof Map<?, ?> messageMap)) {
            return null;
        }
        return objectMapper.convertValue(messageMap, new TypeReference<Map<String, String>>() {});
    }

    private boolean isRequired(DocumentSetupRequest.FieldSpec fieldSpec) {
        Map<String, Object> validations = normalizeValidations(fieldSpec != null ? fieldSpec.getValidations() : null);
        Object required = validations.get("required");
        return required instanceof Map<?, ?> requiredMap && Boolean.TRUE.equals(requiredMap.get("value"));
    }

    private Map<String, Object> normalizeValidations(Object validations) {
        if (!(validations instanceof Map<?, ?> map) || map.isEmpty()) {
            return Collections.emptyMap();
        }
        return objectMapper.convertValue(map, new TypeReference<Map<String, Object>>() {});
    }

    private void applyReferenceLabels(List<RawDomainAttribute> attributes, String prefix) {
        if (attributes == null) {
            return;
        }
        for (RawDomainAttribute attribute : attributes) {
            String currentPath = prefix == null || prefix.isBlank()
                    ? attribute.getAttributeName()
                    : prefix + "/" + attribute.getAttributeName();
            if (!Boolean.TRUE.equals(attribute.getReference()) && !Boolean.TRUE.equals(attribute.getAssociation())) {
                String resolved = resolveLabel(currentPath);
                if ((attribute.getShortLabel() == null || attribute.getShortLabel().isBlank())
                        && resolved != null && !resolved.equals(currentPath)) {
                    attribute.setShortLabel(resolved);
                }
                if ((attribute.getLongLabel() == null || attribute.getLongLabel().isBlank())
                        && resolved != null && !resolved.equals(currentPath)) {
                    attribute.setLongLabel(resolved);
                }
            }
            if (attribute.getDomainModel() != null && attribute.getDomainModel().getAttributes() != null) {
                applyReferenceLabels(attribute.getDomainModel().getAttributes(), currentPath);
            }
        }
    }
}

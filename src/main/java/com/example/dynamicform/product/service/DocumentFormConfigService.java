package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.dto.FieldSpecRequest;
import com.example.dynamicform.platform.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.interpreter.DtoIntrospector;
import com.example.dynamicform.product.dto.RSPWiseDocumentSetupRequest;
import com.example.dynamicform.product.entity.RSPWiseDocumentSetupEntity;
import com.example.dynamicform.product.model.SupportingDocument;
import com.example.dynamicform.product.repository.RSPWiseDocumentSetupRepository;
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

    private final RSPWiseDocumentSetupRepository rspWiseDocumentSetupRepository;
    private final ObjectMapper objectMapper;
    private final DtoIntrospector dtoIntrospector;
    private final RSPWiseDocumentFieldConfigAdapter fieldConfigAdapter;
    private static final String DOCUMENT_METADATA_PATH = "document_metadata.json";
    private List<Map<String, Object>> cachedDocumentMetadata;
    private Map<String, String> cachedLabelMap;
    private static final String DOCUMENT_NUMBER_REQUIRED = "isDocumentNumberRequired";
    private static final String BACK_REQUIRED = "isBackRequired";
    private static final String ISSUED_COUNTRY_REQUIRED = "isIssuedCountryRequired";
    private static final String EXPIRY_DATE_REQUIRED = "isExpiryDateRequired";
    private static final String PRIMARY_CONTENT_REQUIRED = "isPrimaryContentRequired";
    private static final String SECONDARY_CONTENT_REQUIRED = "isSecondaryContentRequired";
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
                Object referenceModel = item.get("referenceModel");
                Object label = item.get("label");
                if (referenceModel != null && label != null) {
                    labels.put(referenceModel.toString(), label.toString());
                }
            }
            cachedLabelMap = labels;
        }
        return cachedLabelMap;
    }

    public RawFormMetadata generateDocumentFormMetadata(Long rspId, java.util.UUID documentId, String service) {
        RSPWiseDocumentSetupEntity setup = rspWiseDocumentSetupRepository.findByRspIdAndDocument_Id(rspId, documentId)
                .orElseThrow(() -> new RuntimeException("RSP Document Setup not found for RSP ID " + rspId + " and Document ID " + documentId));

        Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldConfig = readFieldConfig(setup);
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

    private Map<String, RSPWiseDocumentSetupRequest.FieldSpec> readFieldConfig(RSPWiseDocumentSetupEntity setup) {
        if (setup.getMetadataJson() == null || setup.getMetadataJson().isBlank()) {
            return Collections.emptyMap();
        }
        try {
            List<RSPWiseDocumentSetupRequest.FieldSpec> fields = objectMapper.readValue(
                    setup.getMetadataJson(),
                    new TypeReference<List<RSPWiseDocumentSetupRequest.FieldSpec>>() {});
            Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldMap = new LinkedHashMap<>();
            for (RSPWiseDocumentSetupRequest.FieldSpec field : fields) {
                if (field != null && field.getReferenceModel() != null) {
                    fieldMap.put(field.getReferenceModel(), field);
                }
            }
            return fieldMap;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private void applyBooleanFallback(Map<String, RSPWiseDocumentSetupRequest.FieldSpec> mappedFields, String referenceModel, boolean required) {
        mappedFields.computeIfAbsent(referenceModel, key -> RSPWiseDocumentSetupRequest.FieldSpec.builder()
                .referenceModel(referenceModel)
                .visible(Boolean.TRUE)
                .validations(RSPWiseDocumentSetupRequest.ValidationDefinition.builder()
                        .required(RSPWiseDocumentSetupRequest.RequiredValidation.builder()
                                .value(required)
                                .message(DEFAULT_REQUIRED_MESSAGE)
                                .build())
                        .build())
                .build());
    }

    private List<FieldSpecRequest.FieldSpec> buildSupportingDocumentSpecs(Map<String, RSPWiseDocumentSetupRequest.FieldSpec> fieldConfig) {
        List<FieldSpecRequest.FieldSpec> specs = new ArrayList<>();
        specs.add(alwaysRequiredSpec("documentType/id", "document/documentType/id"));
        specs.add(toFieldSpec("issueCountry/alphaTwoCode", "document/issueCountry/alphaTwoCode", fieldConfig.get(ISSUED_COUNTRY_REQUIRED), true));
        specs.add(toFieldSpec("documentNumber", "document/documentNumber", fieldConfig.get(DOCUMENT_NUMBER_REQUIRED), true));
        specs.add(alwaysRequiredSpec("issueDate", "document/issueDate"));
        specs.add(toFieldSpec("expiryDate", "document/expiryDate", fieldConfig.get(EXPIRY_DATE_REQUIRED), true));
        specs.add(toFieldSpec("primaryContent", "document/primaryContent", fieldConfig.get(PRIMARY_CONTENT_REQUIRED), true));
        specs.add(toFieldSpec(
                "secondaryContent",
                "document/secondaryContent",
                fieldConfig.getOrDefault(SECONDARY_CONTENT_REQUIRED, fieldConfig.get(BACK_REQUIRED)),
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
                                                   RSPWiseDocumentSetupRequest.FieldSpec source,
                                                   boolean defaultVisible) {
        String defaultLabel = resolveLabel(labelReferenceModel);
        return FieldSpecRequest.FieldSpec.builder()
                .referenceModel(targetReferenceModel)
                .visible(source != null && source.getVisible() != null ? source.getVisible() : defaultVisible)
                .shortLabel(source != null && source.getShortLabel() != null ? source.getShortLabel() : defaultLabel)
                .shortLabelI18n(source != null ? source.getShortLabelI18n() : null)
                .longLabel(source != null && source.getLongLabel() != null ? source.getLongLabel() : defaultLabel)
                .longLabelI18n(source != null ? source.getLongLabelI18n() : null)
                .validations(isRequired(source) ? requiredValidationPayload(requiredMessage(source), requiredMessageI18n(source)) : null)
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

    private String requiredMessage(RSPWiseDocumentSetupRequest.FieldSpec fieldSpec) {
        if (fieldSpec == null
                || fieldSpec.getValidations() == null
                || fieldSpec.getValidations().getRequired() == null
                || fieldSpec.getValidations().getRequired().getMessage() == null
                || fieldSpec.getValidations().getRequired().getMessage().isBlank()) {
            return DEFAULT_REQUIRED_MESSAGE;
        }
        return fieldSpec.getValidations().getRequired().getMessage();
    }

    private Map<String, String> requiredMessageI18n(RSPWiseDocumentSetupRequest.FieldSpec fieldSpec) {
        if (fieldSpec == null
                || fieldSpec.getValidations() == null
                || fieldSpec.getValidations().getRequired() == null) {
            return null;
        }
        return fieldSpec.getValidations().getRequired().getMessageI18n();
    }

    private boolean isRequired(RSPWiseDocumentSetupRequest.FieldSpec fieldSpec) {
        return fieldSpec != null
                && fieldSpec.getValidations() != null
                && fieldSpec.getValidations().getRequired() != null
                && Boolean.TRUE.equals(fieldSpec.getValidations().getRequired().getValue());
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

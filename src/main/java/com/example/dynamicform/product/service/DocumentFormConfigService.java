package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.dto.metadata.RawDomainModel;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.product.entity.RSPWiseDocumentSetupEntity;
import com.example.dynamicform.product.repository.RSPWiseDocumentSetupRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentFormConfigService {

    private final RSPWiseDocumentSetupRepository rspWiseDocumentSetupRepository;
    private final ObjectMapper objectMapper;
    private static final String DOCUMENT_METADATA_PATH = "document_metadata.json";
    private List<Map<String, Object>> cachedDocumentMetadata;

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

    public RawFormMetadata generateDocumentFormMetadata(Long rspId, java.util.UUID documentId, String service) {
        List<RSPWiseDocumentSetupEntity> setups = rspWiseDocumentSetupRepository.findByRspId(rspId);
        
        RSPWiseDocumentSetupEntity setup = setups.stream()
                .filter(s -> s.getDocument().getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("RSP Document Setup not found for RSP ID " + rspId + " and Document ID " + documentId));

        List<RawDomainAttribute> attributes = new ArrayList<>();

        // documentType
        attributes.add(createAttribute("documentType", "DocumentType", "document/documentType", true, false, true, resolveLabel("document/documentType/id"), resolveLabel("document/documentType/id"), List.of(requiredValidation())));
        
        // issueCountry
        attributes.add(createAttribute("issueCountry", "Origin", "document/issueCountry", true, false, true, resolveLabel("document/issueCountry/alphaTwoCode"), resolveLabel("document/issueCountry/alphaTwoCode"), List.of(requiredValidation())));

        // documentNumber
        attributes.add(createAttribute("documentNumber", "String", null, false, false, false, resolveLabel("document/documentNumber"), resolveLabel("document/documentNumber"), List.of(requiredValidation())));

        // issueDate
        attributes.add(createAttribute("issueDate", "LocalDate", null, false, false, false, resolveLabel("document/issueDate"), resolveLabel("document/issueDate"), List.of(requiredValidation())));

        // expiryDate
        String expiryLabel = resolveLabel("document/expiryDate");
        if (setup.isExpiryDateRequired()) {
            attributes.add(createAttribute("expiryDate", "LocalDate", null, false, false, false, expiryLabel, expiryLabel, List.of(requiredValidation())));
        } else {
            attributes.add(createAttribute("expiryDate", "LocalDate", null, false, false, false, expiryLabel, expiryLabel, null));
        }

        // primaryContent (Always Required)
        attributes.add(createAttribute("primaryContent", "String", null, false, false, false, "Front Page", "Front Page", List.of(requiredValidation())));

        // secondaryContent (isBackRequired)
        if (setup.isBackRequired()) {
            attributes.add(createAttribute("secondaryContent", "String", null, false, false, false, "Back Page", "Back Page", List.of(requiredValidation())));
        } else {
            attributes.add(createAttribute("secondaryContent", "String", null, false, false, false, "Back Page", "Back Page", null));
        }

        RawDomainModel domainModel = RawDomainModel.builder()
                .attributes(attributes)
                .build();

        return RawFormMetadata.builder()
                .modelName("SupportingDocument")
                .domainModel(domainModel)
                .moduleName("DynamicForm")
                .artifactName("SupportingDocumentForm")
                .version("1.0.0")
                .build();
    }

    private String resolveLabel(String referenceModel) {
        for (Map<String, Object> item : getDocumentMetadata()) {
            if (referenceModel.equals(item.get("referenceModel"))) {
                Object label = item.get("label");
                if (label != null) {
                    return label.toString();
                }
            }
        }
        return referenceModel;
    }

    private RawDomainAttribute createAttribute(String name, String type, String referenceModel, boolean reference, boolean collection, boolean association, String shortLabel, String longLabel, List<Map<String, Object>> validations) {
        return RawDomainAttribute.builder()
                .attributeName(name)
                .attributeType(type)
                .referenceModel(referenceModel)
                .reference(reference)
                .collection(collection)
                .association(association)
                .shortLabel(shortLabel)
                .longLabel(longLabel)
                .validations(validations)
                .visible(true)
                .build();
    }

    private Map<String, Object> requiredValidation() {
        Map<String, Object> params = new HashMap<>();
        params.put("message", "Field is required");
        
        Map<String, Object> rule = new HashMap<>();
        rule.put("required", params);
        return rule;
    }
}

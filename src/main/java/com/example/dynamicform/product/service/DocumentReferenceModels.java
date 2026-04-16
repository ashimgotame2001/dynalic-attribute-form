package com.example.dynamicform.product.service;

import java.util.LinkedHashMap;
import java.util.Map;

final class DocumentReferenceModels {

    static final String DOCUMENT_NUMBER_REQUIRED = "document/documentNumber";
    static final String ISSUED_COUNTRY_REQUIRED = "document/issueCountry/alphaTwoCode";
    static final String EXPIRY_DATE_REQUIRED = "document/expiryDate";
    static final String PRIMARY_CONTENT_REQUIRED = "document/primaryContent";
    static final String SECONDARY_CONTENT_REQUIRED = "document/secondaryContent";

    private static final Map<String, String> LEGACY_TO_METADATA_REFERENCE_MODEL = new LinkedHashMap<>();

    static {
        LEGACY_TO_METADATA_REFERENCE_MODEL.put("isDocumentNumberRequired", DOCUMENT_NUMBER_REQUIRED);
        LEGACY_TO_METADATA_REFERENCE_MODEL.put("isIssuedCountryRequired", ISSUED_COUNTRY_REQUIRED);
        LEGACY_TO_METADATA_REFERENCE_MODEL.put("isExpiryDateRequired", EXPIRY_DATE_REQUIRED);
        LEGACY_TO_METADATA_REFERENCE_MODEL.put("isPrimaryContentRequired", PRIMARY_CONTENT_REQUIRED);
        LEGACY_TO_METADATA_REFERENCE_MODEL.put("isSecondaryContentRequired", SECONDARY_CONTENT_REQUIRED);
        LEGACY_TO_METADATA_REFERENCE_MODEL.put("isBackRequired", SECONDARY_CONTENT_REQUIRED);
    }

    private DocumentReferenceModels() {
    }

    static String normalize(String referenceModel) {
        if (referenceModel == null || referenceModel.isBlank()) {
            return referenceModel;
        }
        return LEGACY_TO_METADATA_REFERENCE_MODEL.getOrDefault(referenceModel, referenceModel);
    }
}

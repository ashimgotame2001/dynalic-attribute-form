package com.example.dynamicform.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSetupResponse {
    private UUID id;
    private UUID documentId;
    private String documentName;
    private Boolean isPrimary;
    private List<FieldSpec> fields;
    private boolean isDocumentNumberRequired;
    private boolean isBackRequired;
    private boolean isIssuedCountryRequired;
    private boolean isExpiryDateRequired;
    private boolean isPrimaryContentRequired;
    private boolean isSecondaryContentRequired;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldSpec {
        private String referenceModel;
        private Boolean visible;
        private String shortLabel;
        private String longLabel;
        private Object validations;
    }
}

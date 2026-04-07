package com.example.dynamicform.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RSPWiseDocumentSetupRequest {
    private UUID documentId;
    private Boolean isPrimary;
    private Long rspId;

    private List<FieldSpec> fields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class FieldSpec {
        private String referenceModel;
        private Boolean visible;
        private String shortLabel;
        private Map<String, String> shortLabelI18n;
        private String longLabel;
        private Map<String, String> longLabelI18n;
        private Object validations;
    }
}

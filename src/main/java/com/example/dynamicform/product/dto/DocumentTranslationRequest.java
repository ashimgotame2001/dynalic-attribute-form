package com.example.dynamicform.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class DocumentTranslationRequest {
    private UUID setupId;
    private UUID documentId;
    private List<FieldTranslation> fields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FieldTranslation {
        private String referenceModel;
        private Map<Long, String> shortLabelI18n;
        private Map<Long, String> longLabelI18n;
        private Object validations;
    }
}

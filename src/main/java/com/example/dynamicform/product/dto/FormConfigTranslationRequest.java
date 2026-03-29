package com.example.dynamicform.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormConfigTranslationRequest {

    private String formName;
    private Integer version;
    private List<FieldTranslation> fields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FieldTranslation {
        private String referenceModel;
        private Map<String, String> shortLabelI18n;
        private Map<String, String> longLabelI18n;
        private List<ValidationTranslation> validations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidationTranslation {
        private String type;
        private Map<String, String> messageI18n;
    }
}

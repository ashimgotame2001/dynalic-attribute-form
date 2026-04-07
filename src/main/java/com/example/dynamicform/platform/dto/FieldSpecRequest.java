package com.example.dynamicform.platform.dto;

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
public class FieldSpecRequest {

    private String formName;


    private List<FieldSpec> fields;

    private String description;


    private Long rspId;

    private List<FieldSpecAdditionalAttribute> additionalAttributes;


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


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class FieldSpecAdditionalAttribute {

        private Long attributeId;


        private Boolean visible;


        private String shortLabel;

        private Map<String, String> shortLabelI18n;


        private String longLabel;
        private Map<String, String> longLabelI18n;
        private String expression;
        private Object validations;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidationRule {
        private String type;
        private Object value;
        private String pattern;
        private String message;
        private Map<String, String> messageI18n;
    }
}

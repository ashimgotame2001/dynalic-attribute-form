package com.example.dynamicform.dto;

import com.example.dynamicform.enums.DynamicFieldFor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldSpecRequest {

    private String formName;

    private DynamicFieldFor fieldType;

    private List<FieldSpec> fields;

    private String description;

    private String targetDtoClassName;

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


        private String longLabel;


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


        private String longLabel;


        private Object validations;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidationRule {
        private String type;
        private Boolean value;
        private String pattern;
        private String message;
    }
}

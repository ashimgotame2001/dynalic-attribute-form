package com.example.dynamicform.dto.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Raw DTO representing an attribute in the domain model.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RawDomainAttribute {

    private String modelName;

    private String referenceModel;

    private String attributeName;

    private String attributeType;

    private Boolean reference;

    private Boolean collection;

    private Boolean association;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean visible;

    private String shortLabel;

    private String longLabel;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Map<String, Object>> validations;

    private RawDomainModel domainModel;
}

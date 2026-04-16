package com.example.dynamicform.platform.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class AttributeContractCreateRequest {

    private DynamicFieldFor fieldType;

    private List<String> referenceModels;
    private Integer minPrimaryDocuments;
    private Integer minSecondaryDocuments;
}

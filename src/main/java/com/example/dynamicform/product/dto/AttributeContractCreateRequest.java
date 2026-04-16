package com.example.dynamicform.product.dto;

import lombok.Data;

import java.util.List;

import com.example.dynamicform.product.enums.DynamicFieldFor;

@Data
public class AttributeContractCreateRequest {

    private DynamicFieldFor fieldType;

    private List<String> referenceModels;
    private Integer minPrimaryDocuments;
    private Integer minSecondaryDocuments;
}


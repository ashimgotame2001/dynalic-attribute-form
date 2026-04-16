package com.example.dynamicform.product.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

import com.example.dynamicform.product.enums.DynamicFieldFor;

@Value
@Builder
public class AttributeContractResponse {

    UUID id;


    DynamicFieldFor fieldType;

    String referenceModel;

    String label;

    Integer minPrimaryDocuments;

    Integer minSecondaryDocuments;
}


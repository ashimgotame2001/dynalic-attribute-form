package com.example.dynamicform.product.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

import com.example.dynamicform.product.enums.DynamicFieldFor;

@Value
@Builder
public class RSPAttributeContractResponse {

    UUID id;

    Long rspId;

    DynamicFieldFor fieldType;

    String referenceModel;

    String label;

    Integer minPrimaryDocuments;

    Integer minSecondaryDocuments;
}


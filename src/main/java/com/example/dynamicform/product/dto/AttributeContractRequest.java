package com.example.dynamicform.product.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AttributeContractRequest {

    private UUID id;

    private Long rspId;

    private String referenceModel;
}


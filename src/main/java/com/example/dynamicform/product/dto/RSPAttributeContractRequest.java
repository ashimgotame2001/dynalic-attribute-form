package com.example.dynamicform.product.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class RSPAttributeContractRequest {

    private UUID id;

    private Long rspId;

    private String referenceModel;
}


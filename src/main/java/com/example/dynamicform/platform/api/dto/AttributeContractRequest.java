package com.example.dynamicform.platform.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AttributeContractRequest {

    private UUID id;

    private String referenceModel;
}

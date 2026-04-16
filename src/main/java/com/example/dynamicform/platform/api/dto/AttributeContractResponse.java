package com.example.dynamicform.platform.api.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

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

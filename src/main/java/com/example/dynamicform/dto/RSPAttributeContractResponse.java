package com.example.dynamicform.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

import com.example.dynamicform.enums.DynamicFieldFor;

@Value
@Builder
public class RSPAttributeContractResponse {

    UUID id;

    Long rspId;

    DynamicFieldFor fieldType;

    String referenceModel;

    String label;
}


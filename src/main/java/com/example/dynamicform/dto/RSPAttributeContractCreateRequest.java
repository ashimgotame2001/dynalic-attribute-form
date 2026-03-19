package com.example.dynamicform.dto;

import lombok.Data;

import java.util.List;

import com.example.dynamicform.enums.DynamicFieldFor;

@Data
public class RSPAttributeContractCreateRequest {

    private Long rspId;

    private DynamicFieldFor fieldType;

    private List<String> referenceModels;
}


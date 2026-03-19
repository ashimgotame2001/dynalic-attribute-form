package com.example.dynamicform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DynamicFormConfigRequest {
    private Long rspId;
    private com.example.dynamicform.enums.DynamicFieldFor fieldType;
    private Map<String, Object> attributes;
}
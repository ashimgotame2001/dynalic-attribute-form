package com.example.dynamicform.dto;

import lombok.Data;
import java.util.UUID;
import com.example.dynamicform.enums.DynamicFieldFor;

@Data
public class RSPRelatedAttributeRequest {
    private UUID id;
    private Long rspId;
    private String attributeName;
    private DynamicFieldFor fieldType;
}

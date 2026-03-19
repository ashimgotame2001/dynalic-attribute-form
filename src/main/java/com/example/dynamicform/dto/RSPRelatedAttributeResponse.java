package com.example.dynamicform.dto;

import lombok.Builder;
import lombok.Value;
import java.util.UUID;
import com.example.dynamicform.enums.DynamicFieldFor;

@Value
@Builder
public class RSPRelatedAttributeResponse {
    UUID id;
    Long rspId;
    String attributeName;
    DynamicFieldFor fieldType;
}

package com.example.dynamicform.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.example.dynamicform.enums.MetadataUpdateTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileScreenConfigUpdateRequest {

    private JsonNode config;

    private String changeRemark;

    @NotNull(message = "{201}")
    MetadataUpdateTypeEnum updateType;
}

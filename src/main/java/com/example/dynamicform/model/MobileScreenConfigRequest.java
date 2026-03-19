package com.example.dynamicform.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileScreenConfigRequest {

    @NotNull(message = "{201}")
    private int major;

    @NotNull(message = "{201}")
    private int minor;

    @NotNull(message = "{201}")
    private int patch;

    private JsonNode config;

    private String changeRemark;
}

package com.example.dynamicform.platform.api.dto;

import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import com.example.dynamicform.product.entity.CustomerFormConfigurationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for form configuration details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormConfigResponse {
    private RawFormMetadata metadata;

    public static FormConfigResponse fromEntity(CustomerFormConfigurationEntity entity, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        RawFormMetadata metadata = null;
        if (entity.getMetadataJson() != null && objectMapper != null) {
            try {
                metadata = objectMapper.readValue(entity.getMetadataJson(), RawFormMetadata.class);
            } catch (Exception e) {
                // Log error or handle as needed
            }
        }
        return FormConfigResponse.builder()
                .metadata(metadata)
                .build();
    }
}

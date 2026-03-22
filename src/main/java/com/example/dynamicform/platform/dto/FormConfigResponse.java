package com.example.dynamicform.platform.dto;

import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
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
    private String formName;
    private Integer version;
    private String description;
    private String targetDtoClassName;
    private Long rspId;

    private Boolean isActive;
    private RawFormMetadata metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
                .formName(entity.getFormName())
                .version(entity.getVersion())
                .description(entity.getDescription())
                .rspId(entity.getRspId())
                .isActive(entity.getIsActive())
                .metadata(metadata)
                .targetDtoClassName(metadata != null ? metadata.getTargetClassName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

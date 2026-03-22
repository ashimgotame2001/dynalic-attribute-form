package com.example.dynamicform.dto;

import com.example.dynamicform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.entity.CustomerFormConfigurationEntity;
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

    public static FormConfigResponse fromEntity(CustomerFormConfigurationEntity entity) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            RawFormMetadata metadata = mapper.readValue(entity.getMetadataJson(), RawFormMetadata.class);
            return FormConfigResponse.builder()
                    .formName(entity.getFormName())
                    .version(entity.getVersion())
                    .description(entity.getDescription())
                    .targetDtoClassName(entity.getTargetDtoClassName())
                    .rspId(entity.getRspId())
                    .isActive(entity.getIsActive())
                    .metadata(metadata)
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse metadata JSON", e);
        }
    }
}

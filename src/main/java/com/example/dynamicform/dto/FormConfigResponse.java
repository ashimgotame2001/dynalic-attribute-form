package com.example.dynamicform.dto;

import com.example.dynamicform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.entity.FormConfigurationEntity;
import com.example.dynamicform.enums.DynamicFieldFor;
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
    private DynamicFieldFor fieldType;
    private Boolean isActive;
    private RawFormMetadata metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FormConfigResponse fromEntity(FormConfigurationEntity entity) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            RawFormMetadata metadata = mapper.readValue(entity.getMetadataJson(), RawFormMetadata.class);
            return FormConfigResponse.builder()
                    .formName(entity.getFormName())
                    .version(entity.getVersion())
                    .description(entity.getDescription())
                    .targetDtoClassName(entity.getTargetDtoClassName())
                    .rspId(entity.getRspId())
                    .fieldType(entity.getFieldType())
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

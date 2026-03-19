package com.example.dynamicform.dto;

import com.example.dynamicform.interpreter.SimplifiedMetadataTransformer;
import com.example.dynamicform.dto.metadata.RawFormMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating or updating a form configuration.
 * Accepts the simplified client format and transforms it to RawFormMetadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormConfigRequest {
    /**
     * Name of the form (must be unique).
     */
    private String formName;

    /**
     * Simplified field definitions in the client format.
     */
    private List<SimplifiedFormField> fields;

    /**
     * Human-readable description.
     */
    private String description;

    /**
     * Fully qualified class name of the target DTO this form maps to.
     */
    private String targetDtoClassName;

    /**
     * RSP ID associated with this form configuration.
     */
    private Long rspId;

    /**
     * Converts the simplified format to RawFormMetadata.
     */
    public RawFormMetadata toRawFormMetadata(SimplifiedMetadataTransformer transformer) {
        return transformer.transform(fields, formName, targetDtoClassName);
    }
}

package com.example.dynamicform.dto.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.dynamicform.dto.metadata.validation.RawValidationRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Raw DTO for deserializing the JSON metadata structure.
 * This matches exactly the provided JSON schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RawFormMetadata {

    @JsonIgnore
    private String modelName;

    private RawDomainModel domainModel;

    private String moduleName;

    private String artifactName;

    private String version;
}

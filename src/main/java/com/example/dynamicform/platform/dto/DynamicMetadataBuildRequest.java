package com.example.dynamicform.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DynamicMetadataBuildRequest {

    private String formName;
    private String description;
    private String moduleName;
    private String artifactName;
    private String targetClassName;
    private String staticMetadataPath;
    private List<String> fallbackStaticMetadataPaths;
    private List<FieldSpecRequest.FieldSpec> fields;
    private Set<String> enabledReferenceModels;
}

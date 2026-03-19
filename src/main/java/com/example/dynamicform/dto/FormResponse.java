package com.example.dynamicform.dto;

import com.example.dynamicform.dto.metadata.RawDomainModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormResponse {
    private String moduleName;
    private String artifactName;
    private String version;
    private RawDomainModel domainModel;
}
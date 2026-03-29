package com.example.dynamicform.product.dto;

import com.example.dynamicform.platform.dto.FieldSpecRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GenericFormConfigRequest extends FieldSpecRequest {

    private String targetClassName;
    private String staticMetadataPath;
    private String moduleName;
    private String artifactName;
}

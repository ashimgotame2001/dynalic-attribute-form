package com.example.dynamicform.controller;

import com.example.dynamicform.dto.FieldSpecRequest;
import com.example.dynamicform.dto.FormConfigResponse;
import com.example.dynamicform.dto.RSPAttributeContractResponse;
import com.example.dynamicform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.engine.DynamicFormEngine;
import com.example.dynamicform.entity.FormConfigurationEntity;
import com.example.dynamicform.exception.FormNotFoundException;
import com.example.dynamicform.interpreter.DtoIntrospector;
import com.example.dynamicform.service.FormConfigService;
import com.example.dynamicform.service.RSPAttributeContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for form configuration management (admin APIs).
 */
@RestController
@RequestMapping("/api/config/forms")
@RequiredArgsConstructor
public class FormConfigController {

    private final FormConfigService formConfigService;
    private final DynamicFormEngine dynamicFormEngine;
    private final DtoIntrospector dtoIntrospector;
    private final RSPAttributeContractService rspAttributeContractService;

    @PostMapping
    public ResponseEntity<FormConfigResponse> saveConfiguration(@Valid @RequestBody FieldSpecRequest request) {
        try {
            // Load the target DTO class
            Class<?> targetClass = Class.forName(request.getTargetDtoClassName());

            // Get enabled reference models from RSP contracts
            List<RSPAttributeContractResponse> contracts = rspAttributeContractService.findByRspIdAndFieldType(request.getRspId(), request.getFieldType());
            Set<String> enabledReferenceModels = contracts.stream()
                    .map(c -> c.getReferenceModel().replace(".", "/"))
                    .collect(java.util.stream.Collectors.toSet());

            // Build RawFormMetadata by introspecting the DTO and field specs
            RawFormMetadata metadata = dtoIntrospector.buildMetadata(
                    request.getFields(),
                    request.getFormName(),
                    targetClass,
                    enabledReferenceModels,
                    null
            );
            String metadataJson = toJsonString(metadata);

            var entity = formConfigService.createOrUpdateConfiguration(
                    request.getFormName(),
                    metadataJson,
                    request.getDescription(),
                    request.getTargetDtoClassName(),
                    request.getRspId(),
                    request.getFieldType()
            );

            FormConfigResponse response = FormConfigResponse.fromEntity(entity);
            return ResponseEntity.ok(response);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Target DTO class not found: " + request.getTargetDtoClassName(), e);
        }
    }

    /**
     * Get a specific form configuration by name and optional version.
     */
    @GetMapping("/{formName}")
    public ResponseEntity<FormConfigResponse> getConfiguration(
            @PathVariable String formName,
            @RequestParam(required = false) Integer version) {
        var entity = version != null 
                ? formConfigService.getConfiguration(formName, version).orElse(null)
                : formConfigService.getActiveConfiguration(formName);
        if (entity == null) {
            throw new FormNotFoundException(formName);
        }
        return ResponseEntity.ok(FormConfigResponse.fromEntity(entity));
    }

    /**
     * List all form configurations (latest versions).
     */
    @GetMapping
    public ResponseEntity<List<FormConfigResponse>> listConfigurations() {
        var all = formConfigService.getAllConfigurations();
        // Group by formName and get latest version for each (since repository returns all)
        var latestByForm = all.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        com.example.dynamicform.entity.FormConfigurationEntity::getFormName,
                        java.util.stream.Collectors.maxBy(java.util.Comparator.comparing(com.example.dynamicform.entity.FormConfigurationEntity::getVersion))
                ))
                .values().stream()
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();

        var response = latestByForm.stream()
                .map(FormConfigResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a specific version of a form configuration.
     */
    @DeleteMapping("/{formName}")
    public ResponseEntity<Void> deleteConfiguration(
            @PathVariable String formName,
            @RequestParam Integer version) {
        formConfigService.deleteConfiguration(formName, version);
        return ResponseEntity.noContent().build();
    }

    private String toJsonString(com.example.dynamicform.dto.metadata.RawFormMetadata metadata) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(metadata);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid metadata", e);
        }
    }
}

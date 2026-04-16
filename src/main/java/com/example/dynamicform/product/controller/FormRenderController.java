package com.example.dynamicform.product.controller;

import com.example.dynamicform.product.dto.DynamicFormConfigRequest;
import com.example.dynamicform.platform.api.dto.AttributeContractResponse;
import com.example.dynamicform.platform.api.dto.FormDefinition;
import com.example.dynamicform.platform.api.dto.ValidationError;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.core.engine.DynamicFormEngine;
import com.example.dynamicform.platform.api.exception.DynamicValidationException;
import com.example.dynamicform.platform.api.exception.FormNotFoundException;
import com.example.dynamicform.platform.service.AttributeContractAccessService;
import com.example.dynamicform.platform.service.ResponseLocalizationService;
import com.example.dynamicform.platform.service.RawMetadataCustomizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller for frontend form rendering and submission.
 */
@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormRenderController {

    private final DynamicFormEngine dynamicFormEngine;
    private final AttributeContractAccessService attributeContractService;
    private final RawMetadataCustomizationService rawMetadataCustomizationService;
    private final ResponseLocalizationService responseLocalizationService;

    /**
     * Get the form definition for rendering a dynamic form.
     */
    @GetMapping("/{formName}")
    public ResponseEntity<FormDefinition> getForm(@PathVariable String formName,
                                                  @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        FormDefinition formDefinition = dynamicFormEngine.getFormDefinition(formName);
        return ResponseEntity.ok(responseLocalizationService.prepareFormDefinitionResponse(
                formDefinition, languageId));
    }

    /**
     * Get customized form definition based on RSP and request attributes.
     */
    @PostMapping("/{formName}/config")
    public ResponseEntity<RawFormMetadata> getCustomForm(@PathVariable String formName,
                                                         @RequestBody DynamicFormConfigRequest request,
                                                         @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        FormDefinition baseForm = dynamicFormEngine.getFormDefinition(formName);
        RawFormMetadata baseRaw = baseForm.getRawMetadata();
        // Default to "customer" module for existing calls
        List<AttributeContractResponse> enabledAttributes = attributeContractService.findByModule( "customer");
        Set<String> enabledReferenceModels = enabledAttributes.stream()
                .map(AttributeContractResponse::getReferenceModel)
                .collect(java.util.stream.Collectors.toSet());
        RawFormMetadata customized = rawMetadataCustomizationService.customize(baseRaw, enabledReferenceModels, request.getAttributes());
        return ResponseEntity.ok(responseLocalizationService.prepareRawMetadataResponse(
                customized, languageId));
    }

    /**
     * Submit form data. The engine will validate and optionally map to DTO.
     * The request body can be any JSON structure matching the form definition.
     */
    @PostMapping("/{formName}/submit")
    public ResponseEntity<?> submitForm(
            @PathVariable String formName,
            @RequestBody Map<String, Object> payload) {
        
        List<ValidationError> errors = dynamicFormEngine.validate(formName, payload);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        // Optionally, we could map to DTO and further processing
        try {
            Object dto = dynamicFormEngine.validateAndMap(formName, payload);
            // Business logic processing would go here, e.g., service.save(dto)
            return ResponseEntity.ok().body(Map.of("status", "success", "message", "Form submitted successfully"));
        } catch (DynamicValidationException | FormNotFoundException e) {
            // Should not happen as we already validated, but just in case
            return ResponseEntity.badRequest().body(List.of(
                    ValidationError.builder()
                            .fieldPath("system")
                            .message(e.getMessage())
                            .validationType("system")
                            .build()
            ));
        }
    }

    /**
     * Validate form data without submitting.
     */
    @PostMapping("/{formName}/validate")
    public ResponseEntity<List<ValidationError>> validateForm(
            @PathVariable String formName,
            @RequestBody Map<String, Object> payload) {
        List<ValidationError> errors = dynamicFormEngine.validate(formName, payload);
        if (errors.isEmpty()) {
            return ResponseEntity.ok().body(List.of());
        }
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Check if a form exists.
     */
    @GetMapping("/{formName}/exists")
    public ResponseEntity<Map<String, Boolean>> exists(@PathVariable String formName) {
        boolean exists = dynamicFormEngine.formExists(formName);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}

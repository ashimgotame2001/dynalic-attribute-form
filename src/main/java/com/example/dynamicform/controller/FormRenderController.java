package com.example.dynamicform.controller;

import com.example.dynamicform.dto.DynamicFormConfigRequest;
import com.example.dynamicform.dto.FieldDefinition;
import com.example.dynamicform.dto.FormDefinition;
import com.example.dynamicform.dto.RSPAttributeContractResponse;
import com.example.dynamicform.dto.ValidationError;
import com.example.dynamicform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.engine.DynamicFormEngine;
import com.example.dynamicform.exception.DynamicValidationException;
import com.example.dynamicform.exception.FormNotFoundException;
import com.example.dynamicform.service.RSPAttributeContractService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for frontend form rendering and submission.
 */
@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormRenderController {

    private final DynamicFormEngine dynamicFormEngine;
    private final RSPAttributeContractService rspAttributeContractService;
    private final ObjectMapper objectMapper;

    /**
     * Get the form definition for rendering a dynamic form.
     */
    @GetMapping("/{formName}")
    public ResponseEntity<FormDefinition> getForm(@PathVariable String formName) {
        FormDefinition formDefinition = dynamicFormEngine.getFormDefinition(formName);
        return ResponseEntity.ok(formDefinition);
    }

    /**
     * Get customized form definition based on RSP and request attributes.
     */
    @PostMapping("/{formName}/config")
    public ResponseEntity<RawFormMetadata> getCustomForm(@PathVariable String formName, @RequestBody DynamicFormConfigRequest request) {
        FormDefinition baseForm = dynamicFormEngine.getFormDefinition(formName);
        RawFormMetadata baseRaw = baseForm.getRawMetadata();
        List<RSPAttributeContractResponse> enabledAttributes = request.getFieldType() != null
                ? rspAttributeContractService.findByRspIdAndFieldType(request.getRspId(), request.getFieldType())
                : rspAttributeContractService.findByRspId(request.getRspId());
        Set<String> enabledReferenceModels = enabledAttributes.stream()
                .map(RSPAttributeContractResponse::getReferenceModel)
                .collect(java.util.stream.Collectors.toSet());
        RawFormMetadata customized = customizeRawFormMetadata(baseRaw, enabledReferenceModels, request.getAttributes());
        return ResponseEntity.ok(customized);
    }

    private FormDefinition customizeFormDefinition(FormDefinition baseForm, Set<String> enabledReferenceModels, Map<String, Object> requestAttributes) {
        List<FieldDefinition> customizedFields = baseForm.getFields().stream()
                .filter(field -> isAttributeEnabled(enabledReferenceModels, field.getFieldName()))
                .map(field -> {
                    FieldDefinition copy = field.toBuilder().build(); // assuming builder
                    if (requestAttributes != null && requestAttributes.containsKey(field.getFieldName())) {
                        copy.setVisible(true);
                    } else {
                        copy.setVisible(false);
                    }
                    if (field.getNestedFields() != null) {
                        copy.setNestedFields(customizeNestedFields(field.getNestedFields(), enabledReferenceModels, requestAttributes));
                    }
                    return copy;
                })
                .toList();
        return baseForm.toBuilder().fields(customizedFields).build();
    }

    private List<FieldDefinition> customizeNestedFields(List<FieldDefinition> nestedFields, Set<String> enabledReferenceModels, Map<String, Object> requestAttributes) {
        return nestedFields.stream()
                .filter(field -> isAttributeEnabled(enabledReferenceModels, field.getFieldName()))
                .map(field -> {
                    FieldDefinition copy = field.toBuilder().build();
                    // For nested, perhaps check in sub-map, but for simplicity, assume same logic
                    copy.setVisible(requestAttributes != null && requestAttributes.containsKey(field.getFieldName()));
                    if (field.getNestedFields() != null) {
                        copy.setNestedFields(customizeNestedFields(field.getNestedFields(), enabledReferenceModels, requestAttributes));
                    }
                    return copy;
                })
                .toList();
    }

    private boolean isAttributeEnabled(Set<String> enabledReferenceModels, String fieldName) {
        // Map fieldName to referenceModel
        String refModel = mapFieldNameToReferenceModel(fieldName);
        return refModel != null && enabledReferenceModels.contains(refModel);
    }

    private RawFormMetadata customizeRawFormMetadata(RawFormMetadata baseRaw, Set<String> enabledReferenceModels, Map<String, Object> requestAttributes) {
        RawFormMetadata customized = baseRaw.toBuilder().build();
        customized.setDomainModel(customizeRawDomainModel(baseRaw.getDomainModel(), enabledReferenceModels, requestAttributes));
        return customized;
    }

    private com.example.dynamicform.dto.metadata.RawDomainModel customizeRawDomainModel(com.example.dynamicform.dto.metadata.RawDomainModel domainModel, Set<String> enabledReferenceModels, Map<String, Object> requestAttributes) {
        if (domainModel == null || domainModel.getAttributes() == null) {
            return domainModel;
        }
        List<com.example.dynamicform.dto.metadata.RawDomainAttribute> customizedAttributes = domainModel.getAttributes().stream()
                .filter(attr -> enabledReferenceModels.contains(attr.getReferenceModel()))
                .map(attr -> {
                    com.example.dynamicform.dto.metadata.RawDomainAttribute copy = attr.toBuilder().build();
                    // For simplicity, assume attributeName is the key for requestAttributes
                    if (requestAttributes != null && requestAttributes.containsKey(attr.getAttributeName())) {
                        copy.setVisible(true);
                    } else {
                        copy.setVisible(false);
                    }
                    // Recursively customize nested
                    if (attr.getDomainModel() != null) {
                        copy.setDomainModel(customizeRawDomainModel(attr.getDomainModel(), enabledReferenceModels, requestAttributes));
                    }
                    return copy;
                })
                .toList();
        return com.example.dynamicform.dto.metadata.RawDomainModel.builder().attributes(customizedAttributes).build();
    }

    private String mapFieldNameToReferenceModel(String fieldName) {
        return switch (fieldName) {
            case "gender" -> "individual/gender/id";
            case "nationality" -> "individual/origin/alphaTwoCode";
            case "residingAlphaTwoCode" -> "individual/residingCountry/alphaTwoCode";
            case "postalCode" -> "individual/address/postalCodeInfo/postalCode";
            case "city" -> "individual/address/city";
            case "addressLine1" -> "individual/address/addressLine1";
            case "firstName" -> "individual/firstName";
            case "middleName" -> "individual/middleName";
            case "lastName" -> "individual/lastName";
            case "dateOfBirth" -> "individual/dateOfBirth";
            case "contactNumber" -> "individual/contactNumber";
            case "email" -> "individual/email";
            case "secret" -> "user/secret";
            case "referralCode" -> "referral/referralCode";
            default -> null;
        };
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

package com.example.dynamicform.product.controller;

import com.example.dynamicform.platform.dto.FieldSpecRequest;
import com.example.dynamicform.platform.dto.FormConfigResponse;
import com.example.dynamicform.product.entity.CustomerFormConfigurationEntity;
import com.example.dynamicform.platform.exception.FormNotFoundException;
import com.example.dynamicform.product.model.BeneficiaryRequest;
import com.example.dynamicform.product.model.RegisterCustomerRequest;
import com.example.dynamicform.product.model.TransactionRequest;
import com.example.dynamicform.product.service.FormConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for form configuration management (admin APIs).
 */
@RestController
@RequestMapping("/api/config/forms")
@RequiredArgsConstructor
public class FormConfigController {

    private final FormConfigService formConfigService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @PostMapping("/customer")
    public ResponseEntity<FormConfigResponse> saveCustomerConfiguration(@Valid @RequestBody FieldSpecRequest request) {
        var entity = formConfigService.generateMetaDataForCustomer(request, RegisterCustomerRequest.class);
        FormConfigResponse response = FormConfigResponse.fromEntity(entity, objectMapper);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/beneficiary")
    public ResponseEntity<FormConfigResponse> saveBeneficiaryConfiguration(@Valid @RequestBody FieldSpecRequest request) {
        var entity = formConfigService.generateMetaDataForBeneficiary(request, BeneficiaryRequest.class);
        FormConfigResponse response = FormConfigResponse.fromEntity(entity, objectMapper);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transaction")
    public ResponseEntity<FormConfigResponse> saveTransactionConfiguration(@Valid @RequestBody FieldSpecRequest request) {
        var entity = formConfigService.generateMetaDataForTransaction(request, TransactionRequest.class);
        FormConfigResponse response = FormConfigResponse.fromEntity(entity, objectMapper);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<FormConfigResponse> saveConfiguration(@Valid @RequestBody FieldSpecRequest request) {
        var entity = formConfigService.createOrUpdateConfiguration(request, RegisterCustomerRequest.class);
        FormConfigResponse response = FormConfigResponse.fromEntity(entity, objectMapper);
        return ResponseEntity.ok(response);
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
        return ResponseEntity.ok(FormConfigResponse.fromEntity(entity, objectMapper));
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
                        CustomerFormConfigurationEntity::getFormName,
                        java.util.stream.Collectors.maxBy(java.util.Comparator.comparing(CustomerFormConfigurationEntity::getVersion))
                ))
                .values().stream()
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();

        var response = latestByForm.stream()
                .map(e -> FormConfigResponse.fromEntity(e, objectMapper))
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

}

package com.example.dynamicform.product.controller;

import com.example.dynamicform.platform.api.dto.FieldSpecRequest;
import com.example.dynamicform.platform.api.dto.FormConfigResponse;
import com.example.dynamicform.product.entity.CustomerFormConfigurationEntity;
import com.example.dynamicform.platform.api.exception.FormNotFoundException;
import com.example.dynamicform.platform.service.ResponseLocalizationService;
import com.example.dynamicform.product.dto.FormConfigTranslationRequest;
import com.example.dynamicform.product.dto.GenericFormConfigRequest;
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
    private final ResponseLocalizationService responseLocalizationService;

    @PostMapping("/customer")
    public ResponseEntity<FormConfigResponse> saveCustomerConfiguration(@Valid @RequestBody FieldSpecRequest request,
                                                                        @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        var entity = formConfigService.generateMetaDataForCustomer(request, RegisterCustomerRequest.class);
        FormConfigResponse response = formConfigService.buildResponse(entity, languageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/beneficiary")
    public ResponseEntity<FormConfigResponse> saveBeneficiaryConfiguration(@Valid @RequestBody FieldSpecRequest request,
                                                                           @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        var entity = formConfigService.generateMetaDataForBeneficiary(request, BeneficiaryRequest.class);
        FormConfigResponse response = formConfigService.buildResponse(entity, languageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transaction")
    public ResponseEntity<FormConfigResponse> saveTransactionConfiguration(@Valid @RequestBody FieldSpecRequest request,
                                                                          @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        var entity = formConfigService.generateMetaDataForTransaction(request, TransactionRequest.class);
        FormConfigResponse response = formConfigService.buildResponse(entity, languageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<FormConfigResponse> saveConfiguration(@Valid @RequestBody FieldSpecRequest request,
                                                                @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        var entity = formConfigService.createOrUpdateConfiguration(request, RegisterCustomerRequest.class);
        FormConfigResponse response = formConfigService.buildResponse(entity, languageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generic")
    public ResponseEntity<FormConfigResponse> saveGenericConfiguration(@Valid @RequestBody GenericFormConfigRequest request,
                                                                       @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        var entity = formConfigService.generateGenericMetaData(
                request,
                request.getTargetClassName(),
                request.getStaticMetadataPath(),
                request.getModuleName(),
                request.getArtifactName() != null ? request.getArtifactName() : request.getFormName());
        FormConfigResponse response = formConfigService.buildResponse(entity, languageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translations")
    public ResponseEntity<FormConfigResponse> saveTranslations(@Valid @RequestBody FormConfigTranslationRequest request,
                                                               @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        var entity = formConfigService.updateTranslations(request);
        return ResponseEntity.ok(formConfigService.buildResponse(entity, languageId));
    }

    /**
     * Get a specific form configuration by name and optional version.
     */
    @GetMapping("/{formName}")
    public ResponseEntity<FormConfigResponse> getConfiguration(
            @PathVariable String formName,
            @RequestParam(required = false) Integer version,
            @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        var entity = version != null 
                ? formConfigService.getConfiguration(formName, version).orElse(null)
                : formConfigService.getActiveConfiguration(formName);
        if (entity == null) {
            throw new FormNotFoundException(formName);
        }
        return ResponseEntity.ok(formConfigService.buildResponse(entity, languageId));
    }

    /**
     * List all form configurations (latest versions).
     */
    @GetMapping
    public ResponseEntity<List<FormConfigResponse>> listConfigurations(
            @RequestHeader(value = "Language-Id", required = false) Long languageId) {
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
                .map(e -> formConfigService.buildResponse(e, languageId))
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

    private String resolveLanguage(String languageHeader, String acceptLanguageHeader) {
        return responseLocalizationService.resolveLanguage(languageHeader, acceptLanguageHeader);
    }

}

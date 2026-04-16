package com.example.dynamicform.product.controller;

import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.service.DynamicRelationshipFormService;
import com.example.dynamicform.product.model.KycRegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for runtime metadata generation.
 * Provides endpoints to generate metadata from domain models at runtime.
 */
@RestController
@RequestMapping("/api/v1/runtime-metadata")
public class RuntimeMetadataController {

    private final com.example.dynamicform.platform.service.RuntimeMetadataGenerator runtimeMetadataGenerator;
    private final DynamicRelationshipFormService dynamicRelationshipFormService;

    public RuntimeMetadataController(com.example.dynamicform.platform.service.RuntimeMetadataGenerator runtimeMetadataGenerator,
                                     DynamicRelationshipFormService dynamicRelationshipFormService) {
        this.runtimeMetadataGenerator = runtimeMetadataGenerator;
        this.dynamicRelationshipFormService = dynamicRelationshipFormService;
    }

    @PostMapping("/generate")
    public ResponseEntity<RawFormMetadata> generateMetadata(
            @RequestParam String className,
            @RequestParam String formName,
            @RequestParam(required = false) String context) {

        try {
            Class<?> domainClass = Class.forName(className);
            RawFormMetadata metadata = runtimeMetadataGenerator.generateMetadata(domainClass, formName, context);
            return ResponseEntity.ok(dynamicRelationshipFormService.augmentWithRelationships(metadata, formName));
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/generate/{context}")
    public ResponseEntity<RawFormMetadata> generateMetadataWithContext(
            @RequestParam String className,
            @RequestParam String formName,
            @PathVariable String context) {

        try {
            Class<?> domainClass = Class.forName(className);
            RawFormMetadata metadata = runtimeMetadataGenerator.generateMetadata(domainClass, formName, context);
            return ResponseEntity.ok(dynamicRelationshipFormService.augmentWithRelationships(metadata, formName));
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/kyc")
    public ResponseEntity<RawFormMetadata> generateKycMetadata() {
        RawFormMetadata metadata = runtimeMetadataGenerator.generateMetadata(KycRegisterRequest.class, "KycRegisterRequest", "kyc");
        return ResponseEntity.ok(dynamicRelationshipFormService.augmentWithRelationships(metadata, "KycRegisterRequest"));
    }
}

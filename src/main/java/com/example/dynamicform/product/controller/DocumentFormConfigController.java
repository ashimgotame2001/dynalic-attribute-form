package com.example.dynamicform.product.controller;

import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.service.ResponseLocalizationService;
import com.example.dynamicform.product.service.DocumentFormConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/document-forms")
@RequiredArgsConstructor
public class DocumentFormConfigController {

    private final DocumentFormConfigService documentFormConfigService;
    private final ResponseLocalizationService responseLocalizationService;

    @GetMapping
    public ResponseEntity<RawFormMetadata> getDocumentForm(
            @RequestParam java.util.UUID documentId,
            @RequestParam(required = false, defaultValue = "customer") String service,
            @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        RawFormMetadata metadata = documentFormConfigService.generateDocumentFormMetadata( documentId, service);
        return ResponseEntity.ok(responseLocalizationService.prepareRawMetadataResponse(
                metadata, languageId));
    }
}

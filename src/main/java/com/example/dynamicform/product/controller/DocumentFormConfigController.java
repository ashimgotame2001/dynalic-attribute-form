package com.example.dynamicform.product.controller;

import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.product.service.DocumentFormConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/document-forms")
@RequiredArgsConstructor
public class DocumentFormConfigController {

    private final DocumentFormConfigService documentFormConfigService;

    @GetMapping
    public ResponseEntity<RawFormMetadata> getDocumentForm(
            @RequestParam Long rspId,
            @RequestParam java.util.UUID documentId,
            @RequestParam(required = false, defaultValue = "customer") String service) {
        RawFormMetadata metadata = documentFormConfigService.generateDocumentFormMetadata(rspId, documentId, service);
        return ResponseEntity.ok(metadata);
    }
}

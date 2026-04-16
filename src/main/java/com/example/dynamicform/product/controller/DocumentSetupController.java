package com.example.dynamicform.product.controller;

import com.example.dynamicform.platform.service.ResponseLocalizationService;
import com.example.dynamicform.product.dto.DocumentSetupRequest;
import com.example.dynamicform.product.dto.DocumentSetupResponse;
import com.example.dynamicform.product.dto.DocumentTranslationRequest;
import com.example.dynamicform.product.service.DocumentSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rsp-wise-documents")
@RequiredArgsConstructor
public class DocumentSetupController {

    private final DocumentSetupService service;
    private final ResponseLocalizationService responseLocalizationService;

    @PostMapping
    public ResponseEntity<DocumentSetupResponse> create(@RequestBody DocumentSetupRequest request,
                                                        @RequestHeader(value = "local_profile_id", required = false) Long languageId) {
        return new ResponseEntity<>(service.create(request, languageId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentSetupResponse> update(@PathVariable UUID id,
                                                        @RequestBody DocumentSetupRequest request,
                                                        @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        return ResponseEntity.ok(service.update(id, request, languageId));
    }

    @PostMapping("/translations")
    public ResponseEntity<DocumentSetupResponse> updateTranslations(@RequestBody DocumentTranslationRequest request,
                                                                    @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        return ResponseEntity.ok(service.updateTranslations(request, languageId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentSetupResponse> getById(@PathVariable UUID id,
                                                         @RequestHeader(value = "Language-Id", required = false) Long languageId) {
        return ResponseEntity.ok(service.getById(id, languageId));
    }

    @GetMapping
    public ResponseEntity<List<DocumentSetupResponse>> getAll(@RequestHeader(value = "Language-Id", required = false) Long languageId) {
        return ResponseEntity.ok(service.getAll(languageId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private String resolveLanguage(String languageHeader, String acceptLanguageHeader) {
        return responseLocalizationService.resolveLanguage(languageHeader, acceptLanguageHeader);
    }
}

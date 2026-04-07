package com.example.dynamicform.product.controller;

import com.example.dynamicform.product.dto.RSPWiseDocumentSetupRequest;
import com.example.dynamicform.product.dto.RSPWiseDocumentSetupResponse;
import com.example.dynamicform.product.dto.RSPWiseDocumentTranslationRequest;
import com.example.dynamicform.platform.service.ResponseLocalizationService;
import com.example.dynamicform.product.service.RSPWiseDocumentSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rsp-wise-documents")
@RequiredArgsConstructor
public class RSPWiseDocumentSetupController {

    private final RSPWiseDocumentSetupService service;
    private final ResponseLocalizationService responseLocalizationService;

    @PostMapping
    public ResponseEntity<RSPWiseDocumentSetupResponse> create(@RequestBody RSPWiseDocumentSetupRequest request,
                                                               @RequestHeader(value = "Language", required = false) String languageHeader,
                                                               @RequestHeader(value = "Accept-Language", required = false) String acceptLanguageHeader) {
        return new ResponseEntity<>(service.create(request, resolveLanguage(languageHeader, acceptLanguageHeader)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RSPWiseDocumentSetupResponse> update(@PathVariable UUID id,
                                                               @RequestBody RSPWiseDocumentSetupRequest request,
                                                               @RequestHeader(value = "Language", required = false) String languageHeader,
                                                               @RequestHeader(value = "Accept-Language", required = false) String acceptLanguageHeader) {
        return ResponseEntity.ok(service.update(id, request, resolveLanguage(languageHeader, acceptLanguageHeader)));
    }

    @PostMapping("/translations")
    public ResponseEntity<RSPWiseDocumentSetupResponse> updateTranslations(@RequestBody RSPWiseDocumentTranslationRequest request,
                                                                           @RequestHeader(value = "Language", required = false) String languageHeader,
                                                                           @RequestHeader(value = "Accept-Language", required = false) String acceptLanguageHeader) {
        return ResponseEntity.ok(service.updateTranslations(request, resolveLanguage(languageHeader, acceptLanguageHeader)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RSPWiseDocumentSetupResponse> getById(@PathVariable UUID id,
                                                                @RequestHeader(value = "Language", required = false) String languageHeader,
                                                                @RequestHeader(value = "Accept-Language", required = false) String acceptLanguageHeader) {
        return ResponseEntity.ok(service.getById(id, resolveLanguage(languageHeader, acceptLanguageHeader)));
    }

    @GetMapping
    public ResponseEntity<List<RSPWiseDocumentSetupResponse>> getAll(@RequestHeader(value = "Language", required = false) String languageHeader,
                                                                     @RequestHeader(value = "Accept-Language", required = false) String acceptLanguageHeader) {
        return ResponseEntity.ok(service.getAll(resolveLanguage(languageHeader, acceptLanguageHeader)));
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

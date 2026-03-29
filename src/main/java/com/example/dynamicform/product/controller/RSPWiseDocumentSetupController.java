package com.example.dynamicform.product.controller;

import com.example.dynamicform.product.dto.RSPWiseDocumentSetupRequest;
import com.example.dynamicform.product.dto.RSPWiseDocumentSetupResponse;
import com.example.dynamicform.product.dto.RSPWiseDocumentTranslationRequest;
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

    @PostMapping
    public ResponseEntity<RSPWiseDocumentSetupResponse> create(@RequestBody RSPWiseDocumentSetupRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RSPWiseDocumentSetupResponse> update(@PathVariable UUID id, @RequestBody RSPWiseDocumentSetupRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PostMapping("/translations")
    public ResponseEntity<RSPWiseDocumentSetupResponse> updateTranslations(@RequestBody RSPWiseDocumentTranslationRequest request) {
        return ResponseEntity.ok(service.updateTranslations(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RSPWiseDocumentSetupResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<RSPWiseDocumentSetupResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

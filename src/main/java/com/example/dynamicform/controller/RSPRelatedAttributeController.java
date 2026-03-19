package com.example.dynamicform.controller;

import com.example.dynamicform.dto.RSPRelatedAttributeRequest;
import com.example.dynamicform.dto.RSPRelatedAttributeResponse;
import com.example.dynamicform.service.RSPRelatedAttributeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rsp-related-attributes")
public class RSPRelatedAttributeController {
    private final RSPRelatedAttributeService service;

    public RSPRelatedAttributeController(RSPRelatedAttributeService service) {
        this.service = service;
    }

    @GetMapping
    public List<RSPRelatedAttributeResponse> list(
            @RequestParam(value = "rspId", required = false) Long rspId,
            @RequestParam(value = "fieldType", required = false) com.example.dynamicform.enums.DynamicFieldFor fieldType) {
        if (rspId != null && fieldType != null) {
            return service.findByRspIdAndFieldType(rspId, fieldType);
        }
        if (rspId != null) {
            return service.findByRspId(rspId);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RSPRelatedAttributeResponse> get(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RSPRelatedAttributeResponse> create(@RequestBody RSPRelatedAttributeRequest request) {
        RSPRelatedAttributeResponse created = service.create(request);
        String uri = "/api/rsp-related-attributes?rspId=" + request.getRspId();
        if (request.getFieldType() != null) {
            uri += "&fieldType=" + request.getFieldType();
        }
        return ResponseEntity.created(URI.create(uri)).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RSPRelatedAttributeResponse> update(
            @PathVariable UUID id,
            @RequestBody RSPRelatedAttributeRequest request
    ) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

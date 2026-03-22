package com.example.dynamicform.controller;

import com.example.dynamicform.dto.RSPAttributeContractCreateRequest;
import com.example.dynamicform.dto.RSPAttributeContractRequest;
import com.example.dynamicform.dto.RSPAttributeContractResponse;
import com.example.dynamicform.service.RSPAttributeContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rsp-attributes")
public class RSPAttributeContractController {

    private final RSPAttributeContractService service;

    public RSPAttributeContractController(RSPAttributeContractService service) {
        this.service = service;
    }

    @GetMapping
    public List<RSPAttributeContractResponse> list(
            @RequestParam(value = "rspId", required = false) Long rspId
          ) {
        if (rspId != null) {
            return service.findByRspId(rspId);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RSPAttributeContractResponse> get(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<List<RSPAttributeContractResponse>> create(@RequestBody RSPAttributeContractCreateRequest request) {
        List<RSPAttributeContractResponse> created = service.create(request);
        String uri = "/api/rsp-attributes?rspId=" + request.getRspId();
        if (request.getFieldType() != null) {
            uri += "&fieldType=" + request.getFieldType();
        }
        return ResponseEntity
                .created(URI.create(uri))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RSPAttributeContractResponse> update(
            @PathVariable UUID id,
            @RequestBody RSPAttributeContractRequest request
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


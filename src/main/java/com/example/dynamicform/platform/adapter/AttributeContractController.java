package com.example.dynamicform.platform.adapter;

import com.example.dynamicform.platform.api.dto.AttributeContractCreateRequest;
import com.example.dynamicform.platform.api.dto.AttributeContractRequest;
import com.example.dynamicform.platform.api.dto.AttributeContractResponse;
import com.example.dynamicform.platform.service.AttributeContractAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/attribute-contract")
public class AttributeContractController {

    private final AttributeContractAccessService service;

    public AttributeContractController(AttributeContractAccessService service) {
        this.service = service;
    }

    @GetMapping
    public List<AttributeContractResponse> list(@RequestParam(value = "module", defaultValue = "customer") String module) {
        return service.findByModule( module);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttributeContractResponse> get(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<List<AttributeContractResponse>> create(@RequestBody AttributeContractCreateRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttributeContractResponse> update(@PathVariable UUID id,
                                                            @RequestBody AttributeContractRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

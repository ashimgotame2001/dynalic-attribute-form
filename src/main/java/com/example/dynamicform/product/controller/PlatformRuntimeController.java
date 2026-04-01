package com.example.dynamicform.product.controller;

import com.example.dynamicform.platform.dto.PlatformEntityResponse;
import com.example.dynamicform.platform.dto.PlatformRuntimeContractResponse;
import com.example.dynamicform.platform.service.PlatformRuntimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * JSON-first runtime platform API for dynamic entity access and relationship management.
 * Keeps existing form responses unchanged while exposing the optimized platform contract.
 */
@RestController
@RequestMapping("/api/platform/runtime")
public class PlatformRuntimeController {

    private final PlatformRuntimeService platformRuntimeService;

    public PlatformRuntimeController(PlatformRuntimeService platformRuntimeService) {
        this.platformRuntimeService = platformRuntimeService;
    }

    @GetMapping("/contract/{entityType}")
    public ResponseEntity<PlatformRuntimeContractResponse> getContract(@PathVariable String entityType) {
        return ResponseEntity.ok(platformRuntimeService.getContract(entityType));
    }

    @PutMapping("/entities/{entityType}/{entityId}")
    public ResponseEntity<PlatformEntityResponse> saveEntity(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(platformRuntimeService.save(entityType, entityId, payload));
    }

    @GetMapping("/entities/{entityType}/{entityId}")
    public ResponseEntity<PlatformEntityResponse> getEntity(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "true") boolean includeRelationships) {
        return platformRuntimeService.getEntity(entityType, entityId, includeRelationships)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/entities/{entityType}")
    public ResponseEntity<List<Map<String, Object>>> getAllEntities(@PathVariable String entityType) {
        return ResponseEntity.ok(platformRuntimeService.getAll(entityType));
    }

    @PostMapping("/entities/{entityType}/{entityId}/relationships/{relationshipName}/{targetId}")
    public ResponseEntity<Void> link(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @PathVariable String relationshipName,
            @PathVariable String targetId) {
        platformRuntimeService.link(entityType, entityId, relationshipName, targetId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/entities/{entityType}/{entityId}/relationships/{relationshipName}/{targetId}")
    public ResponseEntity<Void> unlink(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @PathVariable String relationshipName,
            @PathVariable String targetId) {
        platformRuntimeService.unlink(entityType, entityId, relationshipName, targetId);
        return ResponseEntity.noContent().build();
    }
}

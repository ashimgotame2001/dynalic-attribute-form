package com.example.dynamicform.product.controller;

import com.example.dynamicform.product.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.product.service.RelationshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Product relationship setup APIs.
 * Relationships are configured using sourceEntity and targetEntity definitions.
 */
@RestController
@RequestMapping("/api/admin/relationships")
public class RelationshipController {

    private final RelationshipService relationshipService;

    public RelationshipController(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @PostMapping("/definitions")
    public ResponseEntity<RelationshipDefinitionDTO> createRelationshipDefinition(
            @Valid @RequestBody RelationshipDefinitionDTO definition) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(relationshipService.createRelationshipDefinition(definition));
    }

    @PutMapping("/definitions/{relationshipName}")
    public ResponseEntity<RelationshipDefinitionDTO> updateRelationshipDefinition(
            @PathVariable String relationshipName,
            @Valid @RequestBody RelationshipDefinitionDTO definition) {
        return ResponseEntity.ok(relationshipService.updateRelationshipDefinition(relationshipName, definition));
    }

    @DeleteMapping("/definitions/{relationshipName}")
    public ResponseEntity<Void> deleteRelationshipDefinition(@PathVariable String relationshipName) {
        relationshipService.deleteRelationshipDefinition(relationshipName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/definitions/{relationshipName}")
    public ResponseEntity<RelationshipDefinitionDTO> getRelationshipDefinition(@PathVariable String relationshipName) {
        return relationshipService.getRelationshipDefinition(relationshipName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/definitions")
    public ResponseEntity<List<RelationshipDefinitionDTO>> getAllRelationshipDefinitions() {
        return ResponseEntity.ok(relationshipService.getAllRelationshipDefinitions());
    }

    @GetMapping("/definitions/entity/{entityName}")
    public ResponseEntity<List<RelationshipDefinitionDTO>> getRelationshipDefinitionsByEntity(@PathVariable String entityName) {
        return ResponseEntity.ok(relationshipService.getRelationshipDefinitionsByEntity(entityName));
    }
}

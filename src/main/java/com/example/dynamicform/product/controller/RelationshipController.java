package com.example.dynamicform.product.controller;

import com.example.dynamicform.platform.validation.ValidationRule;
import com.example.dynamicform.platform.service.RuntimeRelationshipPlatform;
import com.example.dynamicform.platform.service.RuntimeValidationPlatform;
import com.example.dynamicform.product.dto.ValidationRuleDTO;
import com.example.dynamicform.product.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.product.dto.RelationshipInstanceDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller for managing dynamic relationships.
 * Provides admin endpoints to configure relationships at runtime.
 */
@RestController
@RequestMapping("/api/admin/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final RuntimeRelationshipPlatform relationshipPlatform;
    private final RuntimeValidationPlatform validationPlatform;

    // ============ Relationship Definition APIs ============

    /**
     * Create a new relationship definition.
        */
    @PostMapping("/definitions")
    public ResponseEntity<RelationshipDefinitionDTO> createRelationshipDefinition(
            @Valid @RequestBody RelationshipDefinitionDTO definition) {
        RelationshipDefinitionDTO created = relationshipPlatform.createRelationshipDefinition(definition);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get a specific relationship definition by name.
     */
    @GetMapping("/definitions/{relationshipName}")
    public ResponseEntity<RelationshipDefinitionDTO> getRelationshipDefinition(
            @PathVariable String relationshipName) {
        return relationshipPlatform.getRelationshipDefinition(relationshipName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update a relationship definition.
     */
    @PutMapping("/definitions/{relationshipName}")
    public ResponseEntity<RelationshipDefinitionDTO> updateRelationshipDefinition(
            @PathVariable String relationshipName,
            @Valid @RequestBody RelationshipDefinitionDTO definition) {
        try {
            RelationshipDefinitionDTO updated = relationshipPlatform.updateRelationshipDefinition(relationshipName, definition);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a relationship definition.
     */
    @DeleteMapping("/definitions/{relationshipName}")
    public ResponseEntity<Void> deleteRelationshipDefinition(
            @PathVariable String relationshipName) {
        relationshipPlatform.deleteRelationshipDefinition(relationshipName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all relationship definitions.
     */
    @GetMapping("/definitions")
    public ResponseEntity<List<RelationshipDefinitionDTO>> getAllRelationshipDefinitions() {
        List<RelationshipDefinitionDTO> definitions = relationshipPlatform.getAllRelationshipDefinitions();
        return ResponseEntity.ok(definitions);
    }

    /**
     * Get all relationship definitions involving a specific entity.
     */
    @GetMapping("/definitions/entity/{entityName}")
    public ResponseEntity<List<RelationshipDefinitionDTO>> getRelationshipDefinitionsByEntity(
            @PathVariable String entityName) {
        List<RelationshipDefinitionDTO> definitions = relationshipPlatform.getRelationshipDefinitionsByEntity(entityName);
        return ResponseEntity.ok(definitions);
    }

    // ============ Relationship Instance APIs ============

    /**
     * Create a new relationship instance.
     */
    @PostMapping("/instances")
    public ResponseEntity<RelationshipInstanceDTO> createRelationshipInstance(
            @Valid @RequestBody RelationshipInstanceDTO instance) {
        try {
            RelationshipInstanceDTO created = relationshipPlatform.createRelationshipInstance(instance);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a relationship instance.
     */
    @DeleteMapping("/instances/{instanceId}")
    public ResponseEntity<Void> deleteRelationshipInstance(
            @PathVariable Long instanceId) {
        relationshipPlatform.deleteRelationshipInstance(instanceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all instances of a specific relationship.
     */
    @GetMapping("/instances/relationship/{relationshipName}")
    public ResponseEntity<List<RelationshipInstanceDTO>> getRelationshipInstances(
            @PathVariable String relationshipName) {
        List<RelationshipInstanceDTO> instances = relationshipPlatform.getRelationshipInstances(relationshipName);
        return ResponseEntity.ok(instances);
    }

    /**
     * Get all related entities for a given entity instance and relationship.
     */
    @GetMapping("/instances/related/{entityName}/{entityId}/{relationshipName}")
    public ResponseEntity<List<RelationshipInstanceDTO>> getRelatedEntities(
            @PathVariable String entityName,
            @PathVariable String entityId,
            @PathVariable String relationshipName) {
        List<RelationshipInstanceDTO> relatedEntities = relationshipPlatform.getRelatedEntities(entityName, entityId, relationshipName);
        return ResponseEntity.ok(relatedEntities);
    }

    /**
     * Get all related entities grouped by relationship name for a given entity instance.
     */
    @GetMapping("/instances/all-related/{entityName}/{entityId}")
    public ResponseEntity<Map<String, List<RelationshipInstanceDTO>>> getAllRelatedEntities(
            @PathVariable String entityName,
            @PathVariable String entityId) {
        Map<String, List<RelationshipInstanceDTO>> relatedEntities = relationshipPlatform.getAllRelatedEntities(entityName, entityId);
        return ResponseEntity.ok(relatedEntities);
    }

    // ============ Validation and Utility APIs ============

    /**
     * Register a validation rule for an entity type.
     * Products use this to provide their custom validation logic.
     */
    @PostMapping("/validation/rules/{entityType}")
    public ResponseEntity<String> registerValidationRule(
            @PathVariable String entityType,
            @RequestBody ValidationRule rule) {
        try {
            validationPlatform.registerValidationRule(entityType, rule);
            return ResponseEntity.ok("Validation rule registered successfully for entity: " + entityType);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to register validation rule: " + e.getMessage());
        }
    }

    /**
     * Register validation configurations for an entity type.
     * Products use this to provide declarative validation rules.
     */
    @PostMapping("/validation/configs/{entityType}")
    public ResponseEntity<String> registerValidationConfigs(
            @PathVariable String entityType,
            @RequestBody List<ValidationRuleDTO> configs) {
        try {
            validationPlatform.registerValidationConfigs(entityType, configs);
            return ResponseEntity.ok("Validation configurations registered successfully for entity: " + entityType);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to register validation configs: " + e.getMessage());
        }
    }

    /**
     * Register a single validation configuration for an entity type.
     */
    @PostMapping("/validation/config/{entityType}")
    public ResponseEntity<String> registerValidationConfig(
            @PathVariable String entityType,
            @RequestBody ValidationRuleDTO config) {
        try {
            validationPlatform.registerValidationConfig(entityType, config);
            return ResponseEntity.ok("Validation configuration registered successfully for entity: " + entityType);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to register validation config: " + e.getMessage());
        }
    }

    /**
     * Get all validation rules for an entity type.
     */
    @GetMapping("/validation/rules/{entityType}")
    public ResponseEntity<List<ValidationRule>> getValidationRules(@PathVariable String entityType) {
        List<ValidationRule> rules = validationPlatform.getValidationRules(entityType);
        return ResponseEntity.ok(rules);
    }

    /**
     * Get all validation configurations for an entity type.
     */
    @GetMapping("/validation/configs/{entityType}")
    public ResponseEntity<List<ValidationRuleDTO>> getValidationConfigs(@PathVariable String entityType) {
        return ResponseEntity.ok(validationPlatform.getValidationConfigs(entityType));
    }

    /**
     * Remove all validation rules for an entity type.
     */
    @DeleteMapping("/validation/rules/{entityType}")
    public ResponseEntity<String> removeValidationRules(@PathVariable String entityType) {
        try {
            // Note: This would need to be implemented in the interface
            return ResponseEntity.ok("Validation rules removal not yet implemented");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to remove validation rules: " + e.getMessage());
        }
    }

    /**
     * Remove all validation configurations for an entity type.
     */
    @DeleteMapping("/validation/configs/{entityType}")
    public ResponseEntity<String> removeValidationConfigs(@PathVariable String entityType) {
        try {
            validationPlatform.removeValidationConfigs(entityType);
            return ResponseEntity.ok("Validation configurations removed successfully for entity: " + entityType);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to remove validation configs: " + e.getMessage());
        }
    }

    /**
     * Validate a relationship.
     */
    @PostMapping("/validate/{relationshipName}")
    public ResponseEntity<Boolean> validateRelationship(
            @PathVariable String relationshipName,
            @RequestBody Map<String, String> entityReferences) {
        try {
            String sourceEntity = entityReferences.get("sourceEntity");
            String targetEntity = entityReferences.get("targetEntity");
            boolean isValid = relationshipPlatform.isValidRelationship(sourceEntity, targetEntity, relationshipName);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Relationship service is healthy");
    }
}

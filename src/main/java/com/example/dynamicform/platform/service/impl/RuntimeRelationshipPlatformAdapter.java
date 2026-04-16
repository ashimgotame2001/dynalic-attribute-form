package com.example.dynamicform.platform.service.impl;

import com.example.dynamicform.platform.service.RuntimeRelationshipPlatform;
import com.example.dynamicform.platform.api.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.platform.api.dto.RelationshipInstanceDTO;
import com.example.dynamicform.platform.service.RelationshipService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter that exposes product relationship capabilities through a platform port.
 */
@Service
public class RuntimeRelationshipPlatformAdapter implements RuntimeRelationshipPlatform {

    private final RelationshipService relationshipService;

    public RuntimeRelationshipPlatformAdapter(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @Override
    public RelationshipDefinitionDTO createRelationshipDefinition(RelationshipDefinitionDTO definition) {
        return relationshipService.createRelationshipDefinition(definition);
    }

    @Override
    public RelationshipDefinitionDTO updateRelationshipDefinition(String relationshipName, RelationshipDefinitionDTO definition) {
        return relationshipService.updateRelationshipDefinition(relationshipName, definition);
    }

    @Override
    public void deleteRelationshipDefinition(String relationshipName) {
        relationshipService.deleteRelationshipDefinition(relationshipName);
    }

    @Override
    public Optional<RelationshipDefinitionDTO> getRelationshipDefinition(String relationshipName) {
        return relationshipService.getRelationshipDefinition(relationshipName);
    }

    @Override
    public List<RelationshipDefinitionDTO> getAllRelationshipDefinitions() {
        return relationshipService.getAllRelationshipDefinitions();
    }

    @Override
    public List<RelationshipDefinitionDTO> getRelationshipDefinitionsByEntity(String entityName) {
        return relationshipService.getRelationshipDefinitionsByEntity(entityName);
    }

    @Override
    public RelationshipInstanceDTO createRelationshipInstance(RelationshipInstanceDTO instance) {
        return relationshipService.createRelationshipInstance(instance);
    }

    @Override
    public void deleteRelationshipInstance(Long instanceId) {
        relationshipService.deleteRelationshipInstance(instanceId);
    }

    @Override
    public List<RelationshipInstanceDTO> getRelationshipInstances(String relationshipName) {
        return relationshipService.getRelationshipInstances(relationshipName);
    }

    @Override
    public List<RelationshipInstanceDTO> getRelatedEntities(String entityName, String entityId, String relationshipName) {
        return relationshipService.getRelatedEntities(entityName, entityId, relationshipName);
    }

    @Override
    public Map<String, List<RelationshipInstanceDTO>> getAllRelatedEntities(String entityName, String entityId) {
        return relationshipService.getAllRelatedEntities(entityName, entityId);
    }

    @Override
    public boolean isValidRelationship(String sourceEntity, String targetEntity, String relationshipName) {
        return relationshipService.isValidRelationship(sourceEntity, targetEntity, relationshipName);
    }

    @Override
    public void validateRelationshipData(String relationshipName, Map<String, Object> data) {
        relationshipService.validateRelationshipData(relationshipName, data);
    }

    @Override
    public List<RelationshipInstanceDTO> getRelationshipHistory(String relationshipName, String sourceId, String targetId) {
        return relationshipService.getRelationshipHistory(relationshipName, sourceId, targetId);
    }
}

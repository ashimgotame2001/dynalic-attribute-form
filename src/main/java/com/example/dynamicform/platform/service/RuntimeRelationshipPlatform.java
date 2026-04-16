package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.api.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.platform.api.dto.RelationshipInstanceDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Platform-facing port for runtime relationship management.
 * Product code should depend on this abstraction rather than concrete relationship services.
 */
public interface RuntimeRelationshipPlatform {

    RelationshipDefinitionDTO createRelationshipDefinition(RelationshipDefinitionDTO definition);

    RelationshipDefinitionDTO updateRelationshipDefinition(String relationshipName, RelationshipDefinitionDTO definition);

    void deleteRelationshipDefinition(String relationshipName);

    Optional<RelationshipDefinitionDTO> getRelationshipDefinition(String relationshipName);

    List<RelationshipDefinitionDTO> getAllRelationshipDefinitions();

    List<RelationshipDefinitionDTO> getRelationshipDefinitionsByEntity(String entityName);

    RelationshipInstanceDTO createRelationshipInstance(RelationshipInstanceDTO instance);

    void deleteRelationshipInstance(Long instanceId);

    List<RelationshipInstanceDTO> getRelationshipInstances(String relationshipName);

    List<RelationshipInstanceDTO> getRelatedEntities(String entityName, String entityId, String relationshipName);

    Map<String, List<RelationshipInstanceDTO>> getAllRelatedEntities(String entityName, String entityId);

    boolean isValidRelationship(String sourceEntity, String targetEntity, String relationshipName);

    void validateRelationshipData(String relationshipName, Map<String, Object> data);

    List<RelationshipInstanceDTO> getRelationshipHistory(String relationshipName, String sourceId, String targetId);
}

package com.example.dynamicform.product.service;

import com.example.dynamicform.product.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.product.dto.RelationshipInstanceDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing dynamic relationships between domain models.
 * Provides methods for configuring relationships at runtime and querying relationship data.
 */
public interface RelationshipService {

    // Relationship Definition Management
    RelationshipDefinitionDTO createRelationshipDefinition(RelationshipDefinitionDTO definition);

    RelationshipDefinitionDTO updateRelationshipDefinition(String relationshipName, RelationshipDefinitionDTO definition);

    void deleteRelationshipDefinition(String relationshipName);

    Optional<RelationshipDefinitionDTO> getRelationshipDefinition(String relationshipName);

    List<RelationshipDefinitionDTO> getAllRelationshipDefinitions();

    List<RelationshipDefinitionDTO> getRelationshipDefinitionsByEntity(String entityName);

    // Relationship Instance Management
    RelationshipInstanceDTO createRelationshipInstance(RelationshipInstanceDTO instance);

    void deleteRelationshipInstance(Long instanceId);

    List<RelationshipInstanceDTO> getRelationshipInstances(String relationshipName);

    List<RelationshipInstanceDTO> getRelatedEntities(String entityName, String entityId, String relationshipName);

    Map<String, List<RelationshipInstanceDTO>> getAllRelatedEntities(String entityName, String entityId);

    // Validation and Utility
    boolean isValidRelationship(String sourceEntity, String targetEntity, String relationshipName);

    void validateRelationshipData(String relationshipName, Map<String, Object> data);

    // Audit and History
    List<RelationshipInstanceDTO> getRelationshipHistory(String relationshipName, String sourceId, String targetId);
}

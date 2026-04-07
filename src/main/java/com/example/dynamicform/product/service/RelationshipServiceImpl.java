package com.example.dynamicform.product.service;

import com.example.dynamicform.product.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.product.dto.RelationshipInstanceDTO;
import com.example.dynamicform.product.entity.RelationshipDefinitionEntity;
import com.example.dynamicform.product.repository.RelationshipDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Minimal product-side relationship service.
 * Product setup is definition-driven: sourceEntity + targetEntity + relationshipType.
 */
@Service
@Transactional
public class RelationshipServiceImpl implements RelationshipService {

    private final RelationshipDefinitionRepository definitionRepository;

    public RelationshipServiceImpl(RelationshipDefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    @Override
    public RelationshipDefinitionDTO createRelationshipDefinition(RelationshipDefinitionDTO definition) {
        validateDefinition(definition);

        String relationshipName = normalizeRelationshipName(definition);
        definitionRepository.findByRelationshipName(relationshipName)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Relationship definition already exists: " + relationshipName);
                });

        RelationshipDefinitionEntity entity = toEntity(definition, new RelationshipDefinitionEntity());
        entity.setRelationshipName(relationshipName);
        return toDto(definitionRepository.save(entity));
    }

    @Override
    public RelationshipDefinitionDTO updateRelationshipDefinition(String relationshipName, RelationshipDefinitionDTO definition) {
        validateDefinition(definition);

        RelationshipDefinitionEntity existing = definitionRepository.findByRelationshipName(relationshipName)
                .orElseThrow(() -> new IllegalArgumentException("Relationship definition not found: " + relationshipName));

        RelationshipDefinitionEntity updated = toEntity(definition, existing);
        updated.setRelationshipName(relationshipName);
        return toDto(definitionRepository.save(updated));
    }

    @Override
    public void deleteRelationshipDefinition(String relationshipName) {
        definitionRepository.findByRelationshipName(relationshipName)
                .ifPresent(definitionRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RelationshipDefinitionDTO> getRelationshipDefinition(String relationshipName) {
        return definitionRepository.findByRelationshipName(relationshipName)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelationshipDefinitionDTO> getAllRelationshipDefinitions() {
        return definitionRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelationshipDefinitionDTO> getRelationshipDefinitionsByEntity(String entityName) {
        return definitionRepository.findByEntityInvolved(entityName).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RelationshipInstanceDTO createRelationshipInstance(RelationshipInstanceDTO instance) {
        throw new UnsupportedOperationException("Relationship instances are not part of the new product relationship flow");
    }

    @Override
    public void deleteRelationshipInstance(Long instanceId) {
        // Definition-driven metadata generation does not use relationship instances.
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelationshipInstanceDTO> getRelationshipInstances(String relationshipName) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelationshipInstanceDTO> getRelatedEntities(String entityName, String entityId, String relationshipName) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<RelationshipInstanceDTO>> getAllRelatedEntities(String entityName, String entityId) {
        return Map.of();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidRelationship(String sourceEntity, String targetEntity, String relationshipName) {
        return definitionRepository.findByRelationshipName(relationshipName)
                .filter(RelationshipDefinitionEntity::isActive)
                .map(entity -> entity.getSourceEntity().equalsIgnoreCase(sourceEntity)
                        && entity.getTargetEntity().equalsIgnoreCase(targetEntity))
                .orElse(false);
    }

    @Override
    public void validateRelationshipData(String relationshipName, Map<String, Object> data) {
        if (data == null) {
            throw new IllegalArgumentException("Relationship data cannot be null");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelationshipInstanceDTO> getRelationshipHistory(String relationshipName, String sourceId, String targetId) {
        return List.of();
    }

    private void validateDefinition(RelationshipDefinitionDTO definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Relationship definition is required");
        }
        if (isBlank(definition.getSourceEntity())) {
            throw new IllegalArgumentException("sourceEntity is required");
        }
        if (isBlank(definition.getTargetEntity())) {
            throw new IllegalArgumentException("targetEntity is required");
        }
        if (definition.getRelationshipType() == null) {
            throw new IllegalArgumentException("relationshipType is required");
        }
    }

    private RelationshipDefinitionEntity toEntity(RelationshipDefinitionDTO dto, RelationshipDefinitionEntity entity) {
        boolean isCreate = entity.getId() == null;
        entity.setSourceEntity(normalizeEntityName(dto.getSourceEntity()));
        entity.setTargetEntity(normalizeEntityName(dto.getTargetEntity()));
        entity.setRelationshipType(dto.getRelationshipType());
        entity.setSourceKeyField(defaultIfBlank(dto.getSourceKeyField(), "id"));
        entity.setTargetKeyField(defaultIfBlank(dto.getTargetKeyField(), "id"));
        entity.setDescription(defaultIfBlank(dto.getDescription(),
                entity.getSourceEntity() + " -> " + entity.getTargetEntity()));
        entity.setActive(isCreate || dto.isActive());
        entity.setCreatedBy(defaultIfBlank(dto.getCreatedBy(), "system"));
        entity.setUpdatedBy(defaultIfBlank(dto.getUpdatedBy(), "system"));
        return entity;
    }

    private RelationshipDefinitionDTO toDto(RelationshipDefinitionEntity entity) {
        RelationshipDefinitionDTO dto = new RelationshipDefinitionDTO();
        dto.setId(entity.getId());
        dto.setRelationshipName(entity.getRelationshipName());
        dto.setSourceEntity(entity.getSourceEntity());
        dto.setTargetEntity(entity.getTargetEntity());
        dto.setRelationshipType(entity.getRelationshipType());
        dto.setSourceKeyField(entity.getSourceKeyField());
        dto.setTargetKeyField(entity.getTargetKeyField());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    private String normalizeRelationshipName(RelationshipDefinitionDTO definition) {
        if (!isBlank(definition.getRelationshipName())) {
            return definition.getRelationshipName().trim();
        }
        return normalizeEntityName(definition.getSourceEntity()) + "_" + normalizeEntityName(definition.getTargetEntity());
    }

    private String normalizeEntityName(String value) {
        return value == null ? null : value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

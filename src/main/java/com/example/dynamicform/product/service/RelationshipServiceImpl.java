package com.example.dynamicform.product.service;

import com.example.dynamicform.product.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.product.dto.RelationshipInstanceDTO;
import com.example.dynamicform.product.entity.RelationshipDefinitionEntity;
import com.example.dynamicform.product.entity.RelationshipInstanceEntity;
import com.example.dynamicform.product.repository.RelationshipDefinitionRepository;
import com.example.dynamicform.product.repository.RelationshipInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of RelationshipService for managing dynamic relationships.
 */
@Service
@Transactional
public class RelationshipServiceImpl implements RelationshipService {

    @Autowired
    private RelationshipDefinitionRepository definitionRepository;

    @Autowired
    private RelationshipInstanceRepository instanceRepository;

    @Autowired
    private RelationshipAuditService auditService; // Will be created later

    @Override
    @CacheEvict(value = "relationshipDefinitions", allEntries = true)
    public RelationshipDefinitionDTO createRelationshipDefinition(RelationshipDefinitionDTO definition) {
        RelationshipDefinitionEntity entity = convertToEntity(definition);
        entity = definitionRepository.save(entity);
        auditService.logDefinitionChange(entity, "CREATE", null);
        return convertToDTO(entity);
    }

    @Override
    @CacheEvict(value = "relationshipDefinitions", allEntries = true)
    public RelationshipDefinitionDTO updateRelationshipDefinition(String relationshipName, RelationshipDefinitionDTO definition) {
        Optional<RelationshipDefinitionEntity> existing = definitionRepository.findByRelationshipName(relationshipName);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Relationship definition not found: " + relationshipName);
        }

        RelationshipDefinitionEntity oldEntity = existing.get();
        RelationshipDefinitionEntity updatedEntity = convertToEntity(definition);
        updatedEntity.setId(oldEntity.getId());
        updatedEntity.setCreatedAt(oldEntity.getCreatedAt());
        updatedEntity.setCreatedBy(oldEntity.getCreatedBy());

        updatedEntity = definitionRepository.save(updatedEntity);
        auditService.logDefinitionChange(updatedEntity, "UPDATE", oldEntity);
        return convertToDTO(updatedEntity);
    }

    @Override
    @CacheEvict(value = "relationshipDefinitions", allEntries = true)
    public void deleteRelationshipDefinition(String relationshipName) {
        Optional<RelationshipDefinitionEntity> existing = definitionRepository.findByRelationshipName(relationshipName);
        if (existing.isPresent()) {
            RelationshipDefinitionEntity entity = existing.get();
            definitionRepository.delete(entity);
            auditService.logDefinitionChange(entity, "DELETE", entity);
        }
    }

    @Override
    @Cacheable(value = "relationshipDefinitions", key = "#relationshipName")
    public Optional<RelationshipDefinitionDTO> getRelationshipDefinition(String relationshipName) {
        return definitionRepository.findByRelationshipName(relationshipName)
                .map(this::convertToDTO);
    }

    @Override
    @Cacheable(value = "relationshipDefinitions", key = "'all'")
    public List<RelationshipDefinitionDTO> getAllRelationshipDefinitions() {
        return definitionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "relationshipDefinitions", key = "#entityName")
    public List<RelationshipDefinitionDTO> getRelationshipDefinitionsByEntity(String entityName) {
        return definitionRepository.findByEntityInvolved(entityName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RelationshipInstanceDTO createRelationshipInstance(RelationshipInstanceDTO instance) {
        Optional<RelationshipDefinitionEntity> definition = definitionRepository.findByRelationshipName(instance.getRelationshipName());
        if (definition.isEmpty()) {
            throw new IllegalArgumentException("Relationship definition not found: " + instance.getRelationshipName());
        }

        RelationshipInstanceEntity entity = convertToEntity(instance, definition.get());
        entity = instanceRepository.save(entity);
        auditService.logInstanceChange(entity, "CREATE", null);
        return convertToDTO(entity);
    }

    @Override
    public void deleteRelationshipInstance(Long instanceId) {
        Optional<RelationshipInstanceEntity> existing = instanceRepository.findById(instanceId);
        if (existing.isPresent()) {
            RelationshipInstanceEntity entity = existing.get();
            instanceRepository.delete(entity);
            auditService.logInstanceChange(entity, "DELETE", entity);
        }
    }

    @Override
    public List<RelationshipInstanceDTO> getRelationshipInstances(String relationshipName) {
        return instanceRepository.findActiveByRelationshipName(relationshipName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RelationshipInstanceDTO> getRelatedEntities(String entityName, String entityId, String relationshipName) {
        return instanceRepository.findActiveBySourceIdAndRelationship(entityId, relationshipName).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<RelationshipInstanceDTO>> getAllRelatedEntities(String entityName, String entityId) {
        List<RelationshipInstanceEntity> instances = instanceRepository.findActiveBySourceEntityId(entityId, entityName);
        return instances.stream()
                .collect(Collectors.groupingBy(
                        instance -> instance.getRelationshipDefinition().getRelationshipName(),
                        Collectors.mapping(this::convertToDTO, Collectors.toList())
                ));
    }

    @Override
    public boolean isValidRelationship(String sourceEntity, String targetEntity, String relationshipName) {
        Optional<RelationshipDefinitionEntity> definition = definitionRepository.findByRelationshipName(relationshipName);
        return definition.isPresent() &&
               definition.get().getSourceEntity().equals(sourceEntity) &&
               definition.get().getTargetEntity().equals(targetEntity) &&
               definition.get().isActive();
    }

    @Override
    public void validateRelationshipData(String relationshipName, Map<String, Object> data) {
        // Basic validation - can be extended
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Relationship data cannot be null or empty");
        }
    }

    @Override
    public List<RelationshipInstanceDTO> getRelationshipHistory(String relationshipName, String sourceId, String targetId) {
        // This would require audit tables - simplified for now
        return getRelationshipInstances(relationshipName).stream()
                .filter(instance -> instance.getSourceEntityId().equals(sourceId) &&
                                   instance.getTargetEntityId().equals(targetId))
                .collect(Collectors.toList());
    }

    // Conversion methods
    private RelationshipDefinitionDTO convertToDTO(RelationshipDefinitionEntity entity) {
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

    private RelationshipDefinitionEntity convertToEntity(RelationshipDefinitionDTO dto) {
        RelationshipDefinitionEntity entity = new RelationshipDefinitionEntity();
        entity.setRelationshipName(dto.getRelationshipName());
        entity.setSourceEntity(dto.getSourceEntity());
        entity.setTargetEntity(dto.getTargetEntity());
        entity.setRelationshipType(dto.getRelationshipType());
        entity.setSourceKeyField(dto.getSourceKeyField());
        entity.setTargetKeyField(dto.getTargetKeyField());
        entity.setDescription(dto.getDescription());
        entity.setActive(dto.isActive());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        return entity;
    }

    private RelationshipInstanceDTO convertToDTO(RelationshipInstanceEntity entity) {
        RelationshipInstanceDTO dto = new RelationshipInstanceDTO();
        dto.setId(entity.getId());
        dto.setRelationshipName(entity.getRelationshipDefinition().getRelationshipName());
        dto.setRelationshipDefinitionId(entity.getRelationshipDefinition().getId());
        dto.setSourceEntityId(entity.getSourceEntityId());
        dto.setTargetEntityId(entity.getTargetEntityId());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setMetadata(entity.getMetadata());
        return dto;
    }

    private RelationshipInstanceEntity convertToEntity(RelationshipInstanceDTO dto, RelationshipDefinitionEntity definition) {
        RelationshipInstanceEntity entity = new RelationshipInstanceEntity();
        entity.setRelationshipDefinition(definition);
        entity.setSourceEntityId(dto.getSourceEntityId());
        entity.setTargetEntityId(dto.getTargetEntityId());
        entity.setActive(dto.isActive());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setMetadata(dto.getMetadata());
        return entity;
    }
}

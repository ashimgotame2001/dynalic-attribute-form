package com.example.dynamicform.product.service;

import com.example.dynamicform.product.entity.RelationshipAuditEntity;
import com.example.dynamicform.product.entity.RelationshipDefinitionEntity;
import com.example.dynamicform.product.entity.RelationshipInstanceEntity;
import com.example.dynamicform.product.enums.AuditAction;
import com.example.dynamicform.product.repository.RelationshipAuditRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for auditing relationship changes.
 */
@Service
public class RelationshipAuditService {

    @Autowired
    private RelationshipAuditRepository auditRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @CacheEvict(value = "auditTrail", allEntries = true)
    public void logDefinitionChange(RelationshipDefinitionEntity entity, String action, RelationshipDefinitionEntity oldEntity) {
        RelationshipAuditEntity audit = new RelationshipAuditEntity();
        audit.setAction(AuditAction.valueOf(action));
        audit.setEntityType("DEFINITION");
        audit.setEntityId(entity.getId().toString());
        audit.setUserId(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : entity.getCreatedBy());

        try {
            audit.setNewValue(objectMapper.writeValueAsString(entity));
            if (oldEntity != null) {
                audit.setOldValue(objectMapper.writeValueAsString(oldEntity));
            }
        } catch (JsonProcessingException e) {
            // Log error but don't fail the operation
            audit.setNewValue("Error serializing entity");
        }

        auditRepository.save(audit);
    }

    @CacheEvict(value = "auditTrail", allEntries = true)
    public void logInstanceChange(RelationshipInstanceEntity entity, String action, RelationshipInstanceEntity oldEntity) {
        RelationshipAuditEntity audit = new RelationshipAuditEntity();
        audit.setAction(AuditAction.valueOf(action));
        audit.setEntityType("INSTANCE");
        audit.setEntityId(entity.getId().toString());
        audit.setUserId(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : entity.getCreatedBy());

        try {
            audit.setNewValue(objectMapper.writeValueAsString(entity));
            if (oldEntity != null) {
                audit.setOldValue(objectMapper.writeValueAsString(oldEntity));
            }
        } catch (JsonProcessingException e) {
            audit.setNewValue("Error serializing entity");
        }

        auditRepository.save(audit);
    }

    @CacheEvict(value = "auditTrail", allEntries = true)
    public void logDataChange(String entityType, String entityId, String action,
                              Map<String, Object> oldValue, Map<String, Object> newValue, String userId) {
        logGenericChange("DATA:" + entityType, entityId, action, oldValue, newValue, userId);
    }

    @CacheEvict(value = "auditTrail", allEntries = true)
    public void logSchemaChange(String schemaName, String schemaId, String action,
                                Object oldValue, Object newValue, String userId) {
        logGenericChange("SCHEMA:" + schemaName, schemaId, action, oldValue, newValue, userId);
    }

    @Cacheable(value = "auditTrail", key = "#entityType + ':' + #entityId")
    public List<RelationshipAuditEntity> getAuditHistory(String entityType, String entityId) {
        return auditRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    private void logGenericChange(String entityType, String entityId, String action,
                                  Object oldValue, Object newValue, String userId) {
        RelationshipAuditEntity audit = new RelationshipAuditEntity();
        audit.setAction(AuditAction.valueOf(action));
        audit.setEntityType(entityType);
        audit.setEntityId(entityId);
        audit.setUserId(userId == null || userId.isBlank() ? "system" : userId);

        try {
            if (newValue != null) {
                audit.setNewValue(objectMapper.writeValueAsString(newValue));
            }
            if (oldValue != null) {
                audit.setOldValue(objectMapper.writeValueAsString(oldValue));
            }
        } catch (JsonProcessingException e) {
            audit.setNewValue("Error serializing audit payload");
        }

        auditRepository.save(audit);
    }
}

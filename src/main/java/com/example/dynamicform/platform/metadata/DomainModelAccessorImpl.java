package com.example.dynamicform.platform.metadata;

import com.example.dynamicform.platform.compatibility.BackwardCompatibilityService;
import com.example.dynamicform.platform.service.RuntimeRelationshipPlatform;
import com.example.dynamicform.product.dto.RelationshipInstanceDTO;
import com.example.dynamicform.product.service.RelationshipAuditService;
import com.example.dynamicform.platform.validation.ConstraintValidationEngine;
import com.example.dynamicform.platform.validation.ValidationError;
import com.example.dynamicform.platform.validation.DomainIntegrityValidationService;
import com.example.dynamicform.platform.versioning.SchemaVersion;
import com.example.dynamicform.platform.versioning.SchemaVersioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of DomainModelAccessor.
 * Manages access to domain models while transparently handling relationships and persistence.
 */
public class DomainModelAccessorImpl implements DomainModelAccessor<Map<String, Object>> {

    private static final Logger logger = LoggerFactory.getLogger(DomainModelAccessorImpl.class);

    private final DomainModelPersistence<Map<String, Object>> persistence;
    private final RuntimeRelationshipPlatform relationshipPlatform;
    private final ConstraintValidationEngine validationEngine;
    private final DomainIntegrityValidationService domainIntegrityService;
    private final BackwardCompatibilityService backwardCompatibilityService;
    private final SchemaVersioningService schemaVersioningService;
    private final RelationshipAuditService auditService;

    private String entityTypeName = "DomainModel";

    public DomainModelAccessorImpl(DomainModelPersistence<Map<String, Object>> persistence,
                                   RuntimeRelationshipPlatform relationshipPlatform,
                                   ConstraintValidationEngine validationEngine,
                                   DomainIntegrityValidationService domainIntegrityService,
                                   BackwardCompatibilityService backwardCompatibilityService,
                                   SchemaVersioningService schemaVersioningService,
                                   RelationshipAuditService auditService,
                                   String entityTypeName) {
        this.persistence = persistence;
        this.relationshipPlatform = relationshipPlatform;
        this.validationEngine = validationEngine;
        this.domainIntegrityService = domainIntegrityService;
        this.backwardCompatibilityService = backwardCompatibilityService;
        this.schemaVersioningService = schemaVersioningService;
        this.auditService = auditService;
        this.entityTypeName = entityTypeName;
    }

    @Override
    @CacheEvict(value = "domainModelData", allEntries = true)
    public Map<String, Object> save(String entityId, Map<String, Object> data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        Map<String, Object> previous = persistence.findById(entityId, entityTypeName)
                .map(HashMap::new)
                .orElse(null);
        Map<String, Object> payload = applyCompatibility(entityTypeName, data);

        // Perform tight domain integrity validation
        DomainIntegrityValidationService.DomainValidationResult integrityResult =
            domainIntegrityService.validateDomainIntegrity("SAVE", entityTypeName, entityId, payload);

        if (!integrityResult.isValid()) {
            String errorMessage = integrityResult.getErrors().stream()
                    .map(ValidationError::getMessage)
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Domain integrity validation failed: " + errorMessage);
        }

        // Additional constraint validation
        List<ValidationError> validationErrors = validationEngine.validate(entityTypeName, entityId, payload);
        if (!validationErrors.isEmpty()) {
            String errorMessage = validationErrors.stream()
                    .map(ValidationError::getMessage)
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Validation failed: " + errorMessage);
        }

        Map<String, Object> saved = persistence.save(entityId, entityTypeName, payload);
        auditService.logDataChange(entityTypeName, entityId, previous == null ? "CREATE" : "UPDATE", previous, saved, "system");
        logger.info("Saved entity: {}/{} with domain integrity validated", entityTypeName, entityId);
        return saved;
    }

    @Override
    @Cacheable(value = "domainModelData", key = "#entityId + '_with_relations'")
    public Optional<Map<String, Object>> findWithRelationships(String entityId) {
        Optional<Map<String, Object>> entity = persistence.findById(entityId, entityTypeName);
        if (entity.isPresent()) {
            Map<String, Object> data = entity.get();
            // Fetch all related entities
            Map<String, List<RelationshipInstanceDTO>> relatedMap = relationshipPlatform.getAllRelatedEntities(entityTypeName, entityId);

            // Add relationships to the entity data
            data.put("_relationships", relatedMap);
            logger.debug("Loaded entity with relationships: {}/{}", entityTypeName, entityId);
        }
        return entity;
    }

    @Override
    @Cacheable(value = "domainModelData", key = "#entityId")
    public Optional<Map<String, Object>> findById(String entityId) {
        return persistence.findById(entityId, entityTypeName);
    }

    @Override
    @Cacheable(value = "domainModelData", key = "'all_' + #root.target.entityTypeName")
    public List<Map<String, Object>> findAll() {
        return persistence.findAll(entityTypeName);
    }

    @Override
    @Cacheable(value = "domainModelData", key = "#criteria.toString() + '_' + #root.target.entityTypeName")
    public List<Map<String, Object>> findByCriteria(Map<String, Object> criteria) {
        return persistence.findByCriteria(entityTypeName, criteria);
    }

    @Override
    public boolean delete(String entityId) {
        Optional<Map<String, Object>> previous = persistence.findById(entityId, entityTypeName);
        boolean deleted = persistence.delete(entityId, entityTypeName);
        if (deleted) {
            auditService.logDataChange(entityTypeName, entityId, "DELETE", previous.orElse(null), null, "system");
            logger.info("Deleted entity: {}/{}", entityTypeName, entityId);
        }
        return deleted;
    }

    @Override
    public boolean exists(String entityId) {
        return persistence.exists(entityId, entityTypeName);
    }

    @Override
    public List<Map<String, Object>> getRelated(String entityId, String relationshipName) {
        List<RelationshipInstanceDTO> relationships = relationshipPlatform.getRelatedEntities(entityTypeName, entityId, relationshipName);
        Optional<com.example.dynamicform.product.dto.RelationshipDefinitionDTO> definition =
                relationshipPlatform.getRelationshipDefinition(relationshipName);
        if (definition.isEmpty()) {
            return List.of();
        }

        return relationships.stream()
                .map(RelationshipInstanceDTO::getTargetEntityId)
                .map(targetId -> persistence.findById(targetId, definition.get().getTargetEntity()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void link(String sourceId, String targetId, String relationshipName) {
        if (!exists(sourceId)) {
            throw new IllegalArgumentException("Source entity not found: " + sourceId);
        }

        // Validate relationship constraints
        List<ValidationError> relationshipErrors = validationEngine.validateRelationship(
            relationshipName, sourceId, targetId);
        if (!relationshipErrors.isEmpty()) {
            String errorMessage = relationshipErrors.stream()
                    .map(ValidationError::getMessage)
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Relationship validation failed: " + errorMessage);
        }

        RelationshipInstanceDTO instance = new RelationshipInstanceDTO();
        instance.setRelationshipName(relationshipName);
        instance.setSourceEntityId(sourceId);
        instance.setTargetEntityId(targetId);

        relationshipPlatform.createRelationshipInstance(instance);
        logger.info("Linked entities: {} -> {} via {}", sourceId, targetId, relationshipName);
    }

    @Override
    public void unlink(String sourceId, String targetId, String relationshipName) {
        List<RelationshipInstanceDTO> instances = relationshipPlatform.getRelatedEntities(entityTypeName, sourceId, relationshipName);
        instances.stream()
                .filter(instance -> instance.getTargetEntityId().equals(targetId))
                .forEach(instance -> relationshipPlatform.deleteRelationshipInstance(instance.getId()));

        logger.info("Unlinked entities: {} -> {} via {}", sourceId, targetId, relationshipName);
    }

    @Override
    public void updateField(String entityId, String fieldName, Object value) {
        persistence.updateField(entityId, entityTypeName, fieldName, value);
        logger.debug("Updated field: {}/{}/{}", entityTypeName, entityId, fieldName);
    }

    @Override
    public String getEntityTypeName() {
        return entityTypeName;
    }

    /**
     * Set entity type name for this accessor instance.
     */
    public void setEntityTypeName(String entityTypeName) {
        this.entityTypeName = entityTypeName;
    }

    private Map<String, Object> applyCompatibility(String schemaName, Map<String, Object> data) {
        Map<String, Object> compatible = new HashMap<>(data);
        int dataVersion = resolveSchemaVersion(compatible);
        Optional<SchemaVersion> latestVersion = schemaVersioningService.getLatestSchemaVersion(schemaName);
        if (latestVersion.isEmpty()) {
            compatible.putIfAbsent("_schemaVersion", dataVersion > 0 ? dataVersion : 1);
            return compatible;
        }

        int targetVersion = latestVersion.get().getVersionNumber();
        compatible = backwardCompatibilityService.ensureCompatibility(schemaName, dataVersion, targetVersion, compatible);
        compatible = backwardCompatibilityService.addDefaultsForNewRequiredFields(schemaName, targetVersion, compatible);
        compatible.put("_schemaVersion", targetVersion);
        return compatible;
    }

    private int resolveSchemaVersion(Map<String, Object> data) {
        Object schemaVersion = data.get("_schemaVersion");
        if (schemaVersion instanceof Number number) {
            return number.intValue();
        }
        Optional<SchemaVersion> latestVersion = schemaVersioningService.getLatestSchemaVersion(entityTypeName);
        return latestVersion.map(SchemaVersion::getVersionNumber).orElse(1);
    }
}

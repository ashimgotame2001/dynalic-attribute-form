package com.example.dynamicform.platform.metadata;

import com.example.dynamicform.platform.compatibility.BackwardCompatibilityService;
import com.example.dynamicform.platform.service.RuntimeRelationshipPlatform;
import com.example.dynamicform.platform.validation.ConstraintValidationEngine;
import com.example.dynamicform.platform.validation.DomainIntegrityValidationService;
import com.example.dynamicform.platform.versioning.SchemaVersioningService;
import com.example.dynamicform.product.service.RelationshipAuditService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Spring-managed runtime access layer that provisions entity-specific accessors with
 * the required platform services attached.
 */
@Component
public class DomainModelAccessLayerImpl implements DomainModelAccessLayer {

    private final DomainModelPersistence<Map<String, Object>> persistence;
    private final RuntimeRelationshipPlatform relationshipPlatform;
    private final ConstraintValidationEngine validationEngine;
    private final DomainIntegrityValidationService domainIntegrityValidationService;
    private final BackwardCompatibilityService backwardCompatibilityService;
    private final SchemaVersioningService schemaVersioningService;
    private final RelationshipAuditService auditService;
    private final ConcurrentMap<String, DomainModelAccessor<?>> accessors = new ConcurrentHashMap<>();

    public DomainModelAccessLayerImpl(
            DomainModelPersistence<Map<String, Object>> persistence,
            RuntimeRelationshipPlatform relationshipPlatform,
            ConstraintValidationEngine validationEngine,
            DomainIntegrityValidationService domainIntegrityValidationService,
            BackwardCompatibilityService backwardCompatibilityService,
            SchemaVersioningService schemaVersioningService,
            RelationshipAuditService auditService) {
        this.persistence = persistence;
        this.relationshipPlatform = relationshipPlatform;
        this.validationEngine = validationEngine;
        this.domainIntegrityValidationService = domainIntegrityValidationService;
        this.backwardCompatibilityService = backwardCompatibilityService;
        this.schemaVersioningService = schemaVersioningService;
        this.auditService = auditService;
    }

    @Override
    public DomainModelAccessor<?> forEntity(String entityTypeName) {
        return accessors.computeIfAbsent(entityTypeName, this::createAccessor);
    }

    @Override
    public Set<String> getRegisteredEntityTypes() {
        return accessors.keySet();
    }

    private DomainModelAccessor<?> createAccessor(String entityTypeName) {
        return new DomainModelAccessorImpl(
                persistence,
                relationshipPlatform,
                validationEngine,
                domainIntegrityValidationService,
                backwardCompatibilityService,
                schemaVersioningService,
                auditService,
                entityTypeName
        );
    }
}

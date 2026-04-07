package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.dto.PlatformAuditEntryResponse;
import com.example.dynamicform.platform.dto.PlatformEntityResponse;
import com.example.dynamicform.platform.dto.PlatformRuntimeContractResponse;
import com.example.dynamicform.platform.metadata.DomainModelAccessLayer;
import com.example.dynamicform.platform.metadata.DomainModelAccessor;
import com.example.dynamicform.platform.versioning.SchemaVersion;
import com.example.dynamicform.platform.versioning.SchemaVersioningService;
import com.example.dynamicform.product.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.product.dto.RelationshipInstanceDTO;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Coordinates the JSON-facing runtime platform contract for dynamic model access.
 */
@Service
public class PlatformRuntimeService {

    private static final List<String> CACHE_REGIONS = List.of(
            "formDefinitions",
            "metadataDefinitions",
            "relationshipDefinitions",
            "relationshipInstances",
            "domainModelData",
            "schemaVersions",
            "auditTrail",
            "validationConfigs"
    );

    private final DomainModelAccessLayer accessLayer;
    private final RuntimeRelationshipPlatform relationshipPlatform;
    private final SchemaVersioningService schemaVersioningService;

    public PlatformRuntimeService(
            DomainModelAccessLayer accessLayer,
            RuntimeRelationshipPlatform relationshipPlatform,
            SchemaVersioningService schemaVersioningService) {
        this.accessLayer = accessLayer;
        this.relationshipPlatform = relationshipPlatform;
        this.schemaVersioningService = schemaVersioningService;
    }

    public PlatformEntityResponse save(String entityType, String entityId, Map<String, Object> payload) {
        accessor(entityType).save(entityId, payload);
        return getEntity(entityType, entityId, true)
                .orElseThrow(() -> new IllegalStateException("Saved entity could not be reloaded"));
    }

    public Optional<PlatformEntityResponse> getEntity(String entityType, String entityId, boolean includeRelationships) {
        DomainModelAccessor<?> accessor = accessor(entityType);
        @SuppressWarnings("unchecked")
        Optional<Map<String, Object>> raw = includeRelationships
                ? (Optional<Map<String, Object>>) accessor.findWithRelationships(entityId)
                : (Optional<Map<String, Object>>) accessor.findById(entityId);

        return raw.map(data -> buildEntityResponse(entityType, entityId, data));
    }

    public List<Map<String, Object>> getAll(String entityType) {
        @SuppressWarnings("unchecked")
        DomainModelAccessor<Map<String, Object>> accessor = (DomainModelAccessor<Map<String, Object>>) accessor(entityType);
        return accessor.findAll();
    }

    public void link(String entityType, String entityId, String relationshipName, String targetId) {
        @SuppressWarnings("unchecked")
        DomainModelAccessor<Map<String, Object>> accessor = (DomainModelAccessor<Map<String, Object>>) accessor(entityType);
        accessor.link(entityId, targetId, relationshipName);
    }

    public void unlink(String entityType, String entityId, String relationshipName, String targetId) {
        @SuppressWarnings("unchecked")
        DomainModelAccessor<Map<String, Object>> accessor = (DomainModelAccessor<Map<String, Object>>) accessor(entityType);
        accessor.unlink(entityId, targetId, relationshipName);
    }

    public PlatformRuntimeContractResponse getContract(String entityType) {
        Optional<SchemaVersion> latestSchema = schemaVersioningService.getLatestSchemaVersion(entityType);
        List<String> relationships = relationshipPlatform.getRelationshipDefinitionsByEntity(entityType).stream()
                .map(RelationshipDefinitionDTO::getRelationshipName)
                .distinct()
                .toList();

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("latestVersion", latestSchema.map(SchemaVersion::getVersionNumber).orElse(0));
        schema.put("registeredVersions", schemaVersioningService.getAllVersions(entityType).size());
        schema.put("migrationPath", schemaVersioningService.getMigrationPath(entityType));

        return new PlatformRuntimeContractResponse(
                entityType,
                "json",
                true,
                true,
                "DomainModelPersistence",
                "DomainModelAccessLayer -> DomainModelAccessor",
                true,
                true,
                CACHE_REGIONS,
                "ConstraintValidationEngine",
                schema,
                relationships
        );
    }

    private PlatformEntityResponse buildEntityResponse(String entityType, String entityId, Map<String, Object> data) {
        Map<String, Object> dataSection = new LinkedHashMap<>(data);
        Map<String, List<Map<String, Object>>> relationships = Map.of();
        Object relationshipPayload = dataSection.remove("_relationships");
        if (relationshipPayload instanceof Map<?, ?> rawRelationships) {
            relationships = rawRelationships.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> String.valueOf(entry.getKey()),
                            entry -> normalizeRelationshipPayload(entry.getValue()),
                            (left, right) -> right,
                            LinkedHashMap::new
                    ));
        }

        int schemaVersion = extractSchemaVersion(dataSection);
        Optional<SchemaVersion> latestSchema = schemaVersioningService.getLatestSchemaVersion(entityType);
        boolean compatible = latestSchema.isEmpty() || latestSchema.get().getVersionNumber() == schemaVersion;

        List<PlatformAuditEntryResponse> auditTrail = List.of();

        Map<String, Object> platform = new LinkedHashMap<>();
        platform.put("persistenceAbstraction", "DomainModelPersistence");
        platform.put("accessLayer", "DomainModelAccessor");
        platform.put("validationEngine", "ConstraintValidationEngine");
        platform.put("cacheRegions", CACHE_REGIONS);
        platform.put("registeredRelationships", relationshipPlatform.getRelationshipDefinitionsByEntity(entityType).size());

        return new PlatformEntityResponse(
                entityType,
                entityId,
                schemaVersion,
                compatible,
                dataSection,
                relationships,
                auditTrail,
                platform
        );
    }

    private int extractSchemaVersion(Map<String, Object> data) {
        Object value = data.get("_schemaVersion");
        return value instanceof Number number ? number.intValue() : 1;
    }

    private List<Map<String, Object>> normalizeRelationshipPayload(Object value) {
        if (!(value instanceof List<?> relationshipInstances)) {
            return List.of();
        }

        return relationshipInstances.stream()
                .map(item -> {
                    if (!(item instanceof RelationshipInstanceDTO instance)) {
                        return Map.<String, Object>of("value", item);
                    }

                    Map<String, Object> normalized = new LinkedHashMap<>();
                    normalized.put("id", instance.getId());
                    normalized.put("sourceEntityId", instance.getSourceEntityId());
                    normalized.put("targetEntityId", instance.getTargetEntityId());
                    normalized.put("active", instance.isActive());
                    normalized.put("metadata", instance.getMetadata());
                    return normalized;
                })
                .collect(Collectors.toList());
    }

    private DomainModelAccessor<?> accessor(String entityType) {
        return accessLayer.forEntity(entityType);
    }
}

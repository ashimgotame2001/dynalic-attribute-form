package com.example.dynamicform.platform.versioning;

import com.example.dynamicform.product.service.RelationshipAuditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing schema versions and ensuring backward compatibility.
 * Supports schema evolution, data migration, and validation against multiple schema versions.
 */
@Service
public class SchemaVersioningService {

    private static final Logger logger = LoggerFactory.getLogger(SchemaVersioningService.class);

    private final ObjectMapper objectMapper;
    private final RelationshipAuditService auditService;
    // schemaName -> list of versions sorted by versionNumber
    private final Map<String, List<SchemaVersion>> schemaVersions = new HashMap<>();

    public SchemaVersioningService(ObjectMapper objectMapper, RelationshipAuditService auditService) {
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    /**
     * Register a new schema version.
     *
     * @param schemaName the name of the schema
     * @param schemaDefinition JSON representation of the schema
     * @param description description of changes in this version
     * @return the created SchemaVersion
     */
    public SchemaVersion registerSchema(String schemaName, String schemaDefinition, String description) {
        List<SchemaVersion> versions = schemaVersions.computeIfAbsent(schemaName, k -> new ArrayList<>());

        int nextVersion = versions.size() + 1;
        SchemaVersion version = new SchemaVersion(schemaName, nextVersion, schemaDefinition);
        version.setDescription(description);
        version.setCreatedAt(LocalDateTime.now());
        version.setCreatedBy("system");

        if (!versions.isEmpty()) {
            version.setPreviousVersionId(String.valueOf(versions.get(versions.size() - 1).getId()));
        }

        versions.add(version);
        auditService.logSchemaChange(schemaName, String.valueOf(nextVersion), "CREATE", null, version, version.getCreatedBy());
        logger.info("Registered schema version: {} v{}", schemaName, nextVersion);
        return version;
    }

    /**
     * Get a specific schema version.
     *
     * @param schemaName the schema name
     * @param versionNumber the version number
     * @return the SchemaVersion if found
     */
    public Optional<SchemaVersion> getSchemaVersion(String schemaName, int versionNumber) {
        return schemaVersions.getOrDefault(schemaName, Collections.emptyList())
                .stream()
                .filter(v -> v.getVersionNumber() == versionNumber)
                .findFirst();
    }

    /**
     * Get the latest active schema version.
     *
     * @param schemaName the schema name
     * @return the latest active SchemaVersion
     */
    public Optional<SchemaVersion> getLatestSchemaVersion(String schemaName) {
        return schemaVersions.getOrDefault(schemaName, Collections.emptyList())
                .stream()
                .filter(SchemaVersion::isActive)
                .max(Comparator.comparingInt(SchemaVersion::getVersionNumber));
    }

    /**
     * Get all versions of a schema.
     *
     * @param schemaName the schema name
     * @return list of SchemaVersions
     */
    public List<SchemaVersion> getAllVersions(String schemaName) {
        return new ArrayList<>(schemaVersions.getOrDefault(schemaName, Collections.emptyList()));
    }

    /**
     * Validate data against a specific schema version.
     *
     * @param schemaName the schema name
     * @param versionNumber the version number to validate against
     * @param data the data to validate
     * @return true if data matches the schema
     */
    public boolean validateAgainstVersion(String schemaName, int versionNumber, Map<String, Object> data) {
        Optional<SchemaVersion> version = getSchemaVersion(schemaName, versionNumber);
        if (version.isEmpty()) {
            logger.warn("Schema version not found: {} v{}", schemaName, versionNumber);
            return false;
        }

        try {
            JsonNode schemaNode = objectMapper.readTree(version.get().getSchemaDefinition());
            JsonNode dataNode = objectMapper.valueToTree(data);

            // Basic validation - checks if all schema fields exist in data
            return validateNodeAgainstSchema(dataNode, schemaNode);
        } catch (Exception e) {
            logger.error("Failed to validate against schema: {} v{}", schemaName, versionNumber, e);
            return false;
        }
    }

    /**
     * Migrate data from one schema version to another.
     *
     * @param schemaName the schema name
     * @param fromVersion the source version
     * @param toVersion the target version
     * @param data the data to migrate
     * @return migrated data
     */
    public Map<String, Object> migrateData(String schemaName, int fromVersion, int toVersion, Map<String, Object> data) {
        if (fromVersion == toVersion) {
            return new HashMap<>(data);
        }

        if (fromVersion > toVersion) {
            logger.warn("Cannot migrate from newer version to older version: {} -> {}", fromVersion, toVersion);
            return data;
        }

        Map<String, Object> migratedData = new HashMap<>(data);

        // Apply incremental migrations
        for (int v = fromVersion + 1; v <= toVersion; v++) {
            Optional<SchemaVersion> version = getSchemaVersion(schemaName, v);
            if (version.isPresent()) {
                migratedData = applyMigration(schemaName, v, migratedData);
            }
        }

        logger.info("Migrated data from version {} to {}", fromVersion, toVersion);
        return migratedData;
    }

    /**
     * Mark a schema version as inactive (deprecated).
     *
     * @param schemaName the schema name
     * @param versionNumber the version to deprecate
     */
    public void deprecateVersion(String schemaName, int versionNumber) {
        Optional<SchemaVersion> version = getSchemaVersion(schemaName, versionNumber);
        version.ifPresent(v -> {
            SchemaVersion previous = cloneVersion(v);
            v.setActive(false);
            auditService.logSchemaChange(schemaName, String.valueOf(versionNumber), "UPDATE", previous, v, v.getCreatedBy());
            logger.info("Deprecated schema version: {} v{}", schemaName, versionNumber);
        });
    }

    /**
     * Get migration path information between versions.
     *
     * @param schemaName the schema name
     * @return map of version transitions
     */
    public Map<String, Object> getMigrationPath(String schemaName) {
        List<SchemaVersion> versions = getAllVersions(schemaName);
        Map<String, Object> path = new HashMap<>();
        path.put("schema_name", schemaName);
        path.put("current_version", getLatestSchemaVersion(schemaName).map(SchemaVersion::getVersionNumber).orElse(0));
        path.put("total_versions", versions.size());
        path.put("active_versions", versions.stream().filter(SchemaVersion::isActive).count());
        return path;
    }

    // Helper methods

    private boolean validateNodeAgainstSchema(JsonNode dataNode, JsonNode schemaNode) {
        if (schemaNode.isObject()) {
            for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
                String field = it.next();
                if (!dataNode.has(field)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Map<String, Object> applyMigration(String schemaName, int toVersion, Map<String, Object> data) {
        // Placeholder for migration logic
        // In a real system, this would apply transformations based on the version differences
        logger.debug("Applying migration for {} to version {}", schemaName, toVersion);
        return data;
    }

    private SchemaVersion cloneVersion(SchemaVersion source) {
        SchemaVersion copy = new SchemaVersion();
        copy.setId(source.getId());
        copy.setSchemaName(source.getSchemaName());
        copy.setVersionNumber(source.getVersionNumber());
        copy.setSchemaDefinition(source.getSchemaDefinition());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setCreatedBy(source.getCreatedBy());
        copy.setDescription(source.getDescription());
        copy.setActive(source.isActive());
        copy.setPreviousVersionId(source.getPreviousVersionId());
        return copy;
    }
}

package com.example.dynamicform.platform.core.versioning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SchemaVersioningServiceImpl implements SchemaVersioningService {

    private static final Logger logger = LoggerFactory.getLogger(SchemaVersioningServiceImpl.class);

    private final ObjectMapper objectMapper;
    private final Map<String, List<SchemaVersion>> schemaVersions = new HashMap<>();

    public SchemaVersioningServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
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
        logger.info("Registered schema version: {} v{}", schemaName, nextVersion);
        return version;
    }

    @Override
    public Optional<SchemaVersion> getSchemaVersion(String schemaName, int versionNumber) {
        return schemaVersions.getOrDefault(schemaName, Collections.emptyList())
                .stream()
                .filter(v -> v.getVersionNumber() == versionNumber)
                .findFirst();
    }

    @Override
    public Optional<SchemaVersion> getLatestSchemaVersion(String schemaName) {
        return schemaVersions.getOrDefault(schemaName, Collections.emptyList())
                .stream()
                .filter(SchemaVersion::isActive)
                .max(Comparator.comparingInt(SchemaVersion::getVersionNumber));
    }

    @Override
    public List<SchemaVersion> getAllVersions(String schemaName) {
        return new ArrayList<>(schemaVersions.getOrDefault(schemaName, Collections.emptyList()));
    }

    @Override
    public boolean validateAgainstVersion(String schemaName, int versionNumber, Map<String, Object> data) {
        Optional<SchemaVersion> version = getSchemaVersion(schemaName, versionNumber);
        if (version.isEmpty()) {
            logger.warn("Schema version not found: {} v{}", schemaName, versionNumber);
            return false;
        }

        try {
            JsonNode schemaNode = objectMapper.readTree(version.get().getSchemaDefinition());
            JsonNode dataNode = objectMapper.valueToTree(data);
            return validateNodeAgainstSchema(dataNode, schemaNode);
        } catch (Exception e) {
            logger.error("Failed to validate against schema: {} v{}", schemaName, versionNumber, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> migrateData(String schemaName, int fromVersion, int toVersion, Map<String, Object> data) {
        if (fromVersion == toVersion) {
            return new HashMap<>(data);
        }

        if (fromVersion > toVersion) {
            logger.warn("Cannot migrate from newer version to older version: {} -> {}", fromVersion, toVersion);
            return data;
        }

        Map<String, Object> migratedData = new HashMap<>(data);
        for (int v = fromVersion + 1; v <= toVersion; v++) {
            Optional<SchemaVersion> version = getSchemaVersion(schemaName, v);
            if (version.isPresent()) {
                migratedData = applyMigration(schemaName, v, migratedData);
            }
        }

        logger.info("Migrated data from version {} to {}", fromVersion, toVersion);
        return migratedData;
    }

    @Override
    public void deprecateVersion(String schemaName, int versionNumber) {
        Optional<SchemaVersion> version = getSchemaVersion(schemaName, versionNumber);
        version.ifPresent(v -> {
            v.setActive(false);
            logger.info("Deprecated schema version: {} v{}", schemaName, versionNumber);
        });
    }

    @Override
    public Map<String, Object> getMigrationPath(String schemaName) {
        List<SchemaVersion> versions = getAllVersions(schemaName);
        Map<String, Object> path = new HashMap<>();
        path.put("schema_name", schemaName);
        path.put("current_version", getLatestSchemaVersion(schemaName).map(SchemaVersion::getVersionNumber).orElse(0));
        path.put("total_versions", versions.size());
        path.put("active_versions", versions.stream().filter(SchemaVersion::isActive).count());
        return path;
    }

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
        logger.debug("Applying migration for {} to version {}", schemaName, toVersion);
        return data;
    }
}

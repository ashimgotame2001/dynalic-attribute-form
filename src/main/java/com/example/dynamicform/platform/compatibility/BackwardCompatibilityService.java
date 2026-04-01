package com.example.dynamicform.platform.compatibility;

import com.example.dynamicform.platform.versioning.SchemaVersioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Service for managing backward compatibility when schemas or relationships change.
 * Ensures existing data remains valid despite schema evolution.
 */
@Service
public class BackwardCompatibilityService {

    private static final Logger logger = LoggerFactory.getLogger(BackwardCompatibilityService.class);

    @Autowired
    private SchemaVersioningService schemaVersioningService;

    /**
     * Check if data is compatible with a specific schema version.
     *
     * @param schemaName the schema name
     * @param versionNumber the version to check against
     * @param data the data to validate
     * @return true if data is compatible
     */
    public boolean isDataCompatible(String schemaName, int versionNumber, Map<String, Object> data) {
        return schemaVersioningService.validateAgainstVersion(schemaName, versionNumber, data);
    }

    /**
     * Ensure data is compatible with the latest schema version.
     * Migrates data if necessary to maintain backward compatibility.
     *
     * @param schemaName the schema name
     * @param dataVersion the version of data
     * @param targetVersion the target schema version
     * @param data the data to ensure compatibility for
     * @return compatible data, migrated if necessary
     */
    public Map<String, Object> ensureCompatibility(String schemaName, int dataVersion, int targetVersion, Map<String, Object> data) {
        if (dataVersion == targetVersion) {
            logger.debug("Data is already compatible with target version");
            return data;
        }

        if (dataVersion > targetVersion) {
            logger.warn("Data version {} is newer than target version {}", dataVersion, targetVersion);
            // In this case, we trust the newer data
            return data;
        }

        // Migrate data to target version
        logger.info("Migrating data from version {} to {}", dataVersion, targetVersion);
        return schemaVersioningService.migrateData(schemaName, dataVersion, targetVersion, data);
    }

    /**
     * Get deprecated fields from an old schema that are no longer used.
     *
     * @param schemaName the schema name
     * @param oldVersion the old schema version
     * @param newVersion the new schema version
     * @return set of deprecated field names
     */
    public java.util.Set<String> getDeprecatedFields(String schemaName, int oldVersion, int newVersion) {
        java.util.Set<String> deprecated = new java.util.HashSet<>();
        
        Optional<com.example.dynamicform.platform.versioning.SchemaVersion> oldVer = 
            schemaVersioningService.getSchemaVersion(schemaName, oldVersion);
        Optional<com.example.dynamicform.platform.versioning.SchemaVersion> newVer = 
            schemaVersioningService.getSchemaVersion(schemaName, newVersion);

        if (oldVer.isPresent() && newVer.isPresent()) {
            // Compare schemas to identify removed fields
            logger.debug("Identifying deprecated fields between versions {} and {}", oldVersion, newVersion);
            // Implementation would parse and compare JSON schemas
        }

        return deprecated;
    }

    /**
     * Get new required fields added in a newer schema version.
     *
     * @param schemaName the schema name
     * @param oldVersion the old schema version
     * @param newVersion the new schema version
     * @return set of newly required field names
     */
    public java.util.Set<String> getNewRequiredFields(String schemaName, int oldVersion, int newVersion) {
        java.util.Set<String> newRequired = new java.util.HashSet<>();
        
        Optional<com.example.dynamicform.platform.versioning.SchemaVersion> oldVer = 
            schemaVersioningService.getSchemaVersion(schemaName, oldVersion);
        Optional<com.example.dynamicform.platform.versioning.SchemaVersion> newVer = 
            schemaVersioningService.getSchemaVersion(schemaName, newVersion);

        if (oldVer.isPresent() && newVer.isPresent()) {
            logger.debug("Identifying new required fields between versions {} and {}", oldVersion, newVersion);
            // Implementation would parse and compare JSON schemas
        }

        return newRequired;
    }

    /**
     * Provide defaults for missing required fields when upgrading to a new schema version.
     *
     * @param schemaName the schema name
     * @param targetVersion the target schema version
     * @param data the data that may be missing new required fields
     * @return data with default values added for missing fields
     */
    public Map<String, Object> addDefaultsForNewRequiredFields(String schemaName, int targetVersion, Map<String, Object> data) {
        Map<String, Object> enhanced = new java.util.HashMap<>(data);
        
        java.util.Set<String> newRequired = getNewRequiredFields(schemaName, 1, targetVersion);
        for (String field : newRequired) {
            if (!enhanced.containsKey(field)) {
                enhanced.put(field, getDefaultValueForField(field));
                logger.debug("Added default value for new required field: {}", field);
            }
        }

        return enhanced;
    }

    // Helper method to provide sensible defaults
    private Object getDefaultValueForField(String fieldName) {
        if (fieldName.toLowerCase().contains("date") || fieldName.toLowerCase().contains("time")) {
            return java.time.LocalDateTime.now();
        }
        if (fieldName.toLowerCase().contains("count") || fieldName.toLowerCase().contains("size")) {
            return 0;
        }
        if (fieldName.toLowerCase().contains("active") || fieldName.toLowerCase().contains("enabled")) {
            return true;
        }
        return "";
    }
}

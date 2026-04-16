package com.example.dynamicform.platform.core.compatibility;

import com.example.dynamicform.platform.core.versioning.SchemaVersion;
import com.example.dynamicform.platform.core.versioning.SchemaVersioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class BackwardCompatibilityServiceImpl implements BackwardCompatibilityService {

    private static final Logger logger = LoggerFactory.getLogger(BackwardCompatibilityServiceImpl.class);

    private final SchemaVersioningService schemaVersioningService;

    public BackwardCompatibilityServiceImpl(SchemaVersioningService schemaVersioningService) {
        this.schemaVersioningService = schemaVersioningService;
    }

    @Override
    public boolean isDataCompatible(String schemaName, int versionNumber, Map<String, Object> data) {
        return schemaVersioningService.validateAgainstVersion(schemaName, versionNumber, data);
    }

    @Override
    public Map<String, Object> ensureCompatibility(String schemaName, int dataVersion, int targetVersion, Map<String, Object> data) {
        if (dataVersion == targetVersion) {
            logger.debug("Data is already compatible with target version");
            return data;
        }

        if (dataVersion > targetVersion) {
            logger.warn("Data version {} is newer than target version {}", dataVersion, targetVersion);
            return data;
        }

        logger.info("Migrating data from version {} to {}", dataVersion, targetVersion);
        return schemaVersioningService.migrateData(schemaName, dataVersion, targetVersion, data);
    }

    @Override
    public Set<String> getDeprecatedFields(String schemaName, int oldVersion, int newVersion) {
        Set<String> deprecated = new HashSet<>();

        Optional<SchemaVersion> oldVer = schemaVersioningService.getSchemaVersion(schemaName, oldVersion);
        Optional<SchemaVersion> newVer = schemaVersioningService.getSchemaVersion(schemaName, newVersion);

        if (oldVer.isPresent() && newVer.isPresent()) {
            logger.debug("Identifying deprecated fields between versions {} and {}", oldVersion, newVersion);
        }

        return deprecated;
    }

    @Override
    public Set<String> getNewRequiredFields(String schemaName, int oldVersion, int newVersion) {
        Set<String> newRequired = new HashSet<>();

        Optional<SchemaVersion> oldVer = schemaVersioningService.getSchemaVersion(schemaName, oldVersion);
        Optional<SchemaVersion> newVer = schemaVersioningService.getSchemaVersion(schemaName, newVersion);

        if (oldVer.isPresent() && newVer.isPresent()) {
            logger.debug("Identifying new required fields between versions {} and {}", oldVersion, newVersion);
        }

        return newRequired;
    }

    @Override
    public Map<String, Object> addDefaultsForNewRequiredFields(String schemaName, int targetVersion, Map<String, Object> data) {
        Map<String, Object> enhanced = new HashMap<>(data);

        Set<String> newRequired = getNewRequiredFields(schemaName, 1, targetVersion);
        for (String field : newRequired) {
            if (!enhanced.containsKey(field)) {
                enhanced.put(field, getDefaultValueForField(field));
                logger.debug("Added default value for new required field: {}", field);
            }
        }

        return enhanced;
    }

    private Object getDefaultValueForField(String fieldName) {
        if (fieldName.toLowerCase().contains("date") || fieldName.toLowerCase().contains("time")) {
            return LocalDateTime.now();
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

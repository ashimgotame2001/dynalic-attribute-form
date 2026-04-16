package com.example.dynamicform.platform.core.versioning;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SchemaVersioningService {

    SchemaVersion registerSchema(String schemaName, String schemaDefinition, String description);

    Optional<SchemaVersion> getSchemaVersion(String schemaName, int versionNumber);

    Optional<SchemaVersion> getLatestSchemaVersion(String schemaName);

    List<SchemaVersion> getAllVersions(String schemaName);

    boolean validateAgainstVersion(String schemaName, int versionNumber, Map<String, Object> data);

    Map<String, Object> migrateData(String schemaName, int fromVersion, int toVersion, Map<String, Object> data);

    void deprecateVersion(String schemaName, int versionNumber);

    Map<String, Object> getMigrationPath(String schemaName);
}

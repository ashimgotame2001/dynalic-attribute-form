package com.example.dynamicform.platform.core.compatibility;

import java.util.Map;
import java.util.Set;

public interface BackwardCompatibilityService {

    boolean isDataCompatible(String schemaName, int versionNumber, Map<String, Object> data);

    Map<String, Object> ensureCompatibility(String schemaName, int dataVersion, int targetVersion, Map<String, Object> data);

    Set<String> getDeprecatedFields(String schemaName, int oldVersion, int newVersion);

    Set<String> getNewRequiredFields(String schemaName, int oldVersion, int newVersion);

    Map<String, Object> addDefaultsForNewRequiredFields(String schemaName, int targetVersion, Map<String, Object> data);
}

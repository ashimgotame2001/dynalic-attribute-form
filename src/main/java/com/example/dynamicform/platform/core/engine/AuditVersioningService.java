package com.example.dynamicform.platform.core.engine;

import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;

public interface AuditVersioningService {

    int incrementVersion(String modelName);

    void auditChange(String modelName, String action, String user, RawFormMetadata metadata);

    int getLatestVersion(String modelName);
}

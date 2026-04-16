package com.example.dynamicform.platform.core.engine;

import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuditVersioningServiceImpl implements AuditVersioningService {

    private static final Logger logger = LoggerFactory.getLogger(AuditVersioningServiceImpl.class);

    private final Map<String, Integer> versions = new HashMap<>();

    @Override
    public int incrementVersion(String modelName) {
        int nextVersion = versions.getOrDefault(modelName, 0) + 1;
        versions.put(modelName, nextVersion);
        logger.info("Metadata for model '{}' versioned up to {}", modelName, nextVersion);
        return nextVersion;
    }

    @Override
    public void auditChange(String modelName, String action, String user, RawFormMetadata metadata) {
        logger.info("AUDIT: Model '{}' action '{}' performed by user '{}'.", modelName, action, user);
    }

    @Override
    public int getLatestVersion(String modelName) {
        return versions.getOrDefault(modelName, 1);
    }
}

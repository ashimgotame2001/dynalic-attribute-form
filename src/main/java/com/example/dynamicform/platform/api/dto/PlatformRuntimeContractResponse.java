package com.example.dynamicform.platform.api.dto;

import java.util.List;
import java.util.Map;

/**
 * Immutable JSON contract describing the platform capabilities for a runtime-managed entity.
 */
public record PlatformRuntimeContractResponse(
        String entityType,
        String responseFormat,
        boolean dynamicRelationshipsSupported,
        boolean existingModelsRemainUnchanged,
        String persistenceAbstraction,
        String accessLayer,
        boolean backwardCompatibilitySupported,
        boolean auditAndTraceabilitySupported,
        List<String> cacheRegions,
        String validationEngine,
        Map<String, Object> schema,
        List<String> relationships
) {
}

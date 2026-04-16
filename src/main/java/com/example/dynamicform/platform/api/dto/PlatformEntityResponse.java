package com.example.dynamicform.platform.api.dto;

import java.util.List;
import java.util.Map;

/**
 * Immutable JSON response contract for runtime entity data managed by the platform.
 */
public record PlatformEntityResponse(
        String entityType,
        String entityId,
        Integer schemaVersion,
        boolean compatibleWithLatestSchema,
        Map<String, Object> data,
        Map<String, List<Map<String, Object>>> relationships,
        List<PlatformAuditEntryResponse> auditTrail,
        Map<String, Object> platform
) {
}

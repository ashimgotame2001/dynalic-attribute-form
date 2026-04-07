package com.example.dynamicform.platform.dto;

import java.time.LocalDateTime;

/**
 * Stable JSON representation of audit history entries exposed by the platform.
 */
public record PlatformAuditEntryResponse(
        Long id,
        String action,
        String entityType,
        String entityId,
        String oldValue,
        String newValue,
        LocalDateTime timestamp,
        String userId
) {
}

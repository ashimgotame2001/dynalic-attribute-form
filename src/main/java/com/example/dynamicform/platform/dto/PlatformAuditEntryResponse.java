package com.example.dynamicform.platform.dto;

import com.example.dynamicform.product.entity.RelationshipAuditEntity;

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

    public static PlatformAuditEntryResponse fromEntity(RelationshipAuditEntity entity) {
        return new PlatformAuditEntryResponse(
                entity.getId(),
                entity.getAction().name(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getOldValue(),
                entity.getNewValue(),
                entity.getTimestamp(),
                entity.getUserId()
        );
    }
}

package com.example.dynamicform.product.entity;

import com.example.dynamicform.product.enums.AuditAction;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for auditing changes to relationship definitions and instances.
 */
@Entity
@Table(name = "relationship_audit")
public class RelationshipAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(nullable = false)
    private String entityType; // "DEFINITION" or "INSTANCE"

    @Column(nullable = false)
    private String entityId; // ID of the definition or instance

    @Column(length = 2000)
    private String oldValue; // JSON representation of old state

    @Column(length = 2000)
    private String newValue; // JSON representation of new state

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String userId;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
        if (userId == null) userId = "system";
    }
}

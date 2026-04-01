package com.example.dynamicform.product.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an instance of a dynamic relationship between domain model instances.
 * Stores the actual links between data records.
 */
@Entity
@Table(name = "relationship_instance")
public class RelationshipInstanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_definition_id", nullable = false)
    private RelationshipDefinitionEntity relationshipDefinition;

    @Column(nullable = false)
    private String sourceEntityId; // ID of the source entity instance

    @Column(nullable = false)
    private String targetEntityId; // ID of the target entity instance

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;

    // Additional metadata for complex relationships
    @Column(length = 2000)
    private String metadata; // JSON string for additional relationship data

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RelationshipDefinitionEntity getRelationshipDefinition() { return relationshipDefinition; }
    public void setRelationshipDefinition(RelationshipDefinitionEntity relationshipDefinition) { this.relationshipDefinition = relationshipDefinition; }

    public String getSourceEntityId() { return sourceEntityId; }
    public void setSourceEntityId(String sourceEntityId) { this.sourceEntityId = sourceEntityId; }

    public String getTargetEntityId() { return targetEntityId; }
    public void setTargetEntityId(String targetEntityId) { this.targetEntityId = targetEntityId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (createdBy == null) createdBy = "system";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (updatedBy == null) updatedBy = "system";
    }
}

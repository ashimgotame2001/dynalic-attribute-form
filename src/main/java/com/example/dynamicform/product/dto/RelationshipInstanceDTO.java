package com.example.dynamicform.product.dto;

import java.time.LocalDateTime;

/**
 * DTO for relationship instance data transfer.
 */
public class RelationshipInstanceDTO {

    private Long id;
    private String relationshipName;
    private Long relationshipDefinitionId;
    private String sourceEntityId;
    private String targetEntityId;
    private boolean active;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private String metadata;

    // Constructors
    public RelationshipInstanceDTO() {}

    public RelationshipInstanceDTO(String relationshipName, String sourceEntityId, String targetEntityId) {
        this.relationshipName = relationshipName;
        this.sourceEntityId = sourceEntityId;
        this.targetEntityId = targetEntityId;
        this.active = true;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRelationshipName() { return relationshipName; }
    public void setRelationshipName(String relationshipName) { this.relationshipName = relationshipName; }

    public Long getRelationshipDefinitionId() { return relationshipDefinitionId; }
    public void setRelationshipDefinitionId(Long relationshipDefinitionId) { this.relationshipDefinitionId = relationshipDefinitionId; }

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
}

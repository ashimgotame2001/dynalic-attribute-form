package com.example.dynamicform.platform.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class RelationshipDefinitionDTO {

    private Long id;
    @NotBlank(message = "relationshipName is required")
    private String relationshipName;
    @NotBlank(message = "sourceEntity is required")
    private String sourceEntity;
    @NotBlank(message = "targetEntity is required")
    private String targetEntity;
    @NotNull(message = "relationshipType is required")
    private RelationshipType relationshipType;
    @NotBlank(message = "sourceKeyField is required")
    private String sourceKeyField;
    @NotBlank(message = "targetKeyField is required")
    private String targetKeyField;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRelationshipName() { return relationshipName; }
    public void setRelationshipName(String relationshipName) { this.relationshipName = relationshipName; }
    public String getSourceEntity() { return sourceEntity; }
    public void setSourceEntity(String sourceEntity) { this.sourceEntity = sourceEntity; }
    public String getTargetEntity() { return targetEntity; }
    public void setTargetEntity(String targetEntity) { this.targetEntity = targetEntity; }
    public RelationshipType getRelationshipType() { return relationshipType; }
    public void setRelationshipType(RelationshipType relationshipType) { this.relationshipType = relationshipType; }
    public String getSourceKeyField() { return sourceKeyField; }
    public void setSourceKeyField(String sourceKeyField) { this.sourceKeyField = sourceKeyField; }
    public String getTargetKeyField() { return targetKeyField; }
    public void setTargetKeyField(String targetKeyField) { this.targetKeyField = targetKeyField; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
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

    public boolean isReference() { return relationshipType != null; }
    public boolean isComposition() { return relationshipType == RelationshipType.ONE_TO_ONE || relationshipType == RelationshipType.ONE_TO_MANY; }
    public boolean isAssociation() { return relationshipType == RelationshipType.MANY_TO_ONE || relationshipType == RelationshipType.MANY_TO_MANY; }
    public boolean isCollection() { return relationshipType == RelationshipType.ONE_TO_MANY || relationshipType == RelationshipType.MANY_TO_MANY; }
}

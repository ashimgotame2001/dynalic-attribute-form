package com.example.dynamicform.product.entity;

import com.example.dynamicform.product.enums.RelationshipType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a dynamic relationship definition between domain models.
 * This allows runtime configuration of relationships without modifying existing entities.
 */
@Entity
@Table(name = "relationship_definition")
public class RelationshipDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String relationshipName;

    @Column(nullable = false)
    private String sourceEntity; // e.g., "Customer", "Beneficiary"

    @Column(nullable = false)
    private String targetEntity; // e.g., "Transaction", "Document"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationshipType relationshipType; // ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY

    @Column(nullable = false)
    private String sourceKeyField; // field name in source entity

    @Column(nullable = false)
    private String targetKeyField; // field name in target entity

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String createdBy;

    private LocalDateTime updatedAt;
    private String updatedBy;

    // Getters and setters
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

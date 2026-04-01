package com.example.dynamicform.platform.versioning;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a version of a schema or relationship definition.
 * Maintains backward compatibility by tracking changes over time.
 */
public class SchemaVersion {

    private Long id;
    private String schemaName;
    private int versionNumber;
    private String schemaDefinition; // JSON representation
    private LocalDateTime createdAt;
    private String createdBy;
    private String description;
    private boolean active;
    private String previousVersionId;

    // Constructors
    public SchemaVersion() {}

    public SchemaVersion(String schemaName, int versionNumber, String schemaDefinition) {
        this.schemaName = schemaName;
        this.versionNumber = versionNumber;
        this.schemaDefinition = schemaDefinition;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }

    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }

    public String getSchemaDefinition() { return schemaDefinition; }
    public void setSchemaDefinition(String schemaDefinition) { this.schemaDefinition = schemaDefinition; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getPreviousVersionId() { return previousVersionId; }
    public void setPreviousVersionId(String previousVersionId) { this.previousVersionId = previousVersionId; }
}

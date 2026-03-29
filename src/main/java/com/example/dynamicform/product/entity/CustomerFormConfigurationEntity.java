package com.example.dynamicform.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class CustomerFormConfigurationEntity {

     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     @Column(name = "id", nullable = false, updatable = false)
     private UUID id;

    @Column(name = "form_name", nullable = false, length = 255)
    private String formName;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;


    @Column(name = "metadata_json", nullable = false, columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "target_class_name", length = 1000)
    private String targetClassName;

    @Column(name = "module_name", length = 255)
    private String moduleName;

    @Column(name = "artifact_name", length = 255)
    private String artifactName;

    @Column(name = "static_metadata_path", length = 1000)
    private String staticMetadataPath;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "rsp_id")
    private Long rspId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Builder.Default
    @OneToMany(mappedBy = "formConfiguration", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private Set<FormConfigFieldEntity> fieldConfigs = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "optimistic_lock_version")
    @Builder.Default
    private Integer optimisticLockVersion = 0;


}

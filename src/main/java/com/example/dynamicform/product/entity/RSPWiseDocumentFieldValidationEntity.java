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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "rsp_wise_document_field_validations",
        uniqueConstraints = @UniqueConstraint(name = "uk_rsp_doc_field_validation_type", columnNames = {"field_config_id", "validation_type"})
)
public class RSPWiseDocumentFieldValidationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "field_config_id", nullable = false)
    private RSPWiseDocumentFieldConfigEntity fieldConfig;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_type", nullable = false, length = 50)
    private RSPWiseDocumentFieldValidationType validationType;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "message", length = 1000)
    private String message;

    @Builder.Default
    @OneToMany(mappedBy = "validation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("locale ASC")
    private Set<RSPWiseDocumentFieldValidationTranslationEntity> translations = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

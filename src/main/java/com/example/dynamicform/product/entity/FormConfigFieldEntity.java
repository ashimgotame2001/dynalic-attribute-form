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
@Table(
        name = "form_configuration_fields",
        uniqueConstraints = @UniqueConstraint(name = "uk_form_config_reference_model", columnNames = {"form_configuration_id", "reference_model"})
)
public class FormConfigFieldEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_configuration_id", nullable = false)
    private CustomerFormConfigurationEntity formConfiguration;

    @Column(name = "reference_model", nullable = false, length = 500)
    private String referenceModel;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "visible")
    private Boolean visible;

    @Column(name = "short_label", length = 1000)
    private String shortLabel;

    @Column(name = "long_label", length = 2000)
    private String longLabel;

    @Builder.Default
    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private Set<FormConfigFieldValidationEntity> validations = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("locale ASC")
    private Set<FormConfigFieldTranslationEntity> translations = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

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
@Table(name = "form_configuration_field_validations")
public class FormConfigFieldValidationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "field_id", nullable = false)
    private FormConfigFieldEntity field;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "validation_type", nullable = false, length = 100)
    private String validationType;

    @Column(name = "value_json", columnDefinition = "TEXT")
    private String valueJson;

    @Column(name = "pattern", length = 2000)
    private String pattern;

    @Column(name = "message", length = 2000)
    private String message;

    @Builder.Default
    @OneToMany(mappedBy = "validation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("locale ASC")
    private Set<FormConfigFieldValidationTranslationEntity> translations = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

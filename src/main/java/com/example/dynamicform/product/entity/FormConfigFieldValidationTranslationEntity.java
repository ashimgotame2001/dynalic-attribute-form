package com.example.dynamicform.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "form_configuration_field_validation_translations",
        uniqueConstraints = @UniqueConstraint(name = "uk_form_config_validation_language", columnNames = {"validation_id", "language_id"})
)
public class FormConfigFieldValidationTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "validation_id", nullable = false)
    private FormConfigFieldValidationEntity validation;

    @Column(name = "language_id", nullable = false)
    private Long languageId;

    @Column(name = "message", length = 2000)
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

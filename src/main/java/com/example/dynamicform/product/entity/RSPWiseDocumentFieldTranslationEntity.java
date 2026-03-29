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
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "rsp_wise_document_field_translations",
        uniqueConstraints = @UniqueConstraint(name = "uk_rsp_doc_field_locale", columnNames = {"field_config_id", "locale"})
)
public class RSPWiseDocumentFieldTranslationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "field_config_id", nullable = false)
    private RSPWiseDocumentFieldConfigEntity fieldConfig;

    @Column(name = "locale", nullable = false, length = 32)
    private String locale;

    @Column(name = "short_label", length = 1000)
    private String shortLabel;

    @Column(name = "long_label", length = 2000)
    private String longLabel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

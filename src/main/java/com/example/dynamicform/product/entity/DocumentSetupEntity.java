package com.example.dynamicform.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rsp_wise_documents")
public class DocumentSetupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Builder.Default
    @OneToMany(mappedBy = "setup", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private Set<DocumentFieldConfigEntity> fieldConfigs = new LinkedHashSet<>();

    @Column(name = "is_document_number_required", nullable = false)
    private boolean isDocumentNumberRequired;

    @Column(name = "is_back_required", nullable = false)
    private boolean isBackRequired;

    @Column(name = "is_issued_country_required", nullable = false)
    private boolean isIssuedCountryRequired;

    @Column(name = "is_expiry_date_required", nullable = false)
    private boolean isExpiryDateRequired;

    @Column(name = "is_primary_content_required", nullable = false)
    private boolean isPrimaryContentRequired;

    @Column(name = "is_secondary_content_required", nullable = false)
    private boolean isSecondaryContentRequired;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

}

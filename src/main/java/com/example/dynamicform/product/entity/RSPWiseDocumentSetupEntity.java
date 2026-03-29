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
@Table(name = "RSP_wise_documents")
public class RSPWiseDocumentSetupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne
    private DocumentEntity document;

    private Boolean isPrimary;

    private Long rspId;

    @Builder.Default
    @OneToMany(mappedBy = "setup", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private Set<RSPWiseDocumentFieldConfigEntity> fieldConfigs = new LinkedHashSet<>();

    // Legacy columns retained temporarily for rollout fallback. New writes should use fieldConfigs.
    private boolean isDocumentNumberRequired;
    private boolean isBackRequired;
    private boolean isIssuedCountryRequired;
    private boolean isExpiryDateRequired;
    private boolean isPrimaryContentRequired;
    private boolean isSecondaryContentRequired;

    // Legacy JSON retained temporarily for rollout fallback. New reads should prefer fieldConfigs.
    @Column(columnDefinition = "TEXT")
    private String metadataJson;

}

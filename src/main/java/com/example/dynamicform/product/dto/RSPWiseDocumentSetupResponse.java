package com.example.dynamicform.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RSPWiseDocumentSetupResponse {
    private UUID id;
    private UUID documentId;
    private String documentName;
    private Boolean isPrimary;
    private Long rspId;
    private boolean isDocumentNumberRequired;
    private boolean isBackRequired;
    private boolean isIssuedCountryRequired;
    private boolean isExpiryDateRequired;
    private boolean isPrimaryContentRequired;
    private boolean isSecondaryContentRequired;
}

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
public class RSPWiseDocumentSetupRequest {
    private UUID documentId;
    private Boolean isPrimary;
    private Long rspId;
    private boolean isBackRequired;
    private boolean isExpiryDateRequired;
}

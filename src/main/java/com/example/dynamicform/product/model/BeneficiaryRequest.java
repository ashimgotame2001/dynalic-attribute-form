package com.example.dynamicform.product.model;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class BeneficiaryRequest {
    @Valid
    private Beneficiary beneficiary;
}

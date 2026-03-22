package com.example.dynamicform.product.model;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class TransactionRequest {
    @Valid
    private Transaction transaction;
}

package com.example.dynamicform.product.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Transaction {
    private BigDecimal amount;
    private String currency;
    private String purpose;
    private String sourceOfFunds;
    private String paymentMethod;
}

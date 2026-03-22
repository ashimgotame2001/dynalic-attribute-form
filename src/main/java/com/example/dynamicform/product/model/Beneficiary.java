package com.example.dynamicform.product.model;

import lombok.Data;

@Data
public class Beneficiary {
    private String firstName;
    private String lastName;
    private String accountNumber;
    private String bankName;
    private String ifscCode;
    private String relationship;
}

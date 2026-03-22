package com.example.dynamicform.product.model;

import lombok.Data;

@Data
public class Address {
    private String postalCode;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String proofOfAddress;

}

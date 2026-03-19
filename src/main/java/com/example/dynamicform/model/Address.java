package com.example.dynamicform.model;

import lombok.Data;

@Data
public class Address {
    private PostalCodeInfo postalCodeInfo;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String proofOfAddress;

}

package com.example.dynamicform.entity;

import com.example.dynamicform.enums.DynamicFieldFor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "customer_rsp_attribute_contract")
public class CustomerRSPAttributeContractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "rsp_id", nullable = false)
    private Long rspId;


    private boolean gender;
    private boolean nationality;
    private boolean residingAlphaTwoCode;
    private boolean postalCode;
    private boolean city;
    private boolean addressLine1;
    private boolean firstName;
    private boolean middleName;
    private boolean lastName;
    private boolean dateOfBirth;
    private boolean contactNumber;
    private boolean email;
    private boolean secret;
    private boolean referralCode;
    private boolean primaryDocument;
}


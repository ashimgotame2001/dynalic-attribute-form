package com.example.dynamicform.product.entity;

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
@Entity(name = "beneficiary_rsp_attribute_contract")
public class BeneficiaryRSPAttributeContractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "rsp_id", nullable = false)
    private Long rspId;

    private boolean firstName;
    private boolean lastName;
    private boolean accountNumber;
    private boolean bankName;
    private boolean ifscCode;
    private boolean relationship;
}

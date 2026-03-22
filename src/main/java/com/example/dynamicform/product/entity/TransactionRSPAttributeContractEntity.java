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
@Entity(name = "transaction_rsp_attribute_contract")
public class TransactionRSPAttributeContractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "rsp_id", nullable = false)
    private Long rspId;

    private boolean amount;
    private boolean currency;
    private boolean purpose;
    private boolean sourceOfFunds;
    private boolean paymentMethod;
}

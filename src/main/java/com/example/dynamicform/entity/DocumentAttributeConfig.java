package com.example.dynamicform.entity;

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
@Entity(name = "customer_rsp_secondary_attribute_config")
public class DocumentAttributeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    private Long rspId;

    @Column(name = "data", nullable = false, columnDefinition = "TEXT")
    private String data;
}

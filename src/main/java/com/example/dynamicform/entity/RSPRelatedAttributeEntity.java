package com.example.dynamicform.entity;

import com.example.dynamicform.enums.DynamicFieldFor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "rsp_related_attributes")
public class RSPRelatedAttributeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    private Long rspId;

    private String attributeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type")
    private DynamicFieldFor fieldType;

}

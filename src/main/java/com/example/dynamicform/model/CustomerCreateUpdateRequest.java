package com.example.dynamicform.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for Customer
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreateUpdateRequest {

    private String custId;

    private String password;
    private String status;
    private LocalDateTime registeredDate;

//    private String type;
//
//    private String status;
//
//    private String amlStatus;
//
//    private Integer riskScore;

//    private CustomerGroup customerGroup;
//
//    private CorporateCustomer corporateInfo;
//
//    private Individual individualInfo;
//
//    private List representatives;
//
//    private List eligibilities;


}
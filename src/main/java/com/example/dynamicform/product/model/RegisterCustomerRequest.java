package com.example.dynamicform.product.model;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class RegisterCustomerRequest {

    @Valid
    private User user;
    @Valid
    private Individual individual;
    private Referral referral;

}

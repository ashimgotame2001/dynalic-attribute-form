package com.example.dynamicform.model;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class RegisterCustomerRequest {

    @Valid
    private User user;
    @Valid
    private Individual individual;
    private Referral referral;
    private SupportingDocument document;

}

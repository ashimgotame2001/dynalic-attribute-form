package com.example.dynamicform.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateUserOtpRequest {
    @NotNull
    private String otp;

    @Valid
    private UserIdentifier user;

    private String requestFor;
}

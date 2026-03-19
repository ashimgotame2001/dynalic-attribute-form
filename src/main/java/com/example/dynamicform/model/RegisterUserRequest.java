package com.example.dynamicform.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    private String countryCode;

    @NotBlank
    private String dateOfBirth;

    @NotBlank
    private String gender;

    @NotBlank
    private String contactNumber;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String origin; //infer from country name maybe

    @NotBlank
    private String city;//get from the validate address api

    @NotBlank
    private String addressLine1; //street, city, state from the validate address response

    @NotBlank
    private String nationality; //infer from country name maybe

    @NotBlank
    private String address1; //street, city, state from the validate address response

    private String fcmId;

    private String appVersion;

    private String phoneOs;

    private String osVersion;

    private String referralCode;
}

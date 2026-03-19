package com.example.dynamicform.model;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressValidateRequest {

    @Valid
    private PostalCodeInfo postalCode;

}

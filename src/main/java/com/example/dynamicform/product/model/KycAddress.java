package com.example.dynamicform.product.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycAddress {
    @NotNull(message = "addressLine1 field is mandatory")
    private String addressLine1;
    private String addressLine2;
    @NotNull(message = "city field is mandatory")
    private String city;
    @Valid
    private PostalCodeInfo postalCodeInfo;
}

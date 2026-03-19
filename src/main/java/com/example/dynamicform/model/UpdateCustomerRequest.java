package com.example.dynamicform.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {
    @Valid
    @NotNull
    private IndividualUpdate individual;

    private String kycStatus;
}

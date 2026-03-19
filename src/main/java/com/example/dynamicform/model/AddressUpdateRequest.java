package com.example.dynamicform.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressUpdateRequest {

    @NotNull
    @Valid
    IndividualAddress individual;
}

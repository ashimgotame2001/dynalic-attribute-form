package com.example.dynamicform.model.custom;

import com.example.dynamicform.dto.KycIndividual;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycRegisterRequest {

    @Valid
    @NotNull
    private KycIndividual individual;
}


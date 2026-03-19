package com.example.dynamicform.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualEmail {
    @NotNull(message = "email field is mandatory")
    private String email;

}

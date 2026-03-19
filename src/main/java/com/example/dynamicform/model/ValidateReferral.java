package com.example.dynamicform.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateReferral {
    @NotEmpty(message = "{201}")
    private String referralCode;
}

package com.example.dynamicform.model;

import jakarta.validation.Valid;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendApplicationUserOtpRequest {

    @Valid
    private UserIdentifier user;
    private String requestFor;
}

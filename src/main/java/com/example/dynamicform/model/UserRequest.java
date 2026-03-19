package com.example.dynamicform.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for User
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotNull(message = "userType field is mandatory")
    private Long userType;
    @NotNull(message = "identifier field is mandatory")
    private String identifier;
    @NotNull(message = "secret field is mandatory")
    private String secret;
    private Long rspId;

}
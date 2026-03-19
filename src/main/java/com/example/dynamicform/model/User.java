package com.example.dynamicform.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class User {
//    @NotNull(message = "identifier is mandatory.")
//    private String identifier;
    @NotNull(message = "secret is mandatory.")
    private String secret;

}
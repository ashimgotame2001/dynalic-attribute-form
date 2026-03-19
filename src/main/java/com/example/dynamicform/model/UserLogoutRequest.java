package com.example.dynamicform.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class UserLogoutRequest {

    @NotEmpty
    String token;
}

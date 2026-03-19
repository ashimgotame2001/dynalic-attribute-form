package com.example.dynamicform.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountValidateRequest {

    private String rspId;

    @JsonProperty("login-key")
    private String loginKey;
}

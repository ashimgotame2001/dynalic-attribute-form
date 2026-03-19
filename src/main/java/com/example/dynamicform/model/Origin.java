package com.example.dynamicform.model;

import com.example.dynamicform.enums.AlphaTwoCountryEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Origin {
    @NotNull
    private AlphaTwoCountryEnum alphaTwoCode;
}

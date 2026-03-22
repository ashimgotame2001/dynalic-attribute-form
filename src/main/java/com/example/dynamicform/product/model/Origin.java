package com.example.dynamicform.product.model;

import com.example.dynamicform.product.enums.AlphaTwoCountryEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Origin {
    @NotNull
    private AlphaTwoCountryEnum alphaTwoCode;
}

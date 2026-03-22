package com.example.dynamicform.product.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Gender {
    @NotNull
    private Long id;

}
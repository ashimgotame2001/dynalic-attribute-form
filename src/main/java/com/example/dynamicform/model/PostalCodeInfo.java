package com.example.dynamicform.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostalCodeInfo {
    @NotNull(message = "postalCode field is mandatory")
    private String postalCode;

}

package com.example.dynamicform.product.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicOccupation {
    @NotNull(message = "id field is mandatory")
    private Long id;
}

package com.example.dynamicform.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdentifier {
    @NotNull(message = "{201}")
    private String identifier;
}

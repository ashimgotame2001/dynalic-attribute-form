package com.example.dynamicform.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {
    @NotNull(message = "documentType field is mandatory")
    private Long id;
}

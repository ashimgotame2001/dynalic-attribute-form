package com.example.dynamicform.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueCountry {
    @NotNull(message = "issueCountry field is mandatory")
    private Long id;
}

package com.example.dynamicform.model.custom;

import com.example.dynamicform.dto.IndividualEmail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmUserCheckRequest {

    @Valid
    private IndividualEmail individual;
    @NotNull(message = "{201}")
    private Long rspId;
}

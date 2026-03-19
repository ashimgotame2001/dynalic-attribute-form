package com.example.dynamicform.model;

import com.example.dynamicform.dto.PaginationRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RewardPointHistoryPaginatedRequest extends PaginationRequest {

    @NotBlank(message = "customer id must not be null")
    @NotNull(message = "customer id must not be null")
    private String customerId;
    private String fromDate;
    private String toDate;

}

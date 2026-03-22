package com.example.dynamicform.dto;

import com.example.dynamicform.enums.ContractFor;
import lombok.Data;

@Data
public class SecondaryContractRequest {
    private ContractFor contractFor;
}

package com.example.dynamicform.model;

import com.example.dynamicform.dto.PaginationRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDataRequest extends PaginationRequest{
    private String searchText;
}
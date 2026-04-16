package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.api.dto.AttributeContractCreateRequest;
import com.example.dynamicform.platform.api.dto.AttributeContractRequest;
import com.example.dynamicform.platform.api.dto.AttributeContractResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttributeContractAccessService {

    List<AttributeContractResponse> findAll();

    List<AttributeContractResponse> findByModule(String module);

    Optional<AttributeContractResponse> findById(UUID id);

    List<AttributeContractResponse> create(AttributeContractCreateRequest request);

    Optional<AttributeContractResponse> update(UUID id, AttributeContractRequest request);

    void delete(UUID id);
}

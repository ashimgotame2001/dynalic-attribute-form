package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.api.dto.PlatformEntityResponse;
import com.example.dynamicform.platform.api.dto.PlatformRuntimeContractResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PlatformRuntimeService {

    PlatformEntityResponse save(String entityType, String entityId, Map<String, Object> payload);

    Optional<PlatformEntityResponse> getEntity(String entityType, String entityId, boolean includeRelationships);

    List<Map<String, Object>> getAll(String entityType);

    void link(String entityType, String entityId, String relationshipName, String targetId);

    void unlink(String entityType, String entityId, String relationshipName, String targetId);

    PlatformRuntimeContractResponse getContract(String entityType);
}

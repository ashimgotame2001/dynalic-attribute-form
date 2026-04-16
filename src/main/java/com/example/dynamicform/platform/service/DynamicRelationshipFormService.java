package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;

public interface DynamicRelationshipFormService {

    RawFormMetadata augmentWithRelationships(RawFormMetadata rawMetadata, String formName);
}

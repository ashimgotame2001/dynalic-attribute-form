package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;

import java.util.Map;
import java.util.Set;

public interface RawMetadataCustomizationService {

    RawFormMetadata customize(RawFormMetadata baseRaw, Set<String> enabledReferenceModels, Map<String, Object> requestAttributes);
}

package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.api.dto.DynamicMetadataBuildRequest;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;

public interface DynamicMetadataBuildService {

    RawFormMetadata build(DynamicMetadataBuildRequest request);
}

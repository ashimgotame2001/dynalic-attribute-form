package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.dto.ValidationError;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of ProductValidationService.
 * Provides default behavior that can be extended by specific products.
 */
@Service
public class BaseProductValidationService implements ProductValidationService {

    @Override
    public List<ValidationError> validate(String formName, Map<String, Object> data, RawFormMetadata metadata) {
        // Base implementation - no additional validation
        // Subclasses can override to add product-specific validation
        return Collections.emptyList();
    }

    @Override
    public RawFormMetadata customizeMetadata(RawFormMetadata baseMetadata, String productContext) {
        // Base implementation - return unchanged
        // Subclasses can override to customize metadata
        return baseMetadata;
    }

    @Override
    public List<String> getProductRelationships(String formName) {
        // Base implementation - no relationships
        // Subclasses can override to define relationships
        return Collections.emptyList();
    }

    @Override
    public String getProductId() {
        return "BASE";
    }
}

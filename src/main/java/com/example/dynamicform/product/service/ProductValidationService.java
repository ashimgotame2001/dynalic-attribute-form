package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.api.dto.ValidationError;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;

import java.util.List;
import java.util.Map;

/**
 * Interface for product-specific validation rules, relationships, and metadata.
 * Each product can implement this to define its own validation logic.
 */
public interface ProductValidationService {

    /**
     * Validates data using product-specific rules.
     *
     * @param formName the form name
     * @param data the data to validate
     * @param metadata the form metadata
     * @return list of validation errors
     */
    List<ValidationError> validate(String formName, Map<String, Object> data, RawFormMetadata metadata);

    /**
     * Customizes metadata for the product.
     *
     * @param baseMetadata the base metadata
     * @param productContext the product context
     * @return customized metadata
     */
    RawFormMetadata customizeMetadata(RawFormMetadata baseMetadata, String productContext);

    /**
     * Defines product-specific relationships.
     *
     * @param formName the form name
     * @return list of relationship definitions
     */
    List<String> getProductRelationships(String formName);

    /**
     * Gets the product identifier.
     *
     * @return product identifier
     */
    String getProductId();
}

package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;

/**
 * Interface for runtime metadata generation from existing domain models.
 * Provides capabilities to introspect domain models and generate metadata
 * representing their structure, constraints, and relationships.
 */
public interface RuntimeMetadataGenerator {

    /**
     * Generates metadata for a given domain model class.
     *
     * @param domainClass the domain model class to introspect
     * @param formName the name of the form
     * @return RawFormMetadata representing the domain model's structure
     */
    RawFormMetadata generateMetadata(Class<?> domainClass, String formName);

    /**
     * Generates metadata with custom configuration.
     *
     * @param domainClass the domain model class
     * @param formName the form name
     * @param context the context for configuration (e.g., "KYC", "Loan")
     * @return RawFormMetadata with context-specific configuration
     */
    RawFormMetadata generateMetadata(Class<?> domainClass, String formName, String context);
}

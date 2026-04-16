package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;

/**
 * Interface for context-based configuration of metadata.
 * Allows applying different configurations based on use cases like KYC, Loan, Remittance, etc.
 */
public interface ContextBasedConfigurationService {

    /**
     * Applies context-specific configuration to the metadata.
     *
     * @param metadata the base metadata
     * @param context the context (e.g., "KYC", "Loan", "Remittance")
     * @return modified metadata with context-specific rules applied
     */
    RawFormMetadata applyContextConfiguration(RawFormMetadata metadata, String context);

    /**
     * Checks if a context is supported.
     *
     * @param context the context to check
     * @return true if supported
     */
    boolean isContextSupported(String context);
}

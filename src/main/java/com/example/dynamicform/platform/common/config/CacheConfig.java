package com.example.dynamicform.platform.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/**
 * Cache configuration for form definitions.
 * In production, replace with Redis or other distributed cache.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public ConcurrentMapCacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
            "formDefinitions",
            "metadataDefinitions",
            "relationshipDefinitions",
            "relationshipInstances",
            "domainModelData",
            "schemaVersions",
            "auditTrail",
            "validationConfigs"
        );
        cacheManager.setStoreByValue(false);
        return cacheManager;
    }


}

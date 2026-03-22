package com.example.dynamicform.product.config;

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
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("formDefinitions");
        cacheManager.setStoreByValue(false); // store by reference for better performance
        return cacheManager;
    }


}
package com.example.dynamicform.platform.core.engine;

import com.example.dynamicform.platform.api.dto.FormDefinition;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple cache for storing interpreted metadata (FormDefinition).
 */
@Component
public class MetadataCache {

    private final Map<String, FormDefinition> cache = new ConcurrentHashMap<>();

    public void put(String modelName, FormDefinition definition) {
        cache.put(modelName, definition);
    }

    public FormDefinition get(String modelName) {
        return cache.get(modelName);
    }

    public void clear() {
        cache.clear();
    }
}

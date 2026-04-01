package com.example.dynamicform.platform.metadata;

import java.util.Set;

/**
 * Interface-based access layer entry point for runtime domain model access.
 * Products use this instead of dealing with persistence, relationships, validation,
 * and compatibility concerns directly.
 */
public interface DomainModelAccessLayer {

    DomainModelAccessor<?> forEntity(String entityTypeName);

    Set<String> getRegisteredEntityTypes();
}

package com.example.dynamicform.platform.core.metadata.access;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating DomainModelAccessor instances for different entity types.
 * Maintains a registry of accessors and ensures backward compatibility.
 */
public class DomainModelAccessorFactory {

    private static final Map<String, DomainModelAccessor<?>> accessorRegistry = new ConcurrentHashMap<>();

    private DomainModelAccessorFactory() {
        // Prevent instantiation
    }

    /**
     * Get or create an accessor for a specific entity type.
     *
     * @param entityTypeName the name of the entity type
     * @return the accessor for the entity type
     */
    public static DomainModelAccessor<?> getAccessor(String entityTypeName) {
        DomainModelAccessor<?> accessor = accessorRegistry.get(entityTypeName);
        if (accessor == null) {
            throw new IllegalStateException(
                    "No accessor registered for entity type " + entityTypeName +
                    ". Use the Spring-managed DomainModelAccessLayer instead."
            );
        }
        return accessor;
    }

    /**
     * Register a custom accessor for an entity type.
     *
     * @param entityTypeName the name of the entity type
     * @param accessor the accessor implementation
     */
    public static void registerAccessor(String entityTypeName, DomainModelAccessor<?> accessor) {
        accessorRegistry.put(entityTypeName, accessor);
    }

    /**
     * Clear all registered accessors.
     */
    public static void clearAccessors() {
        accessorRegistry.clear();
    }

    /**
     * Get all registered entity types.
     *
     * @return set of registered entity type names
     */
    public static java.util.Set<String> getRegisteredEntityTypes() {
        return accessorRegistry.keySet();
    }
}

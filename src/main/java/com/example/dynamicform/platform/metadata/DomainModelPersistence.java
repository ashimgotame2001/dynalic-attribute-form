package com.example.dynamicform.platform.metadata;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generic persistence abstraction interface for storing and retrieving domain model data.
 * Supports nested structures, relationships, and complex data types without modifying existing models.
 *
 * @param <T> the type of entity being persisted
 */
public interface DomainModelPersistence<T> {

    /**
     * Save or update an entity instance.
     *
     * @param entityId unique identifier for the entity
     * @param entityType the type/name of the entity
     * @param data the data to persist (can be nested)
     * @return the saved data
     */
    Map<String, Object> save(String entityId, String entityType, Map<String, Object> data);

    /**
     * Retrieve an entity instance by ID.
     *
     * @param entityId unique identifier
     * @param entityType the type/name of the entity
     * @return the entity data if found
     */
    Optional<Map<String, Object>> findById(String entityId, String entityType);

    /**
     * Retrieve all instances of a specific entity type.
     *
     * @param entityType the type/name of the entity
     * @return list of all instances
     */
    List<Map<String, Object>> findAll(String entityType);

    /**
     * Delete an entity instance.
     *
     * @param entityId unique identifier
     * @param entityType the type/name of the entity
     * @return true if deleted, false if not found
     */
    boolean delete(String entityId, String entityType);

    /**
     * Query entities by field criteria.
     *
     * @param entityType the type/name of the entity
     * @param criteria field name and value to match
     * @return list of matching entities
     */
    List<Map<String, Object>> findByCriteria(String entityType, Map<String, Object> criteria);

    /**
     * Update a specific field in an entity.
     *
     * @param entityId unique identifier
     * @param entityType the type/name of the entity
     * @param fieldName the field to update
     * @param value the new value
     */
    void updateField(String entityId, String entityType, String fieldName, Object value);

    /**
     * Check if an entity exists.
     *
     * @param entityId unique identifier
     * @param entityType the type/name of the entity
     * @return true if exists
     */
    boolean exists(String entityId, String entityType);

    /**
     * Get nested data from an entity.
     *
     * @param entityId unique identifier
     * @param entityType the type/name of the entity
     * @param nestedPath path to nested field (e.g., "address.city")
     * @return the nested data
     */
    Optional<Object> getNestedData(String entityId, String entityType, String nestedPath);

    /**
     * Save nested data in an entity.
     *
     * @param entityId unique identifier
     * @param entityType the type/name of the entity
     * @param nestedPath path to nested field
     * @param data the nested data to save
     */
    void saveNestedData(String entityId, String entityType, String nestedPath, Map<String, Object> data);
}

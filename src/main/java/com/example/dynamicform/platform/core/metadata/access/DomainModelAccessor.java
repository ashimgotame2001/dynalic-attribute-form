package com.example.dynamicform.platform.core.metadata.access;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface-based access layer for products to interact with domain model data.
 * Hides persistence complexity and manages relationships transparently.
 * This is the primary interface that products should use instead of directly accessing persistence.
 *
 * @param <T> the domain model type
 */
public interface DomainModelAccessor<T> {

    /**
     * Save a domain model instance with its relationships.
     *
     * @param entityId unique identifier
     * @param data the entity data with nested relationships
     * @return the saved entity data
     */
    Map<String, Object> save(String entityId, Map<String, Object> data);

    /**
     * Retrieve a domain model instance with all related entities.
     *
     * @param entityId unique identifier
     * @return the entity data with relationships
     */
    Optional<Map<String, Object>> findWithRelationships(String entityId);

    /**
     * Retrieve a domain model instance without relationships.
     *
     * @param entityId unique identifier
     * @return the entity data
     */
    Optional<Map<String, Object>> findById(String entityId);

    /**
     * Retrieve all instances of this domain model.
     *
     * @return list of all entities
     */
    List<Map<String, Object>> findAll();

    /**
     * Find entities matching specific criteria.
     *
     * @param criteria field-value pairs to match
     * @return list of matching entities
     */
    List<Map<String, Object>> findByCriteria(Map<String, Object> criteria);

    /**
     * Delete an entity and its relationships.
     *
     * @param entityId unique identifier
     * @return true if deleted
     */
    boolean delete(String entityId);

    /**
     * Check if an entity exists.
     *
     * @param entityId unique identifier
     * @return true if exists
     */
    boolean exists(String entityId);

    /**
     * Get entities related through a specific relationship.
     *
     * @param entityId the source entity ID
     * @param relationshipName the relationship name
     * @return list of related entities
     */
    List<Map<String, Object>> getRelated(String entityId, String relationshipName);

    /**
     * Link an entity to another through a relationship.
     *
     * @param sourceId the source entity ID
     * @param targetId the target entity ID
     * @param relationshipName the relationship name
     */
    void link(String sourceId, String targetId, String relationshipName);

    /**
     * Unlink entities from a relationship.
     *
     * @param sourceId the source entity ID
     * @param targetId the target entity ID
     * @param relationshipName the relationship name
     */
    void unlink(String sourceId, String targetId, String relationshipName);

    /**
     * Update a single field in an entity.
     *
     * @param entityId unique identifier
     * @param fieldName the field to update
     * @param value the new value
     */
    void updateField(String entityId, String fieldName, Object value);

    /**
     * Get entity type name.
     *
     * @return the entity type name
     */
    String getEntityTypeName();
}

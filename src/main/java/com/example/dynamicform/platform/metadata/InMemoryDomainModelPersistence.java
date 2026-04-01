package com.example.dynamicform.platform.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of DomainModelPersistence for generic data storage.
 * This can be replaced with a database-backed implementation as needed.
 */
@Component
public class InMemoryDomainModelPersistence implements DomainModelPersistence<Map<String, Object>> {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryDomainModelPersistence.class);

    // Structure: entityType -> entityId -> data
    private final Map<String, Map<String, Map<String, Object>>> storage = new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> save(String entityId, String entityType, Map<String, Object> data) {
        if (entityId == null || entityId.isBlank() || entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("Entity ID and type cannot be null or blank");
        }

        storage.computeIfAbsent(entityType, k -> new ConcurrentHashMap<>())
                .put(entityId, deepCopyMap(data));

        logger.debug("Saved entity: {}/{}", entityType, entityId);
        return deepCopyMap(data);
    }

    @Override
    public Optional<Map<String, Object>> findById(String entityId, String entityType) {
        return Optional.ofNullable(storage.get(entityType))
                .flatMap(entities -> Optional.ofNullable(entities.get(entityId)))
                .map(this::deepCopyMap);
    }

    @Override
    public List<Map<String, Object>> findAll(String entityType) {
        return storage.getOrDefault(entityType, Collections.emptyMap())
                .values()
                .stream()
                .map(this::deepCopyMap)
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(String entityId, String entityType) {
        Map<String, Map<String, Object>> entities = storage.get(entityType);
        if (entities != null) {
            boolean deleted = entities.remove(entityId) != null;
            if (deleted) {
                logger.debug("Deleted entity: {}/{}", entityType, entityId);
            }
            return deleted;
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> findByCriteria(String entityType, Map<String, Object> criteria) {
        return storage.getOrDefault(entityType, Collections.emptyMap())
                .values()
                .stream()
                .filter(entity -> matchesCriteria(entity, criteria))
                .map(this::deepCopyMap)
                .collect(Collectors.toList());
    }

    @Override
    public void updateField(String entityId, String entityType, String fieldName, Object value) {
        storage.computeIfPresent(entityType, (type, entities) -> {
            entities.computeIfPresent(entityId, (id, data) -> {
                data.put(fieldName, value);
                logger.debug("Updated field: {}/{}/{}", entityType, entityId, fieldName);
                return data;
            });
            return entities;
        });
    }

    @Override
    public boolean exists(String entityId, String entityType) {
        return storage.containsKey(entityType) && storage.get(entityType).containsKey(entityId);
    }

    @Override
    public Optional<Object> getNestedData(String entityId, String entityType, String nestedPath) {
        Optional<Map<String, Object>> entity = findById(entityId, entityType);
        if (entity.isEmpty()) {
            return Optional.empty();
        }

        String[] pathSegments = nestedPath.split("\\.");
        Object current = entity.get();

        for (String segment : pathSegments) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(segment);
            } else {
                return Optional.empty();
            }
        }

        return Optional.ofNullable(current);
    }

    @Override
    public void saveNestedData(String entityId, String entityType, String nestedPath, Map<String, Object> data) {
        Optional<Map<String, Object>> entity = findById(entityId, entityType);
        if (entity.isEmpty()) {
            throw new IllegalArgumentException("Entity not found: " + entityType + "/" + entityId);
        }

        String[] pathSegments = nestedPath.split("\\.");
        Map<String, Object> current = entity.get();
        Map<String, Object> root = current;

        // Navigate to the parent of the final segment
        for (int i = 0; i < pathSegments.length - 1; i++) {
            String segment = pathSegments[i];
            current = (Map<String, Object>) current.computeIfAbsent(segment, k -> new HashMap<>());
        }

        // Set the final nested value
        String finalSegment = pathSegments[pathSegments.length - 1];
        current.put(finalSegment, data);

        // Save the updated root
        save(entityId, entityType, root);
        logger.debug("Saved nested data: {}/{}/{}", entityType, entityId, nestedPath);
    }

    // Helper method to match entities against criteria
    private boolean matchesCriteria(Map<String, Object> entity, Map<String, Object> criteria) {
        return criteria.entrySet().stream()
                .allMatch(entry -> {
                    Object entityValue = resolveValueByPath(entity, entry.getKey());
                    Object criteriaValue = entry.getValue();
                    return entityValue != null && entityValue.equals(criteriaValue);
                });
    }

    private Object resolveValueByPath(Map<String, Object> entity, String path) {
        String[] segments = path.split("\\.");
        Object current = entity;
        for (String segment : segments) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                return null;
            }
            current = currentMap.get(segment);
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopyMap(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        try {
            return objectMapper.readValue(objectMapper.writeValueAsBytes(source), Map.class);
        } catch (Exception e) {
            logger.warn("Falling back to shallow copy after JSON copy failure", e);
            return new HashMap<>(source);
        }
    }

    /**
     * Export all data as JSON string (for backup/debugging).
     */
    public String exportAsJson() {
        try {
            return objectMapper.writeValueAsString(storage);
        } catch (JsonProcessingException e) {
            logger.error("Failed to export data as JSON", e);
            return "{}";
        }
    }

    /**
     * Get storage statistics.
     */
    public Map<String, Integer> getStorageStats() {
        return storage.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size()
                ));
    }
}

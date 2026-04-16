package com.example.dynamicform.platform.core.engine;

import org.springframework.stereotype.Component;

/**
 * Engine to infer relationship types from metadata flags: reference, collection, association.
 */
@Component
public class RelationshipInferenceEngine {

    /**
     * Infer RelationshipType based on flags.
     * 
     * | reference | association | collection | Relationship Type          |
     * | --------- | ----------- | ---------- | -------------------------- |
     * | false     | false       | false      | Primitive                  |
     * | true      | false       | false      | One-to-One (Composition)   |
     * | true      | false       | true       | One-to-Many (Composition)  |
     * | true      | true        | false      | Many-to-One (Association)  |
     * | true      | true        | true       | Many-to-Many (Association) |
     */
    public RelationshipType infer(boolean reference, boolean association, boolean collection) {
        if (!reference) {
            return RelationshipType.PRIMITIVE;
        }

        if (!association) {
            return collection ? RelationshipType.ONE_TO_MANY : RelationshipType.ONE_TO_ONE;
        } else {
            return collection ? RelationshipType.MANY_TO_MANY : RelationshipType.MANY_TO_ONE;
        }
    }
}

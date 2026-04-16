package com.example.dynamicform.platform.core.engine;

/**
 * Enum representing relationship types derived from metadata flags.
 */
public enum RelationshipType {
    PRIMITIVE,
    ONE_TO_ONE,      // Composition (reference=true, collection=false, association=false)
    ONE_TO_MANY,     // Composition (reference=true, collection=true, association=false)
    MANY_TO_ONE,     // Association (reference=true, collection=false, association=true)
    MANY_TO_MANY;     // Association (reference=true, collection=true, association=true)

    @Override
    public String toString() {
        return name().toLowerCase().replace("_", " ");
    }
}

package com.example.dynamicform.platform.api.dto;

/**
 * Enumeration of supported validation types.
 */
public enum ValidationType {
    REQUIRED("required"),
    REGEX("regex"),
    MIN("min"),
    MAX("max"),
    DATE_FORMAT("dateFormat"),
    ALLOW_FUTURE("allowFuture"),
    ALLOW_PAST("allowPast");

    private final String value;

    ValidationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Convert a string to ValidationType, case-insensitive.
     */
    public static ValidationType fromString(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Validation type cannot be null");
        }
        String normalized = type.trim().toLowerCase();
        for (ValidationType vt : values()) {
            if (vt.value.equalsIgnoreCase(normalized) || vt.name().equalsIgnoreCase(normalized)) {
                return vt;
            }
        }
        throw new IllegalArgumentException("Unknown validation type: " + type);
    }
}

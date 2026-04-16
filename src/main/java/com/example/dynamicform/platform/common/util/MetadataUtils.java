package com.example.dynamicform.platform.common.util;

import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Utility methods for metadata processing.
 */
public final class MetadataUtils {

    private MetadataUtils() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static String toLabel(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }

        // Insert space before uppercase letters and capitalize first letter
        String result = input.replaceAll("([a-z])([A-Z])", "$1 $2");
        // Capitalize first character
        result = result.substring(0, 1).toUpperCase(Locale.ROOT) + result.substring(1).toLowerCase(Locale.ROOT);
        return result;
    }

    /**
     * Determines if a modelName represents a simple (primitive or wrapper) type.
     */
    public static boolean isSimpleType(String modelName) {
        if (modelName == null) return false;
        String normalized = modelName.toLowerCase(Locale.ROOT);
        return normalized.equals("string") ||
               normalized.equals("integer") ||
               normalized.equals("int") ||
               normalized.equals("long") ||
               normalized.equals("double") ||
               normalized.equals("float") ||
               normalized.equals("boolean") ||
               normalized.equals("bool") ||
               normalized.equals("localdate") ||
               normalized.equals("localdatetime") ||
               normalized.equals("date") ||
               normalized.equals("timestamp") ||
               normalized.equals("bigdecimal");
    }

    /**
     * Extracts component type from data type for UI rendering.
     */
    public static String inferComponentType(String dataType) {
        if (dataType == null) return "text";
        String type = dataType.toLowerCase(Locale.ROOT);
        return switch (type) {
            case "string", "text" -> "text";
            case "integer", "int", "long", "double", "float", "bigdecimal" -> "number";
            case "boolean", "bool" -> "checkbox";
            case "localdate", "date" -> "date";
            case "localdatetime", "datetime", "timestamp" -> "datetime";
            default -> "text";
        };
    }
}

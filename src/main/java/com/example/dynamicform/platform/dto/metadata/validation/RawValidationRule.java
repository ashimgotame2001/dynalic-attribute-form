package com.example.dynamicform.platform.dto.metadata.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Raw DTO representing a validation rule from JSON metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawValidationRule {

    private transient Map<String, Object> deserializedMap;

    @JsonCreator
    public RawValidationRule(Map<String, Object> map) {
        this.deserializedMap = map;
        // Parse the map
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            this.type = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) entry.getValue();
            this.parameters = new java.util.HashMap<>();
            for (Map.Entry<String, Object> paramEntry : params.entrySet()) {
                String key = paramEntry.getKey();
                if ("message".equals(key)) {
                    this.message = (String) paramEntry.getValue();
                } else {
                    this.parameters.put(key, paramEntry.getValue());
                }
            }
        }
    }

    /**
     * Custom serialization to return the old format map.
     */
    @com.fasterxml.jackson.annotation.JsonValue
    public Map<String, Object> toMap() {
        Map<String, Object> params = new java.util.HashMap<>(parameters != null ? parameters : Map.of());
        if (message != null) {
            params.put("message", message);
        }
        return Map.of(type, params);
    }
    /**
     * Type of validation, e.g., required, regex, min, max, dateFormat, allowFuture, allowPast.
     */
    private String type;

    /**
     * Parameters for the validation. For regex it contains pattern; for min/max it contains value.
     * The key is parameter name, value is the parameter value.
     */
    private Map<String, Object> parameters;

    /**
     * Custom error message (optional).
     */
    private String message;
}

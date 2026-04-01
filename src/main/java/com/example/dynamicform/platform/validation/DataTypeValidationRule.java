package com.example.dynamicform.platform.validation;

import java.util.*;

/**
 * Built-in validation rule for data types.
 */
public class DataTypeValidationRule implements ValidationRule {

    @Override
    public String getName() {
        return "DataType";
    }

    @Override
    public List<String> getApplicableEntityTypes() {
        return Collections.singletonList("*");
    }

    @Override
    public List<ValidationError> validate(String entityType, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            if (value == null) continue; // Null checks handled by RequiredFieldValidationRule

            // Type-specific validations
            switch (fieldName.toLowerCase()) {
                case "id":
                    if (!(value instanceof String)) {
                        errors.add(new ValidationError(fieldName, "INVALID_TYPE",
                            "ID must be a string", "datatype"));
                    }
                    break;

                case "amount":
                case "balance":
                    if (value instanceof Number) {
                        double numValue = ((Number) value).doubleValue();
                        if (numValue < 0) {
                            errors.add(new ValidationError(fieldName, "INVALID_RANGE",
                                "Amount cannot be negative", "datatype"));
                        }
                    } else {
                        errors.add(new ValidationError(fieldName, "INVALID_TYPE",
                            "Amount must be a number", "datatype"));
                    }
                    break;

                case "email":
                    if (value instanceof String) {
                        String email = (String) value;
                        if (!email.contains("@") || !email.contains(".")) {
                            errors.add(new ValidationError(fieldName, "INVALID_FORMAT",
                                "Invalid email format", "datatype"));
                        }
                    } else {
                        errors.add(new ValidationError(fieldName, "INVALID_TYPE",
                            "Email must be a string", "datatype"));
                    }
                    break;

                case "phone":
                case "phonenumber":
                    if (value instanceof String) {
                        String phone = (String) value;
                        if (!phone.matches("[+]?[0-9\\s\\-\\(\\)]+")) {
                            errors.add(new ValidationError(fieldName, "INVALID_FORMAT",
                                "Invalid phone number format", "datatype"));
                        }
                    } else {
                        errors.add(new ValidationError(fieldName, "INVALID_TYPE",
                            "Phone number must be a string", "datatype"));
                    }
                    break;

                default:
                    // Generic type validation - ensure basic types
                    if (!(value instanceof String || value instanceof Number ||
                          value instanceof Boolean || value instanceof Map ||
                          value instanceof List)) {
                        errors.add(new ValidationError(fieldName, "INVALID_TYPE",
                            "Unsupported data type: " + value.getClass().getSimpleName(), "datatype"));
                    }
                    break;
            }
        }

        return errors;
    }

    @Override
    public int getPriority() {
        return 20; // Run after required field checks
    }
}

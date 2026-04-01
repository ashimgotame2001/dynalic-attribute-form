package com.example.dynamicform.platform.validation;

import java.util.*;

/**
 * Built-in validation rule for field length constraints.
 */
public class LengthValidationRule implements ValidationRule {

    @Override
    public String getName() {
        return "Length";
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

            if (!(value instanceof String)) continue;

            String stringValue = (String) value;

            // Field-specific length validations
            switch (fieldName.toLowerCase()) {
                case "name":
                case "fullname":
                    if (stringValue.length() < 2) {
                        errors.add(new ValidationError(fieldName, "TOO_SHORT",
                            "Name must be at least 2 characters", "length"));
                    }
                    if (stringValue.length() > 100) {
                        errors.add(new ValidationError(fieldName, "TOO_LONG",
                            "Name cannot exceed 100 characters", "length"));
                    }
                    break;

                case "description":
                    if (stringValue.length() > 1000) {
                        errors.add(new ValidationError(fieldName, "TOO_LONG",
                            "Description cannot exceed 1000 characters", "length"));
                    }
                    break;

                case "email":
                    if (stringValue.length() > 254) {
                        errors.add(new ValidationError(fieldName, "TOO_LONG",
                            "Email cannot exceed 254 characters", "length"));
                    }
                    break;

                case "phone":
                case "phonenumber":
                    if (stringValue.length() < 7) {
                        errors.add(new ValidationError(fieldName, "TOO_SHORT",
                            "Phone number must be at least 7 characters", "length"));
                    }
                    if (stringValue.length() > 20) {
                        errors.add(new ValidationError(fieldName, "TOO_LONG",
                            "Phone number cannot exceed 20 characters", "length"));
                    }
                    break;

                case "id":
                    if (stringValue.length() < 1) {
                        errors.add(new ValidationError(fieldName, "TOO_SHORT",
                            "ID cannot be empty", "length"));
                    }
                    if (stringValue.length() > 50) {
                        errors.add(new ValidationError(fieldName, "TOO_LONG",
                            "ID cannot exceed 50 characters", "length"));
                    }
                    break;

                default:
                    // Generic length check for string fields
                    if (stringValue.length() > 10000) {
                        errors.add(new ValidationError(fieldName, "TOO_LONG",
                            "Field value is too long (max 10000 characters)", "length"));
                    }
                    break;
            }
        }

        return errors;
    }

    @Override
    public int getPriority() {
        return 30; // Run after data type checks
    }
}

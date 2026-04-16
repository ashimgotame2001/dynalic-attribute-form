package com.example.dynamicform.platform.core.validation.rule;

import com.example.dynamicform.platform.core.validation.ValidationError;

import java.util.*;

/**
 * Built-in validation rule for unique constraints.
 */
public class UniqueConstraintValidationRule implements ValidationRule {

    // In a real implementation, this would check against a database
    // For now, this is a placeholder that demonstrates the concept

    @Override
    public String getName() {
        return "UniqueConstraint";
    }

    @Override
    public List<String> getApplicableEntityTypes() {
        return Collections.singletonList("*");
    }

    @Override
    public List<ValidationError> validate(String entityType, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        // This would typically query the database to check uniqueness
        // For demonstration, we'll check some basic constraints

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            // Example unique constraints
            if ("email".equals(fieldName) && value instanceof String) {
                // In real implementation: check if email already exists in database
                // For now, just demonstrate the pattern
                String email = (String) value;
                if (email.equals("duplicate@example.com")) {
                    errors.add(new ValidationError(fieldName, "NOT_UNIQUE",
                        "Email address already exists", "uniqueness"));
                }
            }

            if ("username".equals(fieldName) && value instanceof String) {
                String username = (String) value;
                if (username.equals("admin")) {
                    errors.add(new ValidationError(fieldName, "NOT_UNIQUE",
                        "Username already taken", "uniqueness"));
                }
            }

            if ("accountNumber".equals(fieldName) && value instanceof String) {
                String accountNumber = (String) value;
                if (accountNumber.equals("1234567890")) {
                    errors.add(new ValidationError(fieldName, "NOT_UNIQUE",
                        "Account number already exists", "uniqueness"));
                }
            }
        }

        return errors;
    }

    @Override
    public int getPriority() {
        return 60; // Run after basic validations
    }
}

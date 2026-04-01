package com.example.dynamicform.platform.validation;

import java.util.*;

/**
 * Built-in validation rule for required fields.
 */
public class RequiredFieldValidationRule implements ValidationRule {

    @Override
    public String getName() {
        return "RequiredField";
    }

    @Override
    public List<String> getApplicableEntityTypes() {
        return Collections.singletonList("*");
    }

    @Override
    public List<ValidationError> validate(String entityType, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        // This would typically read from metadata/configuration
        // For now, basic implementation
        // In a real system, this would check against schema definitions

        // Example: Check for common required fields based on entity type
        switch (entityType.toLowerCase()) {
            case "customer":
                if (!data.containsKey("id") || data.get("id") == null) {
                    errors.add(new ValidationError("id", "REQUIRED",
                        "Customer ID is required", "constraint"));
                }
                if (!data.containsKey("name") || data.get("name") == null ||
                    data.get("name").toString().trim().isEmpty()) {
                    errors.add(new ValidationError("name", "REQUIRED",
                        "Customer name is required", "constraint"));
                }
                break;

            case "transaction":
                if (!data.containsKey("amount") || data.get("amount") == null) {
                    errors.add(new ValidationError("amount", "REQUIRED",
                        "Transaction amount is required", "constraint"));
                }
                break;

            default:
                // Generic check - assume 'id' is always required
                if (!data.containsKey("id") || data.get("id") == null) {
                    errors.add(new ValidationError("id", "REQUIRED",
                        "Entity ID is required", "constraint"));
                }
                break;
        }

        return errors;
    }

    @Override
    public int getPriority() {
        return 10; // High priority - run first
    }
}

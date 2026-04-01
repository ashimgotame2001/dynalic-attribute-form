package com.example.dynamicform.platform.validation;

import java.util.*;

/**
 * Built-in validation rule for referential integrity.
 */
public class ReferentialIntegrityValidationRule implements ValidationRule {

    @Override
    public String getName() {
        return "ReferentialIntegrity";
    }

    @Override
    public List<String> getApplicableEntityTypes() {
        return Collections.singletonList("*");
    }

    @Override
    public List<ValidationError> validate(String entityType, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        // Check referential integrity for foreign key fields
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            // Example referential integrity checks
            if (fieldName.endsWith("Id") && value instanceof String) {
                String referenceId = (String) value;

                // Check if referenced entity exists
                // In real implementation, this would query the database
                if (referenceId.equals("NONEXISTENT-ID")) {
                    errors.add(new ValidationError(fieldName, "REFERENTIAL_INTEGRITY",
                        "Referenced entity does not exist: " + referenceId, "referential"));
                }
            }

            // Specific field checks
            switch (fieldName.toLowerCase()) {
                case "customerid":
                    if (value instanceof String) {
                        String customerId = (String) value;
                        // In real implementation: check if customer exists
                        if (customerId.startsWith("INVALID-")) {
                            errors.add(new ValidationError(fieldName, "REFERENTIAL_INTEGRITY",
                                "Customer does not exist: " + customerId, "referential"));
                        }
                    }
                    break;

                case "accountid":
                case "bankaccountid":
                    if (value instanceof String) {
                        String accountId = (String) value;
                        // In real implementation: check if account exists and is active
                        if (accountId.startsWith("CLOSED-")) {
                            errors.add(new ValidationError(fieldName, "REFERENTIAL_INTEGRITY",
                                "Account is closed: " + accountId, "referential"));
                        }
                    }
                    break;

                case "transactionid":
                    if (value instanceof String) {
                        String transactionId = (String) value;
                        // In real implementation: check if transaction exists
                        if (transactionId.startsWith("VOIDED-")) {
                            errors.add(new ValidationError(fieldName, "REFERENTIAL_INTEGRITY",
                                "Transaction is voided: " + transactionId, "referential"));
                        }
                    }
                    break;
            }
        }

        return errors;
    }

    @Override
    public int getPriority() {
        return 70; // Run after other validations
    }
}

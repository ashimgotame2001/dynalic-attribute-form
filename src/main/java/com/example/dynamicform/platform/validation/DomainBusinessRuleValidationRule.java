package com.example.dynamicform.platform.validation;

import java.util.*;

/**
 * Domain-specific validation rule for business logic constraints.
 * This rule enforces business rules that are specific to the domain.
 */
public class DomainBusinessRuleValidationRule implements ValidationRule {

    @Override
    public String getName() {
        return "DomainBusinessRule";
    }

    @Override
    public List<String> getApplicableEntityTypes() {
        return Arrays.asList("Customer", "Transaction", "Beneficiary", "Document");
    }

    @Override
    public List<ValidationError> validate(String entityType, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        switch (entityType.toLowerCase()) {
            case "customer":
                errors.addAll(validateCustomer(data));
                break;
            case "transaction":
                errors.addAll(validateTransaction(data));
                break;
            case "beneficiary":
                errors.addAll(validateBeneficiary(data));
                break;
            case "document":
                errors.addAll(validateDocument(data));
                break;
        }

        return errors;
    }

    @Override
    public int getPriority() {
        return 5; // Run before technical validations
    }

    private List<ValidationError> validateCustomer(Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        // Business rule: Customer must have valid identification
        boolean hasId = data.containsKey("id") && data.get("id") != null;
        boolean hasEmail = data.containsKey("email") && data.get("email") != null;
        boolean hasPhone = data.containsKey("phone") && data.get("phone") != null;

        if (!hasId && !hasEmail && !hasPhone) {
            errors.add(new ValidationError("identification",
                "INSUFFICIENT_IDENTIFICATION",
                "Customer must have at least one form of identification (ID, email, or phone)",
                "business"));
        }

        // Business rule: Customer cannot be minor for certain services
        if (data.containsKey("dateOfBirth")) {
            // This would calculate age and validate
            // For demo, just check if DOB is reasonable
            Object dob = data.get("dateOfBirth");
            if (dob instanceof String) {
                // Basic validation - in real system would parse and validate age
                if (((String) dob).length() < 8) {
                    errors.add(new ValidationError("dateOfBirth", "INVALID_DOB",
                        "Date of birth format is invalid", "business"));
                }
            }
        }

        return errors;
    }

    private List<ValidationError> validateTransaction(Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        // Business rule: Transaction amount must be positive
        if (data.containsKey("amount")) {
            Object amount = data.get("amount");
            if (amount instanceof Number) {
                double amt = ((Number) amount).doubleValue();
                if (amt <= 0) {
                    errors.add(new ValidationError("amount", "INVALID_AMOUNT",
                        "Transaction amount must be positive", "business"));
                }
                if (amt > 1000000) { // Example business limit
                    errors.add(new ValidationError("amount", "AMOUNT_TOO_HIGH",
                        "Transaction amount exceeds maximum limit", "business"));
                }
            }
        }

        // Business rule: Transaction must have a valid purpose
        if (data.containsKey("purpose")) {
            Object purpose = data.get("purpose");
            if (purpose instanceof String) {
                String purp = (String) purpose;
                if (purp.trim().isEmpty()) {
                    errors.add(new ValidationError("purpose", "EMPTY_PURPOSE",
                        "Transaction purpose cannot be empty", "business"));
                }
                // Check against allowed purposes
                List<String> allowedPurposes = Arrays.asList(
                    "salary", "transfer", "payment", "refund", "investment", "other"
                );
                if (!allowedPurposes.contains(purp.toLowerCase())) {
                    errors.add(new ValidationError("purpose", "INVALID_PURPOSE",
                        "Transaction purpose is not recognized", "business"));
                }
            }
        }

        return errors;
    }

    private List<ValidationError> validateBeneficiary(Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        // Business rule: Beneficiary must be associated with a customer
        if (!data.containsKey("customerId") || data.get("customerId") == null) {
            errors.add(new ValidationError("customerId", "MISSING_CUSTOMER",
                "Beneficiary must be associated with a customer", "business"));
        }

        // Business rule: Beneficiary account must be active
        if (data.containsKey("accountStatus")) {
            Object status = data.get("accountStatus");
            if ("CLOSED".equals(status) || "SUSPENDED".equals(status)) {
                errors.add(new ValidationError("accountStatus", "ACCOUNT_INACTIVE",
                    "Beneficiary account is not active", "business"));
            }
        }

        return errors;
    }

    private List<ValidationError> validateDocument(Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        // Business rule: Document must have valid expiry date
        if (data.containsKey("expiryDate")) {
            // In real implementation, would check if expiry date is in future
            // For demo, just ensure it exists
            Object expiry = data.get("expiryDate");
            if (expiry == null) {
                errors.add(new ValidationError("expiryDate", "MISSING_EXPIRY",
                    "Document must have an expiry date", "business"));
            }
        }

        // Business rule: Document type must be recognized
        if (data.containsKey("documentType")) {
            Object type = data.get("documentType");
            if (type instanceof String) {
                List<String> validTypes = Arrays.asList(
                    "passport", "license", "id_card", "certificate", "permit"
                );
                if (!validTypes.contains(((String) type).toLowerCase())) {
                    errors.add(new ValidationError("documentType", "INVALID_TYPE",
                        "Document type is not recognized", "business"));
                }
            }
        }

        return errors;
    }
}

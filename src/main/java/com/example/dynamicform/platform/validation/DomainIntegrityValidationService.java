package com.example.dynamicform.platform.validation;

import com.example.dynamicform.product.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.product.dto.RelationshipInstanceDTO;
import com.example.dynamicform.platform.service.RuntimeRelationshipPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Domain Integrity Validation Service.
 * Ensures tight domain checking and maintains referential integrity.
 */
@Service
public class DomainIntegrityValidationService {

    private static final Logger logger = LoggerFactory.getLogger(DomainIntegrityValidationService.class);

    @Autowired
    private RuntimeRelationshipPlatform relationshipPlatform;

    @Autowired
    private ConstraintValidationEngine validationEngine;

    /**
     * Validate domain integrity before any operation.
     *
     * @param operation the operation being performed
     * @param entityType the entity type
     * @param entityId the entity ID
     * @param data the data involved
     * @return validation results
     */
    public DomainValidationResult validateDomainIntegrity(String operation, String entityType,
                                                         String entityId, Map<String, Object> data) {
        DomainValidationResult result = new DomainValidationResult();

        try {
            // 1. Validate basic constraints
            List<ValidationError> constraintErrors = validationEngine.validate(entityType, entityId, data);
            result.addErrors(constraintErrors);

            // 2. Validate domain-specific rules
            List<ValidationError> domainErrors = validateDomainRules(operation, entityType, entityId, data);
            result.addErrors(domainErrors);

            // 3. Validate relationships if applicable
            if (data != null && data.containsKey("_relationships")) {
                @SuppressWarnings("unchecked")
                Map<String, List<RelationshipInstanceDTO>> relationships =
                    (Map<String, List<RelationshipInstanceDTO>>) data.get("_relationships");
                List<ValidationError> relationshipErrors = validateRelationshipIntegrity(entityType, entityId, relationships);
                result.addErrors(relationshipErrors);
            }

            // 4. Check for circular dependencies
            if ("CREATE_RELATIONSHIP".equals(operation) || "UPDATE_RELATIONSHIP".equals(operation)) {
                boolean hasCircularDependency = checkCircularDependencies(entityType, entityId, data);
                if (hasCircularDependency) {
                    result.addError(new ValidationError("relationships", "CIRCULAR_DEPENDENCY",
                        "Circular dependency detected in relationships", "domain"));
                }
            }

            result.setValid(result.getErrors().isEmpty());
            logger.info("Domain integrity validation for {} {}/{}: {} errors",
                       operation, entityType, entityId, result.getErrors().size());

        } catch (Exception e) {
            logger.error("Error during domain integrity validation: {}", e.getMessage(), e);
            result.addError(new ValidationError("system", "VALIDATION_FAILURE",
                "Domain integrity validation failed: " + e.getMessage(), "system"));
            result.setValid(false);
        }

        return result;
    }

    /**
     * Validate domain-specific business rules.
     */
    private List<ValidationError> validateDomainRules(String operation, String entityType,
                                                     String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        switch (entityType.toLowerCase()) {
            case "customer":
                errors.addAll(validateCustomerDomainRules(operation, entityId, data));
                break;
            case "transaction":
                errors.addAll(validateTransactionDomainRules(operation, entityId, data));
                break;
            case "beneficiary":
                errors.addAll(validateBeneficiaryDomainRules(operation, entityId, data));
                break;
            case "document":
                errors.addAll(validateDocumentDomainRules(operation, entityId, data));
                break;
        }

        return errors;
    }

    /**
     * Validate customer domain rules.
     */
    private List<ValidationError> validateCustomerDomainRules(String operation, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        // Rule: Customer must have at least one active account
        if ("UPDATE".equals(operation) || "DELETE".equals(operation)) {
            List<RelationshipInstanceDTO> accounts = relationshipPlatform.getRelatedEntities("Customer", entityId, "customer_has_accounts");
            long activeAccounts = accounts.stream()
                    .filter(RelationshipInstanceDTO::isActive)
                    .count();

            if (activeAccounts == 0) {
                errors.add(new ValidationError("accounts", "NO_ACTIVE_ACCOUNTS",
                    "Customer must have at least one active account", "domain"));
            }
        }

        // Rule: Customer cannot be deleted if they have pending transactions
        if ("DELETE".equals(operation)) {
            List<RelationshipInstanceDTO> transactions = relationshipPlatform.getRelatedEntities("Customer", entityId, "customer_has_transactions");
            boolean hasPendingTransactions = transactions.stream()
                    .anyMatch(t -> {
                        // In real implementation, would check transaction status
                        // For demo, assume all are pending
                        return true;
                    });

            if (hasPendingTransactions) {
                errors.add(new ValidationError("transactions", "PENDING_TRANSACTIONS",
                    "Customer cannot be deleted while having pending transactions", "domain"));
            }
        }

        return errors;
    }

    /**
     * Validate transaction domain rules.
     */
    private List<ValidationError> validateTransactionDomainRules(String operation, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        // Rule: Transaction must be associated with a valid customer
        if (data != null && data.containsKey("customerId")) {
            String customerId = (String) data.get("customerId");
            // In real implementation, would check if customer exists and is active
            if (customerId == null || customerId.trim().isEmpty()) {
                errors.add(new ValidationError("customerId", "INVALID_CUSTOMER",
                    "Transaction must be associated with a valid customer", "domain"));
            }
        }

        // Rule: Transaction amount cannot exceed customer's daily limit
        if (data != null && data.containsKey("amount") && data.containsKey("customerId")) {
            // In real implementation, would check customer's daily limit
            // For demo, just validate the amount is reasonable
            Object amount = data.get("amount");
            if (amount instanceof Number) {
                double amt = ((Number) amount).doubleValue();
                if (amt > 50000) { // Example daily limit
                    errors.add(new ValidationError("amount", "DAILY_LIMIT_EXCEEDED",
                        "Transaction amount exceeds daily limit", "domain"));
                }
            }
        }

        return errors;
    }

    /**
     * Validate beneficiary domain rules.
     */
    private List<ValidationError> validateBeneficiaryDomainRules(String operation, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        // Rule: Beneficiary must belong to exactly one customer
        if (data != null && data.containsKey("customerId")) {
            String customerId = (String) data.get("customerId");
            if (customerId == null || customerId.trim().isEmpty()) {
                errors.add(new ValidationError("customerId", "MISSING_CUSTOMER_ASSOCIATION",
                    "Beneficiary must be associated with a customer", "domain"));
            }
        }

        // Rule: Beneficiary cannot be shared between multiple customers
        if ("CREATE".equals(operation) && entityId != null) {
            // In real implementation, would check if beneficiary is already associated
            // with another customer
            List<RelationshipInstanceDTO> customerRelations = relationshipPlatform.getRelatedEntities("Beneficiary", entityId, "customer_has_beneficiaries");
            if (customerRelations.size() > 1) {
                errors.add(new ValidationError("customerId", "MULTIPLE_CUSTOMER_ASSOCIATION",
                    "Beneficiary cannot be associated with multiple customers", "domain"));
            }
        }

        return errors;
    }

    /**
     * Validate document domain rules.
     */
    private List<ValidationError> validateDocumentDomainRules(String operation, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        // Rule: Document must be associated with a customer
        if (data != null && !data.containsKey("customerId")) {
            errors.add(new ValidationError("customerId", "MISSING_CUSTOMER",
                "Document must be associated with a customer", "domain"));
        }

        // Rule: Document cannot be deleted if it's referenced by active transactions
        if ("DELETE".equals(operation)) {
            // In real implementation, would check if document is referenced
            // by any active transactions or processes
            List<RelationshipInstanceDTO> transactionRelations = relationshipPlatform.getRelatedEntities("Document", entityId, "transaction_has_documents");
            if (!transactionRelations.isEmpty()) {
                errors.add(new ValidationError("references", "ACTIVE_REFERENCES",
                    "Document cannot be deleted while referenced by active transactions", "domain"));
            }
        }

        return errors;
    }

    /**
     * Validate relationship integrity.
     */
    private List<ValidationError> validateRelationshipIntegrity(String entityType, String entityId,
                                                               Map<String, List<RelationshipInstanceDTO>> relationships) {
        List<ValidationError> errors = new ArrayList<>();

        if (relationships != null) {
            for (Map.Entry<String, List<RelationshipInstanceDTO>> entry : relationships.entrySet()) {
                String relationshipName = entry.getKey();
                List<RelationshipInstanceDTO> instances = entry.getValue();

                // Validate relationship definition exists
                Optional<RelationshipDefinitionDTO> definition = relationshipPlatform.getRelationshipDefinition(relationshipName);
                if (definition.isEmpty()) {
                    errors.add(new ValidationError("relationships", "INVALID_RELATIONSHIP",
                        "Relationship definition does not exist: " + relationshipName, "domain"));
                    continue;
                }

                // Validate relationship constraints
                RelationshipDefinitionDTO def = definition.get();
                for (RelationshipInstanceDTO instance : instances) {
                    List<ValidationError> instanceErrors = validationEngine.validateRelationship(
                        relationshipName, instance.getSourceEntityId(), instance.getTargetEntityId());
                    errors.addAll(instanceErrors);
                }
            }
        }

        return errors;
    }

    /**
     * Check for circular dependencies in relationships.
     */
    private boolean checkCircularDependencies(String entityType, String entityId, Map<String, Object> data) {
        // Simplified circular dependency check
        // In real implementation, would build a graph and detect cycles
        return false;
    }

    /**
     * Domain validation result.
     */
    public static class DomainValidationResult {
        private boolean valid = true;
        private List<ValidationError> errors = new ArrayList<>();
        private Map<String, Object> metadata = new HashMap<>();

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public List<ValidationError> getErrors() { return errors; }
        public void addError(ValidationError error) {
            this.errors.add(error);
            this.valid = false;
        }
        public void addErrors(List<ValidationError> errors) {
            this.errors.addAll(errors);
            if (!errors.isEmpty()) {
                this.valid = false;
            }
        }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}

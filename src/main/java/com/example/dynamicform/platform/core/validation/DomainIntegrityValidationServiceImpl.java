package com.example.dynamicform.platform.core.validation;

import com.example.dynamicform.platform.service.RuntimeRelationshipPlatform;
import com.example.dynamicform.platform.api.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.platform.api.dto.RelationshipInstanceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DomainIntegrityValidationServiceImpl implements DomainIntegrityValidationService {

    private static final Logger logger = LoggerFactory.getLogger(DomainIntegrityValidationServiceImpl.class);

    private final RuntimeRelationshipPlatform relationshipPlatform;
    private final ConstraintValidationEngine validationEngine;

    public DomainIntegrityValidationServiceImpl(RuntimeRelationshipPlatform relationshipPlatform,
                                                ConstraintValidationEngine validationEngine) {
        this.relationshipPlatform = relationshipPlatform;
        this.validationEngine = validationEngine;
    }

    @Override
    public DomainValidationResult validateDomainIntegrity(String operation, String entityType,
                                                          String entityId, Map<String, Object> data) {
        DomainValidationResult result = new DomainValidationResult();

        try {
            List<ValidationError> constraintErrors = validationEngine.validate(entityType, entityId, data);
            result.addErrors(constraintErrors);

            List<ValidationError> domainErrors = validateDomainRules(operation, entityType, entityId, data);
            result.addErrors(domainErrors);

            if (data != null && data.containsKey("_relationships")) {
                @SuppressWarnings("unchecked")
                Map<String, List<RelationshipInstanceDTO>> relationships =
                        (Map<String, List<RelationshipInstanceDTO>>) data.get("_relationships");
                List<ValidationError> relationshipErrors = validateRelationshipIntegrity(entityType, entityId, relationships);
                result.addErrors(relationshipErrors);
            }

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
            default:
                break;
        }

        return errors;
    }

    private List<ValidationError> validateCustomerDomainRules(String operation, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

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

        if ("DELETE".equals(operation)) {
            List<RelationshipInstanceDTO> transactions = relationshipPlatform.getRelatedEntities("Customer", entityId, "customer_has_transactions");
            boolean hasPendingTransactions = transactions.stream().anyMatch(t -> true);

            if (hasPendingTransactions) {
                errors.add(new ValidationError("transactions", "PENDING_TRANSACTIONS",
                        "Customer cannot be deleted while having pending transactions", "domain"));
            }
        }

        return errors;
    }

    private List<ValidationError> validateTransactionDomainRules(String operation, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        if (data != null && data.containsKey("customerId")) {
            String customerId = (String) data.get("customerId");
            if (customerId == null || customerId.trim().isEmpty()) {
                errors.add(new ValidationError("customerId", "INVALID_CUSTOMER",
                        "Transaction must be associated with a valid customer", "domain"));
            }
        }

        if (data != null && data.containsKey("amount") && data.containsKey("customerId")) {
            Object amount = data.get("amount");
            if (amount instanceof Number) {
                double amt = ((Number) amount).doubleValue();
                if (amt > 50000) {
                    errors.add(new ValidationError("amount", "DAILY_LIMIT_EXCEEDED",
                            "Transaction amount exceeds daily limit", "domain"));
                }
            }
        }

        return errors;
    }

    private List<ValidationError> validateBeneficiaryDomainRules(String operation, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        if (data != null && data.containsKey("customerId")) {
            String customerId = (String) data.get("customerId");
            if (customerId == null || customerId.trim().isEmpty()) {
                errors.add(new ValidationError("customerId", "MISSING_CUSTOMER_ASSOCIATION",
                        "Beneficiary must be associated with a customer", "domain"));
            }
        }

        if ("CREATE".equals(operation) && entityId != null) {
            List<RelationshipInstanceDTO> customerRelations = relationshipPlatform.getRelatedEntities("Beneficiary", entityId, "customer_has_beneficiaries");
            if (customerRelations.size() > 1) {
                errors.add(new ValidationError("customerId", "MULTIPLE_CUSTOMER_ASSOCIATION",
                        "Beneficiary cannot be associated with multiple customers", "domain"));
            }
        }

        return errors;
    }

    private List<ValidationError> validateDocumentDomainRules(String operation, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        if (data != null && !data.containsKey("customerId")) {
            errors.add(new ValidationError("customerId", "MISSING_CUSTOMER",
                    "Document must be associated with a customer", "domain"));
        }

        if ("DELETE".equals(operation)) {
            List<RelationshipInstanceDTO> transactionRelations = relationshipPlatform.getRelatedEntities("Document", entityId, "transaction_has_documents");
            if (!transactionRelations.isEmpty()) {
                errors.add(new ValidationError("references", "ACTIVE_REFERENCES",
                        "Document cannot be deleted while referenced by active transactions", "domain"));
            }
        }

        return errors;
    }

    private List<ValidationError> validateRelationshipIntegrity(String entityType, String entityId,
                                                                Map<String, List<RelationshipInstanceDTO>> relationships) {
        List<ValidationError> errors = new ArrayList<>();

        if (relationships != null) {
            for (Map.Entry<String, List<RelationshipInstanceDTO>> entry : relationships.entrySet()) {
                String relationshipName = entry.getKey();
                List<RelationshipInstanceDTO> instances = entry.getValue();

                Optional<RelationshipDefinitionDTO> definition = relationshipPlatform.getRelationshipDefinition(relationshipName);
                if (definition.isEmpty()) {
                    errors.add(new ValidationError("relationships", "INVALID_RELATIONSHIP",
                            "Relationship definition does not exist: " + relationshipName, "domain"));
                    continue;
                }

                for (RelationshipInstanceDTO instance : instances) {
                    List<ValidationError> instanceErrors = validationEngine.validateRelationship(
                            relationshipName, instance.getSourceEntityId(), instance.getTargetEntityId());
                    errors.addAll(instanceErrors);
                }
            }
        }

        return errors;
    }

    private boolean checkCircularDependencies(String entityType, String entityId, Map<String, Object> data) {
        return false;
    }
}

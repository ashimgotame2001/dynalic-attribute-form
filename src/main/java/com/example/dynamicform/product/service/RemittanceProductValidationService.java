package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.api.dto.ValidationError;
import com.example.dynamicform.platform.api.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Remittance-specific implementation of ProductValidationService.
 * Defines validation rules, relationships, and metadata specific to remittance products.
 */
@Service
public class RemittanceProductValidationService extends BaseProductValidationService {

    @Override
    public List<ValidationError> validate(String formName, Map<String, Object> data, RawFormMetadata metadata) {
        // Remittance-specific validation logic
        // For example, validate beneficiary details, amounts, etc.
        // This is a placeholder - actual implementation would check specific business rules
        return super.validate(formName, data, metadata);
    }

    @Override
    public RawFormMetadata customizeMetadata(RawFormMetadata baseMetadata, String productContext) {
        RawFormMetadata customized = baseMetadata.toBuilder().build();

        // Customize for remittance context
        if (customized.getDomainModel() != null && customized.getDomainModel().getAttributes() != null) {
            for (RawDomainAttribute attribute : customized.getDomainModel().getAttributes()) {
                // Make remittance-specific fields required
                if (isRemittanceCriticalField(attribute.getReferenceModel())) {
                    addRequiredValidation(attribute);
                }
            }
        }

        return customized;
    }

    @Override
    public List<String> getProductRelationships(String formName) {
        // Define relationships specific to remittance
        return Arrays.asList(
            "customer-beneficiary",
            "transaction-beneficiary",
            "sender-receiver"
        );
    }

    @Override
    public String getProductId() {
        return "REMITTANCE";
    }

    private void addRequiredValidation(RawDomainAttribute attribute) {
        if (attribute.getValidations() == null) {
            attribute.setValidations(new ArrayList<>());
        }
        // Check if required validation already exists
        boolean hasRequired = attribute.getValidations().stream()
                .anyMatch(validation -> "required".equals(validation.get("type")));
        if (!hasRequired) {
            Map<String, Object> requiredValidation = new HashMap<>();
            requiredValidation.put("type", "required");
            requiredValidation.put("message", attribute.getReferenceModel() + " is required for remittance");
            attribute.getValidations().add(requiredValidation);
        }
    }

    private boolean isRemittanceCriticalField(String referenceModel) {
        return referenceModel != null && (
            referenceModel.contains("beneficiary") ||
            referenceModel.contains("amount") ||
            referenceModel.contains("currency") ||
            referenceModel.contains("transfer")
        );
    }
}

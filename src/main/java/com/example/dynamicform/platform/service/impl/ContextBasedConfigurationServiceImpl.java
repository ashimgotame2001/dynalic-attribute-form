package com.example.dynamicform.platform.service.impl;

import com.example.dynamicform.platform.api.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.service.ContextBasedConfigurationService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementation of ContextBasedConfigurationService.
 * Applies different configurations based on contexts like KYC, Loan, Remittance.
 */
@Service
public class ContextBasedConfigurationServiceImpl implements ContextBasedConfigurationService {

    private static final List<String> SUPPORTED_CONTEXTS = Arrays.asList("KYC", "Loan", "Remittance");

    @Override
    public RawFormMetadata applyContextConfiguration(RawFormMetadata metadata, String context) {
        if (!isContextSupported(context)) {
            return metadata;
        }

        RawFormMetadata modifiedMetadata = metadata.toBuilder().build();

        switch (context.toUpperCase()) {
            case "KYC":
                applyKYCConfiguration(modifiedMetadata);
                break;
            case "LOAN":
                applyLoanConfiguration(modifiedMetadata);
                break;
            case "REMITTANCE":
                applyRemittanceConfiguration(modifiedMetadata);
                break;
        }

        return modifiedMetadata;
    }

    @Override
    public boolean isContextSupported(String context) {
        return context != null && SUPPORTED_CONTEXTS.contains(context.toUpperCase());
    }

    private void applyKYCConfiguration(RawFormMetadata metadata) {
        // KYC-specific: Make certain fields required, add identity validation
        if (metadata.getDomainModel() != null && metadata.getDomainModel().getAttributes() != null) {
            for (RawDomainAttribute attribute : metadata.getDomainModel().getAttributes()) {
                if (isIdentityField(attribute.getReferenceModel())) {
                    addRequiredValidation(attribute);
                }
            }
        }
    }

    private void applyLoanConfiguration(RawFormMetadata metadata) {
        // Loan-specific: Focus on financial information
        if (metadata.getDomainModel() != null && metadata.getDomainModel().getAttributes() != null) {
            for (RawDomainAttribute attribute : metadata.getDomainModel().getAttributes()) {
                if (isFinancialField(attribute.getReferenceModel())) {
                    addRequiredValidation(attribute);
                }
            }
        }
    }

    private void applyRemittanceConfiguration(RawFormMetadata metadata) {
        // Remittance-specific: Focus on beneficiary and transaction details
        if (metadata.getDomainModel() != null && metadata.getDomainModel().getAttributes() != null) {
            for (RawDomainAttribute attribute : metadata.getDomainModel().getAttributes()) {
                if (isBeneficiaryField(attribute.getReferenceModel())) {
                    addRequiredValidation(attribute);
                }
            }
        }
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
            requiredValidation.put("message", attribute.getReferenceModel() + " is required");
            attribute.getValidations().add(requiredValidation);
        }
    }

    private boolean isIdentityField(String referenceModel) {
        return referenceModel != null && (
            referenceModel.contains("id") ||
            referenceModel.contains("identity") ||
            referenceModel.contains("document")
        );
    }

    private boolean isFinancialField(String referenceModel) {
        return referenceModel != null && (
            referenceModel.contains("amount") ||
            referenceModel.contains("income") ||
            referenceModel.contains("credit")
        );
    }

    private boolean isBeneficiaryField(String referenceModel) {
        return referenceModel != null && (
            referenceModel.contains("beneficiary") ||
            referenceModel.contains("recipient") ||
            referenceModel.contains("transfer")
        );
    }
}

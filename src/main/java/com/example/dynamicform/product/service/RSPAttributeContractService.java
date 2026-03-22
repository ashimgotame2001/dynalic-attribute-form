package com.example.dynamicform.product.service;

import com.example.dynamicform.product.dto.RSPAttributeContractCreateRequest;
import com.example.dynamicform.product.dto.RSPAttributeContractRequest;
import com.example.dynamicform.product.dto.RSPAttributeContractResponse;
import com.example.dynamicform.product.entity.BeneficiaryRSPAttributeContractEntity;
import com.example.dynamicform.product.entity.CustomerRSPAttributeContractEntity;
import com.example.dynamicform.product.entity.TransactionRSPAttributeContractEntity;
import com.example.dynamicform.product.enums.DynamicFieldFor;
import com.example.dynamicform.product.repository.BeneficiaryRSPAttributeContractRepository;
import com.example.dynamicform.product.repository.RSPAttributeContractRepository;
import com.example.dynamicform.product.repository.TransactionRSPAttributeContractRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class RSPAttributeContractService {

    private static final String CUSTOMER_METADATA_PATH = "customer_static_metadata.json";
    private static final String DOCUMENT_METADATA_PATH = "document_metadata.json";
    private static final String BENEFICIARY_METADATA_PATH = "beneficiary_static_metadata.json";
    private static final String TRANSACTION_METADATA_PATH = "transaction_static_metadata.json";

    private final RSPAttributeContractRepository repository;
    private final BeneficiaryRSPAttributeContractRepository beneficiaryRepository;
    private final TransactionRSPAttributeContractRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, List<Map<String, Object>>> metadataCache = new java.util.concurrent.ConcurrentHashMap<>();

    private List<Map<String, Object>> getMetadata(String filePath) {
        return metadataCache.computeIfAbsent(filePath, path -> {
            try (InputStream is = new ClassPathResource(path).getInputStream()) {
                return objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            } catch (IOException e) {
                return Collections.emptyList();
            }
        });
    }

    public RSPAttributeContractService(RSPAttributeContractRepository repository,
                                      BeneficiaryRSPAttributeContractRepository beneficiaryRepository,
                                      TransactionRSPAttributeContractRepository transactionRepository,
                                      ObjectMapper objectMapper) {
        this.repository = repository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }

    public List<RSPAttributeContractResponse> findAll() {
        return repository.findAll().stream()
                .flatMap(e -> toResponses(e, "customer").stream())
                .toList();
    }

    public List<RSPAttributeContractResponse> findByRspId(Long rspId, String module) {
        if ("beneficiary".equalsIgnoreCase(module)) {
            return beneficiaryRepository.findByRspId(rspId).stream()
                    .flatMap(e -> toBeneficiaryResponses(e).stream())
                    .toList();
        } else if ("transaction".equalsIgnoreCase(module)) {
            return transactionRepository.findByRspId(rspId).stream()
                    .flatMap(e -> toTransactionResponses(e).stream())
                    .toList();
        }
        return repository.findByRspId(rspId).stream()
                .flatMap(e -> toResponses(e, module).stream())
                .toList();
    }



    public Optional<RSPAttributeContractResponse> findById(UUID id) {
        return repository.findById(id)
                .flatMap(e -> toResponses(e, "customer").stream().findFirst());
    }

    /**
     * Create multiple contracts for a single RSP using a list of referenceModels.
     */
    @Transactional
    public List<RSPAttributeContractResponse> create(RSPAttributeContractCreateRequest request) {
        Long rspId = request.getRspId();
        List<String> refs = request.getReferenceModels();
        DynamicFieldFor type = request.getFieldType();

        if (type == DynamicFieldFor.BENEFICIARY) {
            BeneficiaryRSPAttributeContractEntity entity = beneficiaryRepository.findByRspId(rspId).orElse(
                    BeneficiaryRSPAttributeContractEntity.builder().rspId(rspId).build());
            entity.setFirstName(false);
            entity.setLastName(false);
            entity.setAccountNumber(false);
            entity.setBankName(false);
            entity.setIfscCode(false);
            entity.setRelationship(false);
            if (refs != null) {
                for (String ref : refs) {
                    switch (ref) {
                        case "beneficiary/firstName" -> entity.setFirstName(true);
                        case "beneficiary/lastName" -> entity.setLastName(true);
                        case "beneficiary/accountNumber" -> entity.setAccountNumber(true);
                        case "beneficiary/bankName" -> entity.setBankName(true);
                        case "beneficiary/ifscCode" -> entity.setIfscCode(true);
                        case "beneficiary/relationship" -> entity.setRelationship(true);
                    }
                }
            }
            return toBeneficiaryResponses(beneficiaryRepository.save(entity));
        } else if (type == DynamicFieldFor.TRANSACTION) {
            TransactionRSPAttributeContractEntity entity = transactionRepository.findByRspId(rspId).orElse(
                    TransactionRSPAttributeContractEntity.builder().rspId(rspId).build());
            entity.setAmount(false);
            entity.setCurrency(false);
            entity.setPurpose(false);
            entity.setSourceOfFunds(false);
            entity.setPaymentMethod(false);
            if (refs != null) {
                for (String ref : refs) {
                    switch (ref) {
                        case "transaction/amount" -> entity.setAmount(true);
                        case "transaction/currency" -> entity.setCurrency(true);
                        case "transaction/purpose" -> entity.setPurpose(true);
                        case "transaction/sourceOfFunds" -> entity.setSourceOfFunds(true);
                        case "transaction/paymentMethod" -> entity.setPaymentMethod(true);
                    }
                }
            }
            return toTransactionResponses(transactionRepository.save(entity));
        }

        CustomerRSPAttributeContractEntity entity = repository.findByRspId(rspId).stream().findFirst().orElse(
                CustomerRSPAttributeContractEntity.builder().rspId(rspId).build());

        // Reset all flags to false, then enable only requested ones
        entity.setGender(false);
        entity.setNationality(false);
        entity.setResidingAlphaTwoCode(false);
        entity.setPostalCode(false);
        entity.setCity(false);
        entity.setAddressLine1(false);
        entity.setFirstName(false);
        entity.setMiddleName(false);
        entity.setLastName(false);
        entity.setDateOfBirth(false);
        entity.setContactNumber(false);
        entity.setEmail(false);
        entity.setSecret(false);
        entity.setReferralCode(false);
        entity.setDocument(false);
        entity.setDocumentType(false);
        entity.setIssueCountry(false);
        entity.setDocumentNumber(false);
        entity.setIssueDate(false);
        entity.setExpiryDate(false);

        entity.setMinPrimaryDocuments(request.getMinPrimaryDocuments());
        entity.setMinSecondaryDocuments(request.getMinSecondaryDocuments());

        if (refs != null) {
            for (String ref : refs) {
                switch (ref) {
                    case "individual/gender/id" -> entity.setGender(true);
                    case "individual/origin/alphaTwoCode" -> entity.setNationality(true);
                    case "individual/residingCountry/alphaTwoCode" -> entity.setResidingAlphaTwoCode(true);
                    case "individual/address/postalCodeInfo/postalCode" -> entity.setPostalCode(true);
                    case "individual/address/city" -> entity.setCity(true);
                    case "individual/address/addressLine1" -> entity.setAddressLine1(true);
                    case "individual/firstName" -> entity.setFirstName(true);
                    case "individual/middleName" -> entity.setMiddleName(true);
                    case "individual/lastName" -> entity.setLastName(true);
                    case "individual/dateOfBirth" -> entity.setDateOfBirth(true);
                    case "individual/contactNumber" -> entity.setContactNumber(true);
                    case "individual/email" -> entity.setEmail(true);
                    case "user/secret" -> entity.setSecret(true);
                    case "referral/referralCode" -> entity.setReferralCode(true);
                    case "document" -> entity.setDocument(true);
                    case "document/documentType/id" -> entity.setDocumentType(true);
                    case "document/issueCountry/alphaTwoCode" -> entity.setIssueCountry(true);
                    case "document/documentNumber" -> entity.setDocumentNumber(true);
                    case "document/issueDate" -> entity.setIssueDate(true);
                    case "document/expiryDate" -> entity.setExpiryDate(true);
                    default -> {
                    }
                }
            }
        }

        CustomerRSPAttributeContractEntity saved = repository.save(entity);
        return toResponses(saved, "customer");
    }

    @Transactional
    public Optional<RSPAttributeContractResponse> update(UUID id, RSPAttributeContractRequest request) {
        // The current update logic is simple: it only updates the rspId.
        // For module-specific entities, we'd need to know which repository to use.
        // Given the ID is a UUID, we can try each repository or add a module parameter.
        // For now, let's stick with the customer repository to match existing behavior,
        // but it's a known limitation for other modules.
        return repository.findById(id)
                .map(existing -> {
                    existing.setRspId(request.getRspId());
                    return repository.save(existing);
                })
                .flatMap(e -> toResponses(e, "customer").stream().findFirst());
    }

    @Transactional
    public void delete(UUID id) {
        // Try deleting from all repositories if the ID matches.
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else if (beneficiaryRepository.existsById(id)) {
            beneficiaryRepository.deleteById(id);
        } else if (transactionRepository.existsById(id)) {
            transactionRepository.deleteById(id);
        }
    }

    private List<RSPAttributeContractResponse> toResponses(CustomerRSPAttributeContractEntity entity, String module) {
        List<RSPAttributeContractResponse> result = new ArrayList<>();

        addIfTrue(result, entity.isGender(), entity.getId(), entity.getRspId(), "individual/gender/id", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isNationality(), entity.getId(), entity.getRspId(), "individual/origin/alphaTwoCode", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isResidingAlphaTwoCode(), entity.getId(), entity.getRspId(), "individual/residingCountry/alphaTwoCode", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isPostalCode(), entity.getId(), entity.getRspId(), "individual/address/postalCodeInfo/postalCode", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isCity(), entity.getId(), entity.getRspId(), "individual/address/city", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isAddressLine1(), entity.getId(), entity.getRspId(), "individual/address/addressLine1", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isFirstName(), entity.getId(), entity.getRspId(), "individual/firstName", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isMiddleName(), entity.getId(), entity.getRspId(), "individual/middleName", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isLastName(), entity.getId(), entity.getRspId(), "individual/lastName", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isDateOfBirth(), entity.getId(), entity.getRspId(), "individual/dateOfBirth", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isContactNumber(), entity.getId(), entity.getRspId(), "individual/contactNumber", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isEmail(), entity.getId(), entity.getRspId(), "individual/email", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isSecret(), entity.getId(), entity.getRspId(), "user/secret", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isReferralCode(), entity.getId(), entity.getRspId(), "referral/referralCode", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isDocument(), entity.getId(), entity.getRspId(), "document", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isDocumentType(), entity.getId(), entity.getRspId(), "document/documentType/id", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isIssueCountry(), entity.getId(), entity.getRspId(), "document/issueCountry/alphaTwoCode", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isDocumentNumber(), entity.getId(), entity.getRspId(), "document/documentNumber", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isIssueDate(), entity.getId(), entity.getRspId(), "document/issueDate", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());
        addIfTrue(result, entity.isExpiryDate(), entity.getId(), entity.getRspId(), "document/expiryDate", module, DynamicFieldFor.CUSTOMER, entity.getMinPrimaryDocuments(), entity.getMinSecondaryDocuments());

        return result;
    }

    private List<RSPAttributeContractResponse> toBeneficiaryResponses(BeneficiaryRSPAttributeContractEntity entity) {
        List<RSPAttributeContractResponse> result = new ArrayList<>();
        String module = "beneficiary";
        DynamicFieldFor type = DynamicFieldFor.BENEFICIARY;

        addIfTrue(result, entity.isFirstName(), entity.getId(), entity.getRspId(), "beneficiary/firstName", module, type, null, null);
        addIfTrue(result, entity.isLastName(), entity.getId(), entity.getRspId(), "beneficiary/lastName", module, type, null, null);
        addIfTrue(result, entity.isAccountNumber(), entity.getId(), entity.getRspId(), "beneficiary/accountNumber", module, type, null, null);
        addIfTrue(result, entity.isBankName(), entity.getId(), entity.getRspId(), "beneficiary/bankName", module, type, null, null);
        addIfTrue(result, entity.isIfscCode(), entity.getId(), entity.getRspId(), "beneficiary/ifscCode", module, type, null, null);
        addIfTrue(result, entity.isRelationship(), entity.getId(), entity.getRspId(), "beneficiary/relationship", module, type, null, null);

        return result;
    }

    private List<RSPAttributeContractResponse> toTransactionResponses(TransactionRSPAttributeContractEntity entity) {
        List<RSPAttributeContractResponse> result = new ArrayList<>();
        String module = "transaction";
        DynamicFieldFor type = DynamicFieldFor.TRANSACTION;

        addIfTrue(result, entity.isAmount(), entity.getId(), entity.getRspId(), "transaction/amount", module, type, null, null);
        addIfTrue(result, entity.isCurrency(), entity.getId(), entity.getRspId(), "transaction/currency", module, type, null, null);
        addIfTrue(result, entity.isPurpose(), entity.getId(), entity.getRspId(), "transaction/purpose", module, type, null, null);
        addIfTrue(result, entity.isSourceOfFunds(), entity.getId(), entity.getRspId(), "transaction/sourceOfFunds", module, type, null, null);
        addIfTrue(result, entity.isPaymentMethod(), entity.getId(), entity.getRspId(), "transaction/paymentMethod", module, type, null, null);

        return result;
    }

    private void addIfTrue(List<RSPAttributeContractResponse> target,
                           boolean flag,
                           UUID id,
                           Long rspId,
                           String referenceModel,
                           String module,
                           DynamicFieldFor fieldType,
                           Integer minPrimary,
                           Integer minSecondary) {
        if (!flag) {
            return;
        }
        target.add(
                RSPAttributeContractResponse.builder()
                        .id(id)
                        .rspId(rspId)
                        .referenceModel(referenceModel)
                        .label(resolveLabel(referenceModel, module))
                        .fieldType(fieldType)
                        .minPrimaryDocuments(minPrimary)
                        .minSecondaryDocuments(minSecondary)
                        .build()
        );
    }

    /**
     * Reads the metadata files and resolves the label
     * for a given referenceModel.
     */
    public String resolveLabel(String referenceModel, String module) {
        if (referenceModel == null || referenceModel.isBlank()) {
            return "";
        }

        String label = null;
        if ("customer".equalsIgnoreCase(module)) {
            label = findLabelInFile(referenceModel, CUSTOMER_METADATA_PATH);
        } else if ("beneficiary".equalsIgnoreCase(module)) {
            label = findLabelInFile(referenceModel, BENEFICIARY_METADATA_PATH);
        } else if ("transaction".equalsIgnoreCase(module)) {
            label = findLabelInFile(referenceModel, TRANSACTION_METADATA_PATH);
        }

        if (label == null) {
            label = findLabelInFile(referenceModel, DOCUMENT_METADATA_PATH);
        }

        if (label != null) {
            return label;
        }

        String[] parts = referenceModel.split("/");
        String last = parts[parts.length - 1];
        // Simple humanization: "alphaTwoCode" -> "Alpha Two Code"
        return last.replaceAll("([a-z])([a-z])", "$1 $2")
                .replaceFirst("^[a-z]", last.substring(0, 1).toUpperCase());
    }

    private String findLabelInFile(String referenceModel, String filePath) {
        for (Map<String, Object> item : getMetadata(filePath)) {
            if (referenceModel.equals(item.get("referenceModel"))) {
                Object label = item.get("label");
                if (label != null) {
                    return label.toString();
                }
            }
        }
        return null;
    }
}


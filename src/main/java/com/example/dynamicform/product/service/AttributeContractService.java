package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.core.metadata.access.StaticMetadataResolver;
import com.example.dynamicform.platform.entity.BeneficiaryAttributeContractEntity;
import com.example.dynamicform.platform.entity.CustomerAttributeContractEntity;
import com.example.dynamicform.platform.entity.TransactionAttributeContractEntity;
import com.example.dynamicform.platform.repository.BeneficiaryAttributeContractRepository;
import com.example.dynamicform.platform.repository.CustomerAttributeContractRepository;
import com.example.dynamicform.platform.repository.TransactionAttributeContractRepository;
import com.example.dynamicform.product.dto.AttributeContractCreateRequest;
import com.example.dynamicform.product.dto.AttributeContractRequest;
import com.example.dynamicform.product.dto.AttributeContractResponse;
import com.example.dynamicform.product.enums.DynamicFieldFor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttributeContractService {

    private static final String CUSTOMER_METADATA_PATH = "customer_static_metadata.json";
    private static final String DOCUMENT_METADATA_PATH = "document_static_metadata.json";
    private static final String BENEFICIARY_METADATA_PATH = "beneficiary_static_metadata.json";
    private static final String TRANSACTION_METADATA_PATH = "transaction_static_metadata.json";

    private final CustomerAttributeContractRepository repository;
    private final BeneficiaryAttributeContractRepository beneficiaryRepository;
    private final TransactionAttributeContractRepository transactionRepository;
    private final StaticMetadataResolver staticMetadataResolver;

    public AttributeContractService(CustomerAttributeContractRepository repository,
                                    BeneficiaryAttributeContractRepository beneficiaryRepository,
                                    TransactionAttributeContractRepository transactionRepository,
                                    StaticMetadataResolver staticMetadataResolver) {
        this.repository = repository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.transactionRepository = transactionRepository;
        this.staticMetadataResolver = staticMetadataResolver;
    }

    public List<AttributeContractResponse> findAll() {
        return repository.findAll().stream()
                .flatMap(e -> toResponses(e, "customer").stream())
                .toList();
    }

    public List<AttributeContractResponse> findByRspId(Long rspId, String module) {
        if ("beneficiary".equalsIgnoreCase(module)) {
            return beneficiaryRepository.findFirstByOrderByIdAsc().stream()
                    .flatMap(e -> toBeneficiaryResponses(e).stream())
                    .toList();
        } else if ("transaction".equalsIgnoreCase(module)) {
            return transactionRepository.findFirstByOrderByIdAsc().stream()
                    .flatMap(e -> toTransactionResponses(e).stream())
                    .toList();
        }
        return repository.findFirstByOrderByIdAsc().stream()
                .flatMap(e -> toResponses(e, module).stream())
                .toList();
    }


    public Optional<AttributeContractResponse> findById(UUID id) {
        return repository.findById(id)
                .flatMap(e -> toResponses(e, "customer").stream().findFirst());
    }

    /**
     * Create multiple contracts for a single RSP using a list of referenceModels.
     */
    @Transactional
    public List<AttributeContractResponse> create(AttributeContractCreateRequest request) {

        List<String> refs = request.getReferenceModels();
        DynamicFieldFor type = request.getFieldType();

        if (type == DynamicFieldFor.BENEFICIARY) {
            BeneficiaryAttributeContractEntity entity = beneficiaryRepository.findAll().stream().findFirst().orElse(new BeneficiaryAttributeContractEntity());
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
            TransactionAttributeContractEntity entity = transactionRepository.findAll().stream().findFirst().orElse(new TransactionAttributeContractEntity());
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

        CustomerAttributeContractEntity entity = repository.findAll().stream().findFirst().orElse(new CustomerAttributeContractEntity());

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
                    default -> {
                    }
                }
            }
        }

        CustomerAttributeContractEntity saved = repository.save(entity);
        return toResponses(saved, "customer");
    }

    @Transactional
    public Optional<AttributeContractResponse> update(UUID id, AttributeContractRequest request) {
        return repository.findById(id)
                .map(repository::save)
                .flatMap(e -> toResponses(e, "customer").stream().findFirst());
    }

    @Transactional
    public void delete(UUID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else if (beneficiaryRepository.existsById(id)) {
            beneficiaryRepository.deleteById(id);
        } else if (transactionRepository.existsById(id)) {
            transactionRepository.deleteById(id);
        }
    }

    private List<AttributeContractResponse> toResponses(CustomerAttributeContractEntity entity, String module) {
        List<AttributeContractResponse> result = new ArrayList<>();

        addIfTrue(result, entity.isGender(), entity.getId(), "individual/gender/id", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isNationality(), entity.getId(), "individual/origin/alphaTwoCode", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isResidingAlphaTwoCode(), entity.getId(), "individual/residingCountry/alphaTwoCode", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isPostalCode(), entity.getId(), "individual/address/postalCodeInfo/postalCode", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isCity(), entity.getId(), "individual/address/city", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isAddressLine1(), entity.getId(), "individual/address/addressLine1", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isFirstName(), entity.getId(), "individual/firstName", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isMiddleName(), entity.getId(), "individual/middleName", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isLastName(), entity.getId(), "individual/lastName", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isDateOfBirth(), entity.getId(), "individual/dateOfBirth", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isContactNumber(), entity.getId(), "individual/contactNumber", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isEmail(), entity.getId(), "individual/email", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isSecret(), entity.getId(), "user/secret", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isReferralCode(), entity.getId(), "referral/referralCode", module, DynamicFieldFor.CUSTOMER);
        addIfTrue(result, entity.isDocument(), entity.getId(), "document", module, DynamicFieldFor.CUSTOMER);
        return result;
    }

    private List<AttributeContractResponse> toBeneficiaryResponses(BeneficiaryAttributeContractEntity entity) {
        List<AttributeContractResponse> result = new ArrayList<>();
        String module = "beneficiary";
        DynamicFieldFor type = DynamicFieldFor.BENEFICIARY;

        addIfTrue(result, entity.isFirstName(), entity.getId(), "beneficiary/firstName", module, type);
        addIfTrue(result, entity.isLastName(), entity.getId(), "beneficiary/lastName", module, type);
        addIfTrue(result, entity.isAccountNumber(), entity.getId(), "beneficiary/accountNumber", module, type);
        addIfTrue(result, entity.isBankName(), entity.getId(), "beneficiary/bankName", module, type);
        addIfTrue(result, entity.isIfscCode(), entity.getId(), "beneficiary/ifscCode", module, type);
        addIfTrue(result, entity.isRelationship(), entity.getId(), "beneficiary/relationship", module, type);

        return result;
    }

    private List<AttributeContractResponse> toTransactionResponses(TransactionAttributeContractEntity entity) {
        List<AttributeContractResponse> result = new ArrayList<>();
        String module = "transaction";
        DynamicFieldFor type = DynamicFieldFor.TRANSACTION;

        addIfTrue(result, entity.isAmount(), entity.getId(), "transaction/amount", module, type);
        addIfTrue(result, entity.isCurrency(), entity.getId(), "transaction/currency", module, type);
        addIfTrue(result, entity.isPurpose(), entity.getId(), "transaction/purpose", module, type);
        addIfTrue(result, entity.isSourceOfFunds(), entity.getId(), "transaction/sourceOfFunds", module, type);
        addIfTrue(result, entity.isPaymentMethod(), entity.getId(), "transaction/paymentMethod", module, type);

        return result;
    }

    private void addIfTrue(List<AttributeContractResponse> target,
                           boolean flag,
                           UUID id,
                           String referenceModel,
                           String module,
                           DynamicFieldFor fieldType
    ) {
        if (!flag) {
            return;
        }
        target.add(
                AttributeContractResponse.builder()
                        .id(id)
                        .referenceModel(referenceModel)
                        .label(resolveLabel(referenceModel, module))
                        .fieldType(fieldType)
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
            label = staticMetadataResolver.resolveLabel(CUSTOMER_METADATA_PATH, referenceModel);
        } else if ("beneficiary".equalsIgnoreCase(module)) {
            label = staticMetadataResolver.resolveLabel(BENEFICIARY_METADATA_PATH, referenceModel);
        } else if ("transaction".equalsIgnoreCase(module)) {
            label = staticMetadataResolver.resolveLabel(TRANSACTION_METADATA_PATH, referenceModel);
        }

        if (label == null || label.equals(referenceModel)) {
            String documentLabel = staticMetadataResolver.resolveLabel(DOCUMENT_METADATA_PATH, referenceModel);
            if (!documentLabel.equals(referenceModel)) {
                label = documentLabel;
            }
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

}

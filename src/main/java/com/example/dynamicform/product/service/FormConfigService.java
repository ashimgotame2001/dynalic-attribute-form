package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.dto.FieldSpecRequest;
import com.example.dynamicform.product.entity.CustomerFormConfigurationEntity;
import com.example.dynamicform.product.repository.FormConfigurationRepository;
import com.example.dynamicform.product.repository.RSPWiseDocumentSetupRepository;
import com.example.dynamicform.platform.interpreter.DtoIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FormConfigService {

    private static final Logger logger = LoggerFactory.getLogger(FormConfigService.class);

    private final FormConfigurationRepository repository;
    private final ObjectMapper objectMapper;
    private final RSPAttributeContractService rspAttributeContractService;
    private final com.example.dynamicform.platform.interpreter.DtoIntrospector dtoIntrospector;
    private final com.example.dynamicform.product.repository.RSPWiseDocumentSetupRepository rspWiseDocumentSetupRepository;

    public FormConfigService(FormConfigurationRepository repository, 
                               ObjectMapper objectMapper, 
                               RSPAttributeContractService rspAttributeContractService,
                               com.example.dynamicform.platform.interpreter.DtoIntrospector dtoIntrospector,
                               com.example.dynamicform.product.repository.RSPWiseDocumentSetupRepository rspWiseDocumentSetupRepository) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.rspAttributeContractService = rspAttributeContractService;
        this.dtoIntrospector = dtoIntrospector;
        this.rspWiseDocumentSetupRepository = rspWiseDocumentSetupRepository;
    }

    public List<CustomerFormConfigurationEntity> getAllConfigurations() {
        return repository.findAll();
    }

    public Optional<CustomerFormConfigurationEntity> getConfiguration(String formName, Integer version) {
        if (version != null && version > 0) {
            return repository.findByFormNameAndVersion(formName, version);
        }
        return repository.findFirstByFormNameOrderByVersionDesc(formName);
    }

    public CustomerFormConfigurationEntity getActiveConfiguration(String formName) {
        Optional<CustomerFormConfigurationEntity> opt = repository.findLatestActiveByFormName(formName);
        return opt.orElse(null);
    }

    public CustomerFormConfigurationEntity findActiveConfiguration(String formName) {
        return getActiveConfiguration(formName);
    }

    public CustomerFormConfigurationEntity findActiveConfiguration(String formName, int version) {
        return repository.findByFormNameAndVersion(formName, version).orElse(null);
    }

    @CacheEvict(value = "formDefinitions", key = "#request.formName")
    @Transactional
    public CustomerFormConfigurationEntity createOrUpdateConfiguration(FieldSpecRequest request, Class<?> targetClass) {
        return generateMetaData(request, "customer", targetClass);
    }

    @Transactional
    public CustomerFormConfigurationEntity generateMetaDataForCustomer(FieldSpecRequest request, Class<?> targetClass) {
        return generateMetaData(request, "customer", targetClass);
    }

    @Transactional
    public CustomerFormConfigurationEntity generateMetaDataForBeneficiary(FieldSpecRequest request, Class<?> targetClass) {
        return generateMetaData(request, "beneficiary", targetClass);
    }

    @Transactional
    public CustomerFormConfigurationEntity generateMetaDataForTransaction(FieldSpecRequest request, Class<?> targetClass) {
        return generateMetaData(request, "transaction", targetClass);
    }

    private CustomerFormConfigurationEntity generateMetaData(FieldSpecRequest request, String module, Class<?> targetClass) {
        String formName = request.getFormName();
        Long rspId = request.getRspId();

        try {

            // Get enabled reference models from RSP contracts
            List<com.example.dynamicform.product.dto.RSPAttributeContractResponse> contracts = rspAttributeContractService.findByRspId(rspId, module);
            java.util.Set<String> enabledReferenceModels = contracts.stream()
                    .map(c -> c.getReferenceModel().replace(".", "/"))
                    .collect(java.util.stream.Collectors.toSet());

            // Build RawFormMetadata by introspecting the DTO and field specs
            com.example.dynamicform.platform.dto.metadata.RawFormMetadata metadata = dtoIntrospector.buildMetadata(
                    request.getFields(),
                    formName,
                    targetClass,
                    enabledReferenceModels,
                    null
            );

            // Enhance document collection metadata with RSP-specific primary/secondary counts
            if (enabledReferenceModels.contains("document")) {
                com.example.dynamicform.product.dto.RSPAttributeContractResponse docContract = contracts.stream()
                        .filter(c -> "document".equals(c.getReferenceModel()))
                        .findFirst()
                        .orElse(null);

                if (docContract != null && (docContract.getMinPrimaryDocuments() != null || docContract.getMinSecondaryDocuments() != null)) {
                    metadata.getDomainModel().getAttributes().stream()
                            .filter(attr -> "document".equals(attr.getAttributeName()))
                            .findFirst()
                            .ifPresent(attr -> {
                                List<java.util.Map<String, Object>> validations = attr.getValidations();
                                if (validations == null) {
                                    validations = new java.util.ArrayList<>();
                                    attr.setValidations(validations);
                                }
                                if (docContract.getMinPrimaryDocuments() != null) {
                                    java.util.Map<String, Object> rule = new java.util.HashMap<>();
                                    java.util.Map<String, Object> params = new java.util.HashMap<>();
                                    params.put("value", docContract.getMinPrimaryDocuments());
                                    params.put("message", "At least " + docContract.getMinPrimaryDocuments() + " primary document(s) required");
                                    rule.put("minPrimaryDocuments", params);
                                    validations.add(rule);
                                }
                                if (docContract.getMinSecondaryDocuments() != null) {
                                    java.util.Map<String, Object> rule = new java.util.HashMap<>();
                                    java.util.Map<String, Object> params = new java.util.HashMap<>();
                                    params.put("value", docContract.getMinSecondaryDocuments());
                                    params.put("message", "At least " + docContract.getMinSecondaryDocuments() + " secondary document(s) required");
                                    rule.put("minSecondaryDocuments", params);
                                    validations.add(rule);
                                }
                            });
                }
            }

            // Resolve labels based on module metadata
            metadata.getDomainModel().getAttributes().forEach(attr -> resolveLabelsRecursive(attr, module));
            metadata.setTargetClassName(targetClass.getName());

            String metadataJson = objectMapper.writeValueAsString(metadata);

            Optional<CustomerFormConfigurationEntity> latestOpt = repository.findFirstByFormNameOrderByVersionDesc(formName);
            int newVersion = latestOpt.map(c -> c.getVersion() + 1).orElse(1);

            CustomerFormConfigurationEntity entity = CustomerFormConfigurationEntity.builder()
                    .formName(formName)
                    .version(newVersion)
                    .metadataJson(metadataJson)
                    .description(request.getDescription())
                    .rspId(rspId)
                    .isActive(true)
                    .build();

            if (latestOpt.isPresent()) {
                CustomerFormConfigurationEntity previous = latestOpt.get();
                previous.setIsActive(false);
                repository.save(previous);
            }

            CustomerFormConfigurationEntity saved = repository.save(entity);
            return saved;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize metadata for " + formName, e);
        }
    }

    private void resolveLabelsRecursive(com.example.dynamicform.platform.dto.metadata.RawDomainAttribute attr, String module) {
        if (attr.getReferenceModel() != null) {
            String resolvedLabel = rspAttributeContractService.resolveLabel(attr.getReferenceModel(), module);
            if (resolvedLabel != null && !resolvedLabel.isBlank()) {
                // User requirement: If it's a reference/object, it doesn't need shortLabel or longLabel
                if (Boolean.TRUE.equals(attr.getReference()) || Boolean.TRUE.equals(attr.getAssociation())) {
                    attr.setShortLabel(null);
                    attr.setLongLabel(null);
                } else {
                    attr.setShortLabel(resolvedLabel);
                    attr.setLongLabel(resolvedLabel);
                }
            }
        }
        if (attr.getDomainModel() != null && attr.getDomainModel().getAttributes() != null) {
            attr.getDomainModel().getAttributes().forEach(child -> resolveLabelsRecursive(child, module));
        }
    }

    @CacheEvict(value = "formDefinitions", key = "#formName")
    public void deleteConfiguration(String formName, Integer version) {
        Optional<CustomerFormConfigurationEntity> opt = repository.findByFormNameAndVersion(formName, version);
        if (opt.isPresent()) {
            repository.delete(opt.get());
            logger.info("Deleted form configuration for {} version {}", formName, version);
        }
    }

    public List<Integer> getAvailableVersions(String formName) {
        List<CustomerFormConfigurationEntity> versions = repository.findByFormNameOrderByVersionDesc(formName);
        return versions.stream().map(CustomerFormConfigurationEntity::getVersion).toList();
    }

    public boolean existsByFormName(String formName) {
        return repository.existsByFormName(formName);
    }
}

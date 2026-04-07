package com.example.dynamicform.product.service;

import com.example.dynamicform.platform.dto.FieldSpecRequest;
import com.example.dynamicform.platform.dto.DynamicMetadataBuildRequest;
import com.example.dynamicform.platform.dto.FormConfigResponse;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.service.ResponseLocalizationService;
import com.example.dynamicform.product.dto.FormConfigTranslationRequest;
import com.example.dynamicform.product.entity.CustomerFormConfigurationEntity;
import com.example.dynamicform.product.entity.FormConfigFieldEntity;
import com.example.dynamicform.product.entity.FormConfigFieldTranslationEntity;
import com.example.dynamicform.product.entity.FormConfigFieldValidationEntity;
import com.example.dynamicform.product.entity.FormConfigFieldValidationTranslationEntity;
import com.example.dynamicform.product.repository.FormConfigurationRepository;
import com.example.dynamicform.product.repository.RSPWiseDocumentSetupRepository;
import com.example.dynamicform.platform.service.DynamicMetadataBuildService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FormConfigService {

    private static final Logger logger = LoggerFactory.getLogger(FormConfigService.class);
    private static final String CUSTOMER_METADATA_PATH = "customer_static_metadata.json";
    private static final String BENEFICIARY_METADATA_PATH = "beneficiary_static_metadata.json";
    private static final String TRANSACTION_METADATA_PATH = "transaction_static_metadata.json";
    private static final String DOCUMENT_METADATA_PATH = "document_metadata.json";

    private final FormConfigurationRepository repository;
    private final ObjectMapper objectMapper;
    private final RSPAttributeContractService rspAttributeContractService;
    private final DynamicMetadataBuildService dynamicMetadataBuildService;
    private final FormConfigFieldAdapter formConfigFieldAdapter;
    private final FormConfigMetadataResolver formConfigMetadataResolver;
    private final ResponseLocalizationService responseLocalizationService;
    private final com.example.dynamicform.product.repository.RSPWiseDocumentSetupRepository rspWiseDocumentSetupRepository;

    public FormConfigService(FormConfigurationRepository repository, 
                               ObjectMapper objectMapper, 
                               RSPAttributeContractService rspAttributeContractService,
                               DynamicMetadataBuildService dynamicMetadataBuildService,
                               FormConfigFieldAdapter formConfigFieldAdapter,
                               FormConfigMetadataResolver formConfigMetadataResolver,
                               ResponseLocalizationService responseLocalizationService,
                               com.example.dynamicform.product.repository.RSPWiseDocumentSetupRepository rspWiseDocumentSetupRepository) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.rspAttributeContractService = rspAttributeContractService;
        this.dynamicMetadataBuildService = dynamicMetadataBuildService;
        this.formConfigFieldAdapter = formConfigFieldAdapter;
        this.formConfigMetadataResolver = formConfigMetadataResolver;
        this.responseLocalizationService = responseLocalizationService;
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

    public FormConfigResponse buildResponse(CustomerFormConfigurationEntity entity, String language) {
        RawFormMetadata metadata = language == null || language.isBlank()
                ? formConfigMetadataResolver.resolve(entity)
                : formConfigMetadataResolver.resolveWithTranslations(loadDetailedConfiguration(entity.getId()));
        return FormConfigResponse.builder()
                .formName(entity.getFormName())
                .version(entity.getVersion())
                .description(entity.getDescription())
                .rspId(entity.getRspId())
                .isActive(entity.getIsActive())
                .metadata(responseLocalizationService.prepareRawMetadataResponse(metadata, language))
                .targetDtoClassName(metadata != null ? metadata.getTargetClassName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public RawFormMetadata resolveMetadata(CustomerFormConfigurationEntity entity) {
        return formConfigMetadataResolver.resolve(entity);
    }

    @CacheEvict(value = "formDefinitions", key = "#request.formName")
    @Transactional
    public CustomerFormConfigurationEntity updateTranslations(FormConfigTranslationRequest request) {
        CustomerFormConfigurationEntity entity = resolveTranslationTarget(request.getFormName(), request.getVersion());
        Map<String, FormConfigFieldEntity> fieldsByReferenceModel = new LinkedHashMap<>();
        for (FormConfigFieldEntity fieldConfig : entity.getFieldConfigs()) {
            if (fieldConfig.getReferenceModel() != null) {
                fieldsByReferenceModel.put(fieldConfig.getReferenceModel(), fieldConfig);
            }
        }

        if (request.getFields() != null) {
            for (FormConfigTranslationRequest.FieldTranslation fieldTranslation : request.getFields()) {
                if (fieldTranslation == null || fieldTranslation.getReferenceModel() == null) {
                    continue;
                }
                FormConfigFieldEntity fieldEntity = fieldsByReferenceModel.get(fieldTranslation.getReferenceModel());
                if (fieldEntity == null) {
                    continue;
                }
                mergeFieldTranslations(fieldEntity, fieldTranslation);
            }
        }

        entity.setMetadataJson(formConfigMetadataResolver.toMetadataJson(entity));
        return repository.save(entity);
    }

    @CacheEvict(value = "formDefinitions", key = "#request.formName")
    @Transactional
    public CustomerFormConfigurationEntity createOrUpdateConfiguration(FieldSpecRequest request, Class<?> targetClass) {
        return generateMetaData(request, "customer", targetClass, CUSTOMER_METADATA_PATH, "Customer Management");
    }

    @Transactional
    public CustomerFormConfigurationEntity generateMetaDataForCustomer(FieldSpecRequest request, Class<?> targetClass) {
        return generateMetaData(request, "customer", targetClass, CUSTOMER_METADATA_PATH, "Customer Management");
    }

    @Transactional
    public CustomerFormConfigurationEntity generateMetaDataForBeneficiary(FieldSpecRequest request, Class<?> targetClass) {
        return generateMetaData(request, "beneficiary", targetClass, BENEFICIARY_METADATA_PATH, "Beneficiary Management");
    }

    @Transactional
    public CustomerFormConfigurationEntity generateMetaDataForTransaction(FieldSpecRequest request, Class<?> targetClass) {
        return generateMetaData(request, "transaction", targetClass, TRANSACTION_METADATA_PATH, "Transaction Management");
    }

    public CustomerFormConfigurationEntity generateGenericMetaData(FieldSpecRequest request,
                                                                  String targetClassName,
                                                                  String staticMetadataPath,
                                                                  String moduleName,
                                                                  String artifactName) {
        return generateMetaData(request, moduleName != null ? moduleName : "generic",
                targetClassName,
                staticMetadataPath,
                artifactName,
                moduleName != null ? moduleName : "Dynamic Form");
    }

    private CustomerFormConfigurationEntity generateMetaData(FieldSpecRequest request,
                                                             String module,
                                                             Class<?> targetClass,
                                                             String staticMetadataPath,
                                                             String moduleName) {
        return generateMetaData(request, module, targetClass.getName(), staticMetadataPath, request.getFormName(), moduleName);
    }

    private CustomerFormConfigurationEntity generateMetaData(FieldSpecRequest request,
                                                             String module,
                                                             String targetClassName,
                                                             String staticMetadataPath,
                                                             String artifactName,
                                                             String moduleName) {
        String formName = request.getFormName();
        Long rspId = request.getRspId();

        try {

            List<com.example.dynamicform.product.dto.RSPAttributeContractResponse> contracts = rspAttributeContractService.findByRspId(rspId, module);
            java.util.Set<String> enabledReferenceModels = contracts.stream()
                    .map(c -> c.getReferenceModel().replace(".", "/"))
                    .collect(java.util.stream.Collectors.toSet());

            com.example.dynamicform.platform.dto.metadata.RawFormMetadata metadata = dynamicMetadataBuildService.build(
                    DynamicMetadataBuildRequest.builder()
                            .formName(formName)
                            .moduleName(moduleName)
                            .artifactName(artifactName)
                            .targetClassName(targetClassName)
                            .staticMetadataPath(staticMetadataPath)
                            .fallbackStaticMetadataPaths(java.util.List.of(DOCUMENT_METADATA_PATH))
                            .fields(request.getFields())
                            .enabledReferenceModels(enabledReferenceModels)
                            .build()
            );


            String metadataJson = objectMapper.writeValueAsString(metadata);

            Optional<CustomerFormConfigurationEntity> latestOpt = repository.findFirstByFormNameOrderByVersionDesc(formName);
            int newVersion = latestOpt.map(c -> c.getVersion() + 1).orElse(1);

            CustomerFormConfigurationEntity entity = CustomerFormConfigurationEntity.builder()
                    .formName(formName)
                    .version(newVersion)
                    .metadataJson(metadataJson)
                    .targetClassName(targetClassName)
                    .moduleName(moduleName)
                    .artifactName(artifactName)
                    .staticMetadataPath(staticMetadataPath)
                    .description(request.getDescription())
                    .rspId(rspId)
                    .isActive(true)
                    .fieldConfigs(new java.util.LinkedHashSet<>())
                    .build();
            entity.getFieldConfigs().addAll(formConfigFieldAdapter.toEntities(entity, request.getFields()));

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

    private CustomerFormConfigurationEntity resolveTranslationTarget(String formName, Integer version) {
        if (version != null && version > 0) {
            return repository.findDetailedByFormNameAndVersion(formName, version)
                    .orElseThrow(() -> new IllegalArgumentException("Form configuration not found for " + formName + " version " + version));
        }
        return repository.findDetailedLatestActiveByFormName(formName)
                .orElseThrow(() -> new IllegalArgumentException("Active form configuration not found for " + formName));
    }

    private CustomerFormConfigurationEntity loadDetailedConfiguration(java.util.UUID id) {
        return repository.findDetailedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Form configuration not found with id " + id));
    }

    private void mergeFieldTranslations(FormConfigFieldEntity fieldEntity,
                                        FormConfigTranslationRequest.FieldTranslation fieldTranslation) {
        Map<String, FormConfigFieldTranslationEntity> translationsByLocale = new LinkedHashMap<>();
        for (FormConfigFieldTranslationEntity translation : fieldEntity.getTranslations()) {
            if (translation.getLocale() != null) {
                translationsByLocale.put(translation.getLocale(), translation);
            }
        }
        mergeLabelTranslations(fieldEntity, translationsByLocale, fieldTranslation.getShortLabelI18n(), true);
        mergeLabelTranslations(fieldEntity, translationsByLocale, fieldTranslation.getLongLabelI18n(), false);

        if (fieldTranslation.getValidations() == null) {
            return;
        }
        Map<String, FormConfigFieldValidationEntity> validationsByType = new LinkedHashMap<>();
        for (FormConfigFieldValidationEntity validation : fieldEntity.getValidations()) {
            if (validation.getValidationType() != null) {
                validationsByType.put(validation.getValidationType(), validation);
            }
        }
        for (FormConfigTranslationRequest.ValidationTranslation validationTranslation : fieldTranslation.getValidations()) {
            if (validationTranslation == null || validationTranslation.getType() == null) {
                continue;
            }
            FormConfigFieldValidationEntity validationEntity = validationsByType.get(validationTranslation.getType());
            if (validationEntity == null) {
                continue;
            }
            mergeValidationTranslations(validationEntity, validationTranslation.getMessageI18n());
        }
    }

    private void mergeLabelTranslations(FormConfigFieldEntity fieldEntity,
                                        Map<String, FormConfigFieldTranslationEntity> translationsByLocale,
                                        Map<String, String> values,
                                        boolean shortLabel) {
        if (values == null || values.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }
            FormConfigFieldTranslationEntity translation = translationsByLocale.computeIfAbsent(entry.getKey(), locale -> {
                FormConfigFieldTranslationEntity created = FormConfigFieldTranslationEntity.builder()
                        .field(fieldEntity)
                        .locale(locale)
                        .build();
                fieldEntity.getTranslations().add(created);
                return created;
            });
            if (shortLabel) {
                translation.setShortLabel(entry.getValue());
            } else {
                translation.setLongLabel(entry.getValue());
            }
        }
    }

    private void mergeValidationTranslations(FormConfigFieldValidationEntity validationEntity,
                                             Map<String, String> messageI18n) {
        if (messageI18n == null || messageI18n.isEmpty()) {
            return;
        }
        Map<String, FormConfigFieldValidationTranslationEntity> translationsByLocale = new LinkedHashMap<>();
        for (FormConfigFieldValidationTranslationEntity translation : validationEntity.getTranslations()) {
            if (translation.getLocale() != null) {
                translationsByLocale.put(translation.getLocale(), translation);
            }
        }
        for (Map.Entry<String, String> entry : messageI18n.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }
            FormConfigFieldValidationTranslationEntity translation = translationsByLocale.computeIfAbsent(entry.getKey(), locale -> {
                FormConfigFieldValidationTranslationEntity created = FormConfigFieldValidationTranslationEntity.builder()
                        .validation(validationEntity)
                        .locale(locale)
                        .build();
                validationEntity.getTranslations().add(created);
                return created;
            });
            translation.setMessage(entry.getValue());
        }
    }
}

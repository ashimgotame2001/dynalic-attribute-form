package com.example.dynamicform.service;

import com.example.dynamicform.entity.CustomerFormConfigurationEntity;
import com.example.dynamicform.repository.FormConfigurationRepository;
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

    public FormConfigService(FormConfigurationRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
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

    @CacheEvict(value = "formDefinitions", key = "#formName")
    @Transactional
    public CustomerFormConfigurationEntity createOrUpdateConfiguration(String formName, String metadataJson, String description, String targetDtoClass, Long rspId) {
        Optional<CustomerFormConfigurationEntity> latestOpt = repository.findFirstByFormNameOrderByVersionDesc(formName);
        int newVersion = latestOpt.map(c -> c.getVersion() + 1).orElse(1);

        CustomerFormConfigurationEntity entity = CustomerFormConfigurationEntity.builder()
                .formName(formName)
                .version(newVersion)
                .metadataJson(metadataJson)
                .description(description)
                .targetDtoClassName(targetDtoClass)
                .rspId(rspId)
                .isActive(true)
                .build();

        if (latestOpt.isPresent()) {
            CustomerFormConfigurationEntity previous = latestOpt.get();
            previous.setIsActive(false);
            repository.save(previous);
        }

        CustomerFormConfigurationEntity saved = repository.save(entity);
        logger.info("Saved form configuration for {} version {}", formName, newVersion);
        return saved;
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

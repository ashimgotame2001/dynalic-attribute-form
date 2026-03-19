package com.example.dynamicform.service;

import com.example.dynamicform.entity.FormConfigurationEntity;
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

    public List<FormConfigurationEntity> getAllConfigurations() {
        return repository.findAll();
    }

    public Optional<FormConfigurationEntity> getConfiguration(String formName, Integer version) {
        if (version != null && version > 0) {
            return repository.findByFormNameAndVersion(formName, version);
        }
        return repository.findFirstByFormNameOrderByVersionDesc(formName);
    }

    public FormConfigurationEntity getActiveConfiguration(String formName) {
        Optional<FormConfigurationEntity> opt = repository.findLatestActiveByFormName(formName);
        return opt.orElse(null);
    }

    public FormConfigurationEntity findActiveConfiguration(String formName) {
        return getActiveConfiguration(formName);
    }

    public FormConfigurationEntity findActiveConfiguration(String formName, int version) {
        return repository.findByFormNameAndVersion(formName, version).orElse(null);
    }

    @CacheEvict(value = "formDefinitions", key = "#formName")
    @Transactional
    public FormConfigurationEntity createOrUpdateConfiguration(String formName, String metadataJson, String description, String targetDtoClass, Long rspId, com.example.dynamicform.enums.DynamicFieldFor fieldType) {
        Optional<FormConfigurationEntity> latestOpt = repository.findFirstByFormNameOrderByVersionDesc(formName);
        int newVersion = latestOpt.map(c -> c.getVersion() + 1).orElse(1);

        FormConfigurationEntity entity = FormConfigurationEntity.builder()
                .formName(formName)
                .version(newVersion)
                .metadataJson(metadataJson)
                .description(description)
                .targetDtoClassName(targetDtoClass)
                .rspId(rspId)
                .fieldType(fieldType)
                .isActive(true)
                .build();

        if (latestOpt.isPresent()) {
            FormConfigurationEntity previous = latestOpt.get();
            previous.setIsActive(false);
            repository.save(previous);
        }

        FormConfigurationEntity saved = repository.save(entity);
        logger.info("Saved form configuration for {} version {}", formName, newVersion);
        return saved;
    }

    @CacheEvict(value = "formDefinitions", key = "#formName")
    public void deleteConfiguration(String formName, Integer version) {
        Optional<FormConfigurationEntity> opt = repository.findByFormNameAndVersion(formName, version);
        if (opt.isPresent()) {
            repository.delete(opt.get());
            logger.info("Deleted form configuration for {} version {}", formName, version);
        }
    }

    public List<Integer> getAvailableVersions(String formName) {
        List<FormConfigurationEntity> versions = repository.findByFormNameOrderByVersionDesc(formName);
        return versions.stream().map(FormConfigurationEntity::getVersion).toList();
    }

    public boolean existsByFormName(String formName) {
        return repository.existsByFormName(formName);
    }
}

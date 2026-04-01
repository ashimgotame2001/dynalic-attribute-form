package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.dto.DynamicMetadataBuildRequest;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implementation of RuntimeMetadataGenerator.
 * Uses the existing DynamicMetadataBuildService to generate metadata from domain models.
 */
@Service
public class RuntimeMetadataGeneratorImpl implements RuntimeMetadataGenerator {

    private final DynamicMetadataBuildService dynamicMetadataBuildService;
    private final ContextBasedConfigurationService contextBasedConfigurationService;

    public RuntimeMetadataGeneratorImpl(DynamicMetadataBuildService dynamicMetadataBuildService,
                                        ContextBasedConfigurationService contextBasedConfigurationService) {
        this.dynamicMetadataBuildService = dynamicMetadataBuildService;
        this.contextBasedConfigurationService = contextBasedConfigurationService;
    }

    @Override
    @Cacheable(value = "metadataDefinitions", key = "#domainClass.name + ':' + #formName")
    public RawFormMetadata generateMetadata(Class<?> domainClass, String formName) {
        return generateMetadata(domainClass, formName, null);
    }

    @Override
    @Cacheable(value = "metadataDefinitions", key = "#domainClass.name + ':' + #formName + ':' + (#context == null ? 'default' : #context)")
    public RawFormMetadata generateMetadata(Class<?> domainClass, String formName, String context) {
        DynamicMetadataBuildRequest request = DynamicMetadataBuildRequest.builder()
                .targetClassName(domainClass.getName())
                .formName(formName)
                .fields(Collections.emptyList()) // Will be introspected from the class
                .enabledReferenceModels(Collections.emptySet())
                .staticMetadataPath(null)
                .fallbackStaticMetadataPaths(Collections.emptyList())
                .moduleName("Dynamic Form")
                .artifactName(formName)
                .build();

        RawFormMetadata metadata = dynamicMetadataBuildService.build(request);

        // Apply context if provided
        if (context != null && !context.isBlank()) {
            metadata = contextBasedConfigurationService.applyContextConfiguration(metadata, context);
        }

        return metadata;
    }
}

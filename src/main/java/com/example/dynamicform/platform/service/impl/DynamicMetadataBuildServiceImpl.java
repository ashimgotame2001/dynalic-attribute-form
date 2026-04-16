package com.example.dynamicform.platform.service.impl;

import com.example.dynamicform.platform.api.dto.DynamicMetadataBuildRequest;
import com.example.dynamicform.platform.api.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.common.util.MetadataUtils;
import com.example.dynamicform.platform.core.engine.DtoIntrospector;
import com.example.dynamicform.platform.core.metadata.access.StaticMetadataResolver;
import com.example.dynamicform.platform.core.metadata.access.TargetClassResolver;
import com.example.dynamicform.platform.service.DynamicMetadataBuildService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicMetadataBuildServiceImpl implements DynamicMetadataBuildService {

    private final DtoIntrospector dtoIntrospector;
    private final StaticMetadataResolver staticMetadataResolver;
    private final TargetClassResolver targetClassResolver;

    public DynamicMetadataBuildServiceImpl(DtoIntrospector dtoIntrospector,
                                           StaticMetadataResolver staticMetadataResolver,
                                           TargetClassResolver targetClassResolver) {
        this.dtoIntrospector = dtoIntrospector;
        this.staticMetadataResolver = staticMetadataResolver;
        this.targetClassResolver = targetClassResolver;
    }

    @Override
    public RawFormMetadata build(DynamicMetadataBuildRequest request) {
        Class<?> targetClass = targetClassResolver.resolve(request.getTargetClassName());
        RawFormMetadata metadata = dtoIntrospector.buildMetadata(
                request.getFields(),
                request.getFormName(),
                targetClass,
                request.getEnabledReferenceModels(),
                null
        );
        applyLabels(
                metadata.getDomainModel() != null ? metadata.getDomainModel().getAttributes() : null,
                request.getStaticMetadataPath(),
                request.getFallbackStaticMetadataPaths());
        metadata.setModuleName(request.getModuleName());
        metadata.setArtifactName(request.getArtifactName());
        metadata.setVersion("1.0.0");
        metadata.setTargetClassName(targetClass.getName());
        return metadata;
    }

    private void applyLabels(List<RawDomainAttribute> attributes, String staticMetadataPath, List<String> fallbackPaths) {
        if (attributes == null) {
            return;
        }
        for (RawDomainAttribute attribute : attributes) {
            if (attribute.getReferenceModel() != null && !attribute.getReferenceModel().isBlank()) {
                if (Boolean.TRUE.equals(attribute.getReference()) || Boolean.TRUE.equals(attribute.getAssociation())) {
                    attribute.setShortLabel(resolveLabel(staticMetadataPath, fallbackPaths, attribute.getReferenceModel(), attribute.getAttributeName()));
                    attribute.setLongLabel(attribute.getLongLabel() != null ? attribute.getLongLabel() : attribute.getShortLabel());
                } else {
                    if (attribute.getShortLabel() == null || attribute.getShortLabel().isBlank()) {
                        attribute.setShortLabel(resolveLabel(staticMetadataPath, fallbackPaths, attribute.getReferenceModel(), attribute.getAttributeName()));
                    }
                    if (attribute.getLongLabel() == null || attribute.getLongLabel().isBlank()) {
                        attribute.setLongLabel(attribute.getShortLabel());
                    }
                }
            }
            if (attribute.getDomainModel() != null && attribute.getDomainModel().getAttributes() != null) {
                applyLabels(attribute.getDomainModel().getAttributes(), staticMetadataPath, fallbackPaths);
            }
        }
    }

    private String resolveLabel(String staticMetadataPath, List<String> fallbackPaths, String referenceModel, String fallbackLabel) {
        String label = staticMetadataResolver.resolveLabel(staticMetadataPath, referenceModel);
        if (label != null && !label.equals(referenceModel)) {
            return label;
        }
        if (fallbackPaths != null) {
            for (String fallbackPath : fallbackPaths) {
                label = staticMetadataResolver.resolveLabel(fallbackPath, referenceModel);
                if (label != null && !label.equals(referenceModel)) {
                    return label;
                }
            }
        }
        String candidate = fallbackLabel;
        if (candidate == null || candidate.isBlank()) {
            candidate = referenceModel;
            if (candidate != null && candidate.contains("/")) {
                candidate = candidate.substring(candidate.lastIndexOf('/') + 1);
            }
        }
        return MetadataUtils.toLabel(candidate);
    }
}

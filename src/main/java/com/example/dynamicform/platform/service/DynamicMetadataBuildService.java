package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.dto.DynamicMetadataBuildRequest;
import com.example.dynamicform.platform.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.interpreter.DtoIntrospector;
import com.example.dynamicform.platform.metadata.StaticMetadataResolver;
import com.example.dynamicform.platform.resolver.TargetClassResolver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicMetadataBuildService {

    private final DtoIntrospector dtoIntrospector;
    private final StaticMetadataResolver staticMetadataResolver;
    private final TargetClassResolver targetClassResolver;

    public DynamicMetadataBuildService(DtoIntrospector dtoIntrospector,
                                       StaticMetadataResolver staticMetadataResolver,
                                       TargetClassResolver targetClassResolver) {
        this.dtoIntrospector = dtoIntrospector;
        this.staticMetadataResolver = staticMetadataResolver;
        this.targetClassResolver = targetClassResolver;
    }

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
                    attribute.setShortLabel(null);
                    attribute.setLongLabel(null);
                } else {
                    if (attribute.getShortLabel() == null) {
                        attribute.setShortLabel("");
                    }
                    if (attribute.getLongLabel() == null) {
                        attribute.setLongLabel("");
                    }
                }
            }
            if (attribute.getDomainModel() != null && attribute.getDomainModel().getAttributes() != null) {
                applyLabels(attribute.getDomainModel().getAttributes(), staticMetadataPath, fallbackPaths);
            }
        }
    }

}

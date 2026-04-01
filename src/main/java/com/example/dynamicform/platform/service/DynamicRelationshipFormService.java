package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.dto.metadata.RawDomainModel;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import com.example.dynamicform.platform.util.MetadataUtils;
import com.example.dynamicform.product.dto.RelationshipDefinitionDTO;
import com.example.dynamicform.product.enums.RelationshipType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Expands raw form metadata with runtime relationship definitions so forms can
 * render relationship-backed sections dynamically.
 */
@Service
public class DynamicRelationshipFormService {

    private static final List<String> TARGET_ENTITY_PACKAGES = List.of(
            "com.example.dynamicform.product.model",
            "com.example.dynamicform.product.entity"
    );

    private final RuntimeRelationshipPlatform relationshipPlatform;
    private final RuntimeMetadataGenerator runtimeMetadataGenerator;

    public DynamicRelationshipFormService(RuntimeRelationshipPlatform relationshipPlatform,
                                          RuntimeMetadataGenerator runtimeMetadataGenerator) {
        this.relationshipPlatform = relationshipPlatform;
        this.runtimeMetadataGenerator = runtimeMetadataGenerator;
    }

    public RawFormMetadata augmentWithRelationships(RawFormMetadata rawMetadata, String formName) {
        if (rawMetadata == null) {
            return null;
        }

        RawFormMetadata copy = rawMetadata.toBuilder().build();
        Set<String> rootCandidates = collectRootEntityCandidates(rawMetadata, formName);
        copy.setDomainModel(augmentDomainModel(copy.getDomainModel(), rootCandidates));
        return copy;
    }

    private boolean shouldRenderRelationship(String sourceEntity, RelationshipDefinitionDTO relationship) {
        return relationship != null
                && relationship.isActive()
                && sourceEntity.equalsIgnoreCase(relationship.getSourceEntity())
                && relationship.getRelationshipName() != null
                && relationship.getTargetEntity() != null;
    }

    private RawDomainAttribute buildRelationshipAttribute(String attributeName, RelationshipDefinitionDTO relationship) {
        RawDomainModel nestedDomainModel = resolveTargetDomainModel(relationship.getTargetEntity());
        String labelBase = relationship.getDescription() != null && !relationship.getDescription().isBlank()
                ? relationship.getDescription()
                : MetadataUtils.toLabel(relationship.getTargetEntity());

        return RawDomainAttribute.builder()
                .modelName(relationship.getTargetEntity())
                .referenceModel("relationship/" + relationship.getRelationshipName())
                .attributeName(attributeName)
                .attributeType(relationship.getTargetEntity())
                .reference(Boolean.valueOf(relationship.isReference()))
                .association(Boolean.valueOf(relationship.isAssociation()))
                .composition(Boolean.valueOf(relationship.isComposition()))
                .collection(Boolean.valueOf(relationship.isCollection()))
                .visible(Boolean.TRUE)
                .shortLabel(labelBase)
                .longLabel("Runtime relationship: " + relationship.getRelationshipName())
                .domainModel(nestedDomainModel)
                .build();
    }

    private RawDomainModel resolveTargetDomainModel(String targetEntity) {
        Optional<Class<?>> targetClass = resolveTargetClass(targetEntity);
        if (targetClass.isEmpty()) {
            return RawDomainModel.builder().attributes(List.of()).build();
        }

        RawFormMetadata targetMetadata = runtimeMetadataGenerator.generateMetadata(targetClass.get(), targetEntity, "relationship");
        return targetMetadata != null ? targetMetadata.getDomainModel() : RawDomainModel.builder().attributes(List.of()).build();
    }

    private Optional<Class<?>> resolveTargetClass(String targetEntity) {
        if (targetEntity == null || targetEntity.isBlank()) {
            return Optional.empty();
        }

        if (targetEntity.contains(".")) {
            try {
                return Optional.of(Class.forName(targetEntity));
            } catch (ClassNotFoundException ignored) {
                return Optional.empty();
            }
        }

        for (String packageName : TARGET_ENTITY_PACKAGES) {
            try {
                return Optional.of(Class.forName(packageName + "." + targetEntity));
            } catch (ClassNotFoundException ignored) {
                // try next package
            }
        }
        return Optional.empty();
    }

    private RawDomainModel augmentDomainModel(RawDomainModel domainModel, Set<String> currentEntityCandidates) {
        if (domainModel == null) {
            return null;
        }

        List<RawDomainAttribute> sourceAttributes = domainModel.getAttributes() != null
                ? domainModel.getAttributes()
                : List.of();
        List<RawDomainAttribute> attributes = new ArrayList<>();
        Set<String> existingAttributeNames = new LinkedHashSet<>();

        for (RawDomainAttribute attribute : sourceAttributes) {
            if (attribute == null) {
                continue;
            }

            RawDomainAttribute copy = attribute.toBuilder().build();
            Set<String> nestedCandidates = collectAttributeEntityCandidates(attribute);
            if (attribute.getDomainModel() != null) {
                copy.setDomainModel(augmentDomainModel(attribute.getDomainModel(), nestedCandidates));
            }
            attributes.add(copy);
            if (copy.getAttributeName() != null) {
                existingAttributeNames.add(copy.getAttributeName());
            }
        }

        for (String sourceEntity : currentEntityCandidates) {
            List<RelationshipDefinitionDTO> relationships = relationshipPlatform.getRelationshipDefinitionsByEntity(sourceEntity);
            for (RelationshipDefinitionDTO relationship : relationships) {
                if (!shouldRenderRelationship(sourceEntity, relationship)) {
                    continue;
                }

                String attributeName = buildRelationshipAttributeName(relationship);
                if (existingAttributeNames.contains(attributeName)) {
                    continue;
                }

                attributes.add(buildRelationshipAttribute(attributeName, relationship));
                existingAttributeNames.add(attributeName);
            }
        }

        return RawDomainModel.builder().attributes(attributes).build();
    }

    private Set<String> collectRootEntityCandidates(RawFormMetadata rawMetadata, String formName) {
        Set<String> candidates = new LinkedHashSet<>();
        if (formName != null && !formName.isBlank()) {
            candidates.add(formName);
            candidates.add(toPascalCase(formName));
        }
        if (rawMetadata.getTargetClassName() != null && !rawMetadata.getTargetClassName().isBlank()) {
            String targetClassName = rawMetadata.getTargetClassName();
            int lastDot = targetClassName.lastIndexOf('.');
            candidates.add(lastDot >= 0 ? targetClassName.substring(lastDot + 1) : targetClassName);
        }
        return candidates;
    }

    private Set<String> collectAttributeEntityCandidates(RawDomainAttribute attribute) {
        Set<String> candidates = new LinkedHashSet<>();
        if (attribute == null) {
            return candidates;
        }
        if (attribute.getModelName() != null && !attribute.getModelName().isBlank()) {
            candidates.add(attribute.getModelName());
        }
        if (attribute.getAttributeType() != null && !attribute.getAttributeType().isBlank()) {
            candidates.add(attribute.getAttributeType());
        }
        if (attribute.getReferenceModel() != null && !attribute.getReferenceModel().isBlank()) {
            String[] segments = attribute.getReferenceModel().split("/");
            for (String segment : segments) {
                if (!segment.isBlank()) {
                    candidates.add(segment);
                    candidates.add(toPascalCase(segment));
                }
            }
        }
        return candidates;
    }

    private String buildRelationshipAttributeName(RelationshipDefinitionDTO relationship) {
        return relationship.getRelationshipName();
    }

    private String toPascalCase(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String[] segments = value.split("[^A-Za-z0-9]+");
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (segment.isBlank()) {
                continue;
            }
            builder.append(segment.substring(0, 1).toUpperCase(Locale.ROOT));
            if (segment.length() > 1) {
                builder.append(segment.substring(1));
            }
        }
        return builder.toString();
    }
}

package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.dto.metadata.RawDomainModel;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RawMetadataCustomizationService {

    public RawFormMetadata customize(RawFormMetadata baseRaw, Set<String> enabledReferenceModels, Map<String, Object> requestAttributes) {
        if (baseRaw == null) {
            return null;
        }
        Set<String> requestPaths = collectRequestPaths(requestAttributes);
        RawFormMetadata customized = baseRaw.toBuilder().build();
        customized.setDomainModel(customizeDomainModel(baseRaw.getDomainModel(), enabledReferenceModels, requestPaths, ""));
        return customized;
    }

    private RawDomainModel customizeDomainModel(RawDomainModel domainModel,
                                                Set<String> enabledReferenceModels,
                                                Set<String> requestPaths,
                                                String parentPath) {
        if (domainModel == null || domainModel.getAttributes() == null) {
            return domainModel;
        }
        List<RawDomainAttribute> customized = new ArrayList<>();
        for (RawDomainAttribute attribute : domainModel.getAttributes()) {
            String referencePath = resolveReferencePath(attribute, parentPath);
            RawDomainAttribute copy = attribute.toBuilder().build();
            if (attribute.getDomainModel() != null) {
                copy.setDomainModel(customizeDomainModel(attribute.getDomainModel(), enabledReferenceModels, requestPaths, referencePath));
            }
            boolean hasNested = copy.getDomainModel() != null
                    && copy.getDomainModel().getAttributes() != null
                    && !copy.getDomainModel().getAttributes().isEmpty();
            boolean enabled = isEnabled(referencePath, enabledReferenceModels) || hasNested;
            if (!enabled) {
                continue;
            }
            if (!Boolean.TRUE.equals(copy.getReference()) && !Boolean.TRUE.equals(copy.getAssociation())) {
                copy.setVisible(isRequested(referencePath, requestPaths) ? Boolean.TRUE : copy.getVisible());
            } else if (hasNested && isRequested(referencePath, requestPaths)) {
                copy.setVisible(Boolean.TRUE);
            }
            customized.add(copy);
        }
        return RawDomainModel.builder().attributes(customized).build();
    }

    private boolean isEnabled(String referencePath, Set<String> enabledReferenceModels) {
        if (enabledReferenceModels == null || enabledReferenceModels.isEmpty()) {
            return true;
        }
        if (referencePath == null || referencePath.isBlank()) {
            return true;
        }
        for (String enabled : enabledReferenceModels) {
            if (enabled.equals(referencePath)
                    || enabled.startsWith(referencePath + "/")
                    || referencePath.startsWith(enabled + "/")) {
                return true;
            }
        }
        return false;
    }

    private boolean isRequested(String referencePath, Set<String> requestPaths) {
        if (requestPaths.isEmpty() || referencePath == null || referencePath.isBlank()) {
            return false;
        }
        for (String path : requestPaths) {
            if (path.equals(referencePath) || path.startsWith(referencePath + "/")) {
                return true;
            }
        }
        return false;
    }

    private String resolveReferencePath(RawDomainAttribute attribute, String parentPath) {
        if (attribute.getReferenceModel() != null && !attribute.getReferenceModel().isBlank()) {
            return attribute.getReferenceModel();
        }
        if (parentPath == null || parentPath.isBlank()) {
            return attribute.getAttributeName();
        }
        return parentPath + "/" + attribute.getAttributeName();
    }

    private Set<String> collectRequestPaths(Map<String, Object> requestAttributes) {
        Set<String> paths = new LinkedHashSet<>();
        collectRequestPaths("", requestAttributes, paths);
        return paths;
    }

    @SuppressWarnings("unchecked")
    private void collectRequestPaths(String prefix, Object value, Set<String> paths) {
        if (value == null) {
            return;
        }
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                String path = prefix.isBlank() ? entry.getKey().toString() : prefix + "/" + entry.getKey();
                paths.add(path);
                collectRequestPaths(path, entry.getValue(), paths);
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                collectRequestPaths(prefix, item, paths);
            }
        }
    }
}

package com.example.dynamicform.platform.metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClasspathStaticMetadataResolver implements StaticMetadataResolver {

    private final ObjectMapper objectMapper;
    private final Map<String, Map<String, String>> labelCache = new ConcurrentHashMap<>();

    public ClasspathStaticMetadataResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String resolveLabel(String metadataSource, String referenceModel) {
        if (referenceModel == null || referenceModel.isBlank()) {
            return "";
        }
        return getLabels(metadataSource).getOrDefault(referenceModel, referenceModel);
    }

    @Override
    public Map<String, String> getLabels(String metadataSource) {
        if (metadataSource == null || metadataSource.isBlank()) {
            return Collections.emptyMap();
        }
        return labelCache.computeIfAbsent(metadataSource, this::loadLabels);
    }

    private Map<String, String> loadLabels(String metadataSource) {
        try (InputStream is = new ClassPathResource(metadataSource).getInputStream()) {
            List<Map<String, Object>> items = objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
            Map<String, String> labels = new HashMap<>();
            for (Map<String, Object> item : items) {
                Object referenceModel = item.get("referenceModel");
                Object label = item.get("label");
                if (referenceModel != null && label != null) {
                    labels.put(referenceModel.toString(), label.toString());
                }
            }
            return labels;
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }
}

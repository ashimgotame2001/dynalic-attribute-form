package com.example.dynamicform.platform.core.engine;

import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility to load raw metadata from JSON files in the classpath.
 */
@Component
public class MetadataLoader {

    @Autowired
    private ObjectMapper objectMapper;

    public RawFormMetadata loadMetadata(String path) throws IOException {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(is, RawFormMetadata.class);
        }
    }
}

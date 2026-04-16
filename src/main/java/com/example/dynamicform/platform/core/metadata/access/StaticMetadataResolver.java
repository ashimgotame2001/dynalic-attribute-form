package com.example.dynamicform.platform.core.metadata.access;

import java.util.Map;

public interface StaticMetadataResolver {

    String resolveLabel(String metadataSource, String referenceModel);

    Map<String, String> getLabels(String metadataSource);
}

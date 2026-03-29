package com.example.dynamicform.platform.metadata;

import java.util.Map;

public interface StaticMetadataResolver {

    String resolveLabel(String metadataSource, String referenceModel);

    Map<String, String> getLabels(String metadataSource);
}

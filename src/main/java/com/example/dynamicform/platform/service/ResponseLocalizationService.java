package com.example.dynamicform.platform.service;

import com.example.dynamicform.platform.api.dto.FieldDefinition;
import com.example.dynamicform.platform.api.dto.FormDefinition;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;

import java.util.List;

public interface ResponseLocalizationService {

    String resolveLanguage(String languageHeader, String acceptLanguageHeader);

    RawFormMetadata localizeRawMetadata(RawFormMetadata metadata, Long languageId);

    RawFormMetadata prepareRawMetadataResponse(RawFormMetadata metadata, Long languageId);

    FormDefinition localizeFormDefinition(FormDefinition formDefinition, Long languageId);

    FormDefinition prepareFormDefinitionResponse(FormDefinition formDefinition, Long languageId);
}

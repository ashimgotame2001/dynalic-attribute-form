package com.example.dynamicform.platform.core.engine;

import com.example.dynamicform.platform.api.dto.FormDefinition;
import com.example.dynamicform.platform.api.dto.metadata.RawFormMetadata;

/**
 * Service interface for interpreting raw metadata JSON into internal FormDefinition.
 */
public interface MetadataInterpreter {

    /**
     * Interprets raw form metadata and produces an internal FormDefinition.
     *
     * @param rawMetadata the raw metadata deserialized from JSON
     * @param formName the name of the form (used as identifier)
     * @return FormDefinition object ready for use by the form engine
     */
    FormDefinition interpret(RawFormMetadata rawMetadata, String formName);

    /**
     * Interprets raw form metadata and produces an internal FormDefinition with version.
     *
     * @param rawMetadata the raw metadata
     * @param formName form name
     * @param version version number
     * @return FormDefinition
     */
    FormDefinition interpret(RawFormMetadata rawMetadata, String formName, int version);
}

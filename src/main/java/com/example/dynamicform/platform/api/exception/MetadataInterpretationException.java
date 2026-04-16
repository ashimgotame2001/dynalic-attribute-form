package com.example.dynamicform.platform.api.exception;

import lombok.Getter;

/**
 * Exception thrown when metadata interpretation fails.
 */
public class MetadataInterpretationException extends RuntimeException {
    @Getter
    private final String errorCode;

    public MetadataInterpretationException(String message) {
        super(message);
        this.errorCode = "METADATA_INTERPRETATION_ERROR";
    }

    public MetadataInterpretationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "METADATA_INTERPRETATION_ERROR";
    }

    public MetadataInterpretationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

package com.example.dynamicform.platform.core.validation;

import java.util.Map;

public interface DomainIntegrityValidationService {

    DomainValidationResult validateDomainIntegrity(String operation, String entityType,
                                                   String entityId, Map<String, Object> data);

    class DomainValidationResult {
        private boolean valid = true;
        private java.util.List<ValidationError> errors = new java.util.ArrayList<>();
        private java.util.Map<String, Object> metadata = new java.util.HashMap<>();

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public java.util.List<ValidationError> getErrors() { return errors; }
        public void addError(ValidationError error) {
            this.errors.add(error);
            this.valid = false;
        }
        public void addErrors(java.util.List<ValidationError> errors) {
            this.errors.addAll(errors);
            if (!errors.isEmpty()) {
                this.valid = false;
            }
        }

        public java.util.Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(java.util.Map<String, Object> metadata) { this.metadata = metadata; }
    }
}

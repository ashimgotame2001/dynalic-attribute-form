package com.example.dynamicform.platform.core.validation;

import com.example.dynamicform.platform.api.dto.FieldDefinition;
import com.example.dynamicform.platform.api.dto.FormDefinition;
import com.example.dynamicform.platform.api.dto.ValidationDefinition;
import com.example.dynamicform.platform.api.dto.ValidationError;
import com.example.dynamicform.platform.api.dto.ValidationType;
import com.example.dynamicform.platform.api.exception.DynamicValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Implementation of the ValidationEngine that performs runtime validation against metadata rules.
 */
@Component
public class ValidationEngineImpl implements ValidationEngine {

    private static final Logger logger = LoggerFactory.getLogger(ValidationEngineImpl.class);

    @Override
    public List<ValidationError> validate(FormDefinition formDefinition, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();
        if (formDefinition == null || formDefinition.getFields() == null) {
            return errors;
        }

        for (FieldDefinition field : formDefinition.getFields()) {
            validateFieldRecursive("", data, field, errors);
        }

        return errors;
    }

    private void validateFieldRecursive(String parentPath, Map<String, Object> data, FieldDefinition field, List<ValidationError> errors) {
        String fieldName = field.getFieldName();
        String currentPath = parentPath.isEmpty() ? fieldName : parentPath + "." + fieldName;

        Object value = data != null ? data.get(fieldName) : null;

        if (field.getValidations() != null && !field.getValidations().isEmpty()) {
            List<ValidationError> fieldErrors = validateField(fieldName, value, field.getValidations());
            // Prepend path to errors
            for (ValidationError ve : fieldErrors) {
                ve.setFieldPath(currentPath);
                errors.add(ve);
            }
        }

        if (value != null && field.isNestedObject()) {
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                if (field.getNestedFields() != null) {
                    for (FieldDefinition nestedField : field.getNestedFields()) {
                        validateFieldRecursive(currentPath, nestedMap, nestedField, errors);
                    }
                }
            } else {
                errors.add(ValidationError.builder()
                        .fieldPath(currentPath)
                        .message("Expected nested object but got: " + value.getClass().getSimpleName())
                        .validationType("type")
                        .build());
            }
        }

        if (value != null && field.isCollection()) {
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                if (field.isNestedObject() && field.getNestedFields() != null) {
                    int index = 0;
                    for (Object element : list) {
                        if (element instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> elementMap = (Map<String, Object>) element;
                            String elementPath = currentPath + "[" + index + "]";
                            for (FieldDefinition nestedField : field.getNestedFields()) {
                                validateFieldRecursive(elementPath, elementMap, nestedField, errors);
                            }
                        } else {
                            errors.add(ValidationError.builder()
                                    .fieldPath(currentPath + "[" + index + "]")
                                    .message("Expected nested object in list but got: " + (element != null ? element.getClass().getSimpleName() : "null"))
                                    .validationType("type")
                                    .build());
                        }
                        index++;
                    }
                }
            } else {
                errors.add(ValidationError.builder()
                        .fieldPath(currentPath)
                        .message("Expected list but got: " + value.getClass().getSimpleName())
                        .validationType("type")
                        .build());
            }
        }
    }

    @Override
    public List<ValidationError> validateField(String fieldName, Object value, List<ValidationDefinition> validations) {
        List<ValidationError> errors = new ArrayList<>();
        if (validations == null) {
            return errors;
        }

        for (ValidationDefinition validation : validations) {
            try {
                applyValidation(fieldName, value, validation);
            } catch (DynamicValidationException e) {
                errors.add(ValidationError.builder()
                        .fieldPath(fieldName)
                        .message(e.getMessage())
                        .validationType(validation.getType().name())
                        .invalidValue(value)
                        .build());
            } catch (Exception e) {
                logger.error("Unexpected validation error for field {}: {}", fieldName, e.getMessage(), e);
                errors.add(ValidationError.builder()
                        .fieldPath(fieldName)
                        .message("Validation error: " + e.getMessage())
                        .validationType(validation.getType().name())
                        .build());
            }
        }
        return errors;
    }

    private void applyValidation(String fieldName, Object value, ValidationDefinition validation) {
        ValidationType type = validation.getType();
        Map<String, Object> params = validation.getParameters();

        switch (type) {
            case REQUIRED -> {
                if (value == null) {
                    throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : "Field is required", fieldName, "required");
                }
                if (value instanceof String str && str.trim().isEmpty()) {
                    throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : "Field is required", fieldName, "required");
                }
                if (value instanceof List<?> list && list.isEmpty()) {
                    throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : "Field must contain at least one item", fieldName, "required");
                }
            }
            case REGEX -> {
                if (!(value instanceof String str)) {
                    throw new DynamicValidationException("Value must be a string for regex validation", fieldName, "regex");
                }
                String patternStr = getParamAsString(params, "pattern", "regex");
                try {
                    Pattern pattern = Pattern.compile(patternStr);
                    if (!pattern.matcher(str).matches()) {
                        String msg = validation.getMessage() != null ? validation.getMessage() : "Value does not match required pattern";
                        throw new DynamicValidationException(msg, fieldName, "regex");
                    }
                } catch (PatternSyntaxException e) {
                    throw new DynamicValidationException("Invalid regex pattern: " + e.getDescription(), fieldName, "regex");
                }
            }
            case MIN -> {
                // For numbers: min value; for strings: min length; for dates: min date
                if (value instanceof Number num) {
                    double min = getParamAsDouble(params, "value", 0);
                    if (num.doubleValue() < min) {
                        throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : 
                                "Value must be at least " + min, fieldName, "min");
                    }
                } else if (value instanceof String str) {
                    int min = getParamAsInt(params, "value", 0);
                    if (str.length() < min) {
                        throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : 
                                "Length must be at least " + min, fieldName, "min");
                    }
                } else if (value instanceof LocalDate date) {
                    LocalDate minDate = parseDateParam(params, "value");
                    if (date.isBefore(minDate)) {
                        throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : 
                                "Date must be on or after " + minDate, fieldName, "min");
                    }
                }
            }
            case MAX -> {
                if (value instanceof Number num) {
                    double max = getParamAsDouble(params, "value", 0);
                    if (num.doubleValue() > max) {
                        throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : 
                                "Value must be at most " + max, fieldName, "max");
                    }
                } else if (value instanceof String str) {
                    int max = getParamAsInt(params, "value", Integer.MAX_VALUE);
                    if (str.length() > max) {
                        throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : 
                                "Length must be at most " + max, fieldName, "max");
                    }
                } else if (value instanceof LocalDate date) {
                    LocalDate maxDate = parseDateParam(params, "value");
                    if (date.isAfter(maxDate)) {
                        throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : 
                                "Date must be on or before " + maxDate, fieldName, "max");
                    }
                }
            }
            case DATE_FORMAT -> {
                if (!(value instanceof String str)) {
                    throw new DynamicValidationException("Value must be a string for date format validation", fieldName, "dateFormat");
                }
                String pattern = getParamAsString(params, "pattern", "yyyy-MM-dd");
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                    LocalDate.parse(str, formatter);
                } catch (DateTimeParseException e) {
                    throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : 
                            "Date must be in format " + pattern, fieldName, "dateFormat");
                }
            }
            case ALLOW_FUTURE -> {
                if (value instanceof LocalDate date) {
                    if (date.isAfter(LocalDate.now())) {
                        throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : 
                                "Future dates are not allowed", fieldName, "allowFuture");
                    }
                } else if (value instanceof LocalDateTime dateTime) {
                    if (dateTime.isAfter(LocalDateTime.now())) {
                        throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : 
                                "Future dates are not allowed", fieldName, "allowFuture");
                    }
                }
            }
            case ALLOW_PAST -> {
                if (value instanceof LocalDate date) {
                    if (date.isBefore(LocalDate.now())) {
                        throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : 
                                "Past dates are not allowed", fieldName, "allowPast");
                    }
                } else if (value instanceof LocalDateTime dateTime) {
                    if (dateTime.isBefore(LocalDateTime.now())) {
                        throw new DynamicValidationException(validation.getMessage() != null ? validation.getMessage() : 
                                "Past dates are not allowed", fieldName, "allowPast");
                    }
                }
            }
        }
    }

    private String getParamAsString(Map<String, Object> params, String key, String defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        Object val = params.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    private int getParamAsInt(Map<String, Object> params, String key, int defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        Object val = params.get(key);
        if (val instanceof Number num) {
            return num.intValue();
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double getParamAsDouble(Map<String, Object> params, String key, double defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        Object val = params.get(key);
        if (val instanceof Number num) {
            return num.doubleValue();
        }
        try {
            return Double.parseDouble(val.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private LocalDate parseDateParam(Map<String, Object> params, String key) {
        String dateStr = getParamAsString(params, key, null);
        if (dateStr == null) {
            throw new DynamicValidationException("Missing date parameter for validation", "date");
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new DynamicValidationException("Invalid date format, use ISO (yyyy-MM-dd)", "date");
        }
    }
}

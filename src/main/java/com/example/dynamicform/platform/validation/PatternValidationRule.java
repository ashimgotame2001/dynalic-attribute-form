package com.example.dynamicform.platform.validation;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Built-in validation rule for pattern constraints.
 */
public class PatternValidationRule implements ValidationRule {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+]?[0-9\\s\\-\\(\\)]{7,20}$");

    private static final Pattern ALPHA_ONLY_PATTERN =
        Pattern.compile("^[A-Za-z\\s]+$");

    private static final Pattern ALPHA_NUMERIC_PATTERN =
        Pattern.compile("^[A-Za-z0-9]+$");

    @Override
    public String getName() {
        return "Pattern";
    }

    @Override
    public List<String> getApplicableEntityTypes() {
        return Collections.singletonList("*");
    }

    @Override
    public List<ValidationError> validate(String entityType, String entityId, Map<String, Object> data) {
        List<ValidationError> errors = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            if (!(value instanceof String)) continue;

            String stringValue = (String) value;

            // Field-specific pattern validations
            switch (fieldName.toLowerCase()) {
                case "email":
                    if (!EMAIL_PATTERN.matcher(stringValue).matches()) {
                        errors.add(new ValidationError(fieldName, "INVALID_PATTERN",
                            "Invalid email format", "pattern"));
                    }
                    break;

                case "phone":
                case "phonenumber":
                case "mobile":
                    if (!PHONE_PATTERN.matcher(stringValue).matches()) {
                        errors.add(new ValidationError(fieldName, "INVALID_PATTERN",
                            "Invalid phone number format", "pattern"));
                    }
                    break;

                case "firstname":
                case "lastname":
                case "middlename":
                    if (!ALPHA_ONLY_PATTERN.matcher(stringValue).matches()) {
                        errors.add(new ValidationError(fieldName, "INVALID_PATTERN",
                            "Name can only contain letters and spaces", "pattern"));
                    }
                    break;

                case "username":
                case "loginid":
                    if (!ALPHA_NUMERIC_PATTERN.matcher(stringValue).matches()) {
                        errors.add(new ValidationError(fieldName, "INVALID_PATTERN",
                            "Username can only contain letters and numbers", "pattern"));
                    }
                    break;

                case "postalcode":
                case "zipcode":
                    if (!stringValue.matches("^[A-Za-z0-9\\s-]{3,10}$")) {
                        errors.add(new ValidationError(fieldName, "INVALID_PATTERN",
                            "Invalid postal code format", "pattern"));
                    }
                    break;
            }
        }

        return errors;
    }

    @Override
    public int getPriority() {
        return 50;
    }
}

package com.example.dynamicform.platform.core.validation.rule;

import com.example.dynamicform.platform.core.validation.ValidationError;

import java.util.*;

/**
 * Built-in validation rule for range constraints.
 */
public class RangeValidationRule implements ValidationRule {

    @Override
    public String getName() {
        return "Range";
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

            if (!(value instanceof Number)) continue;

            double numValue = ((Number) value).doubleValue();

            // Field-specific range validations
            switch (fieldName.toLowerCase()) {
                case "age":
                    if (numValue < 0 || numValue > 150) {
                        errors.add(new ValidationError(fieldName, "OUT_OF_RANGE",
                            "Age must be between 0 and 150", "range"));
                    }
                    break;

                case "amount":
                case "balance":
                case "price":
                    if (numValue < -999999999.99 || numValue > 999999999.99) {
                        errors.add(new ValidationError(fieldName, "OUT_OF_RANGE",
                            "Amount must be between -999,999,999.99 and 999,999,999.99", "range"));
                    }
                    break;

                case "percentage":
                case "rate":
                    if (numValue < 0 || numValue > 100) {
                        errors.add(new ValidationError(fieldName, "OUT_OF_RANGE",
                            "Percentage must be between 0 and 100", "range"));
                    }
                    break;

                case "quantity":
                case "count":
                    if (numValue < 0 || numValue > 999999) {
                        errors.add(new ValidationError(fieldName, "OUT_OF_RANGE",
                            "Quantity must be between 0 and 999,999", "range"));
                    }
                    break;
            }
        }

        return errors;
    }

    @Override
    public int getPriority() {
        return 40;
    }
}

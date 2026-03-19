package com.example.dynamicform.interpreter;

import com.example.dynamicform.dto.SimplifiedFormField;
import com.example.dynamicform.dto.SimplifiedFormField.ValidationParam;
import com.example.dynamicform.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.dto.metadata.RawDomainModel;
import com.example.dynamicform.dto.metadata.RawFormMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Transforms simplified client format into RawFormMetadata.
 */
@Component
public class SimplifiedMetadataTransformer {

    /**
     * Transforms a list of simplified fields into RawFormMetadata.
     *
     * @param fields the simplified fields from request
     * @param formName the form name
     * @param targetDtoClassName the target DTO class name
     * @return RawFormMetadata ready for storage/interpretation
     */
    public RawFormMetadata transform(List<SimplifiedFormField> fields, String formName, String targetDtoClassName) {
        if (fields == null) {
            fields = new ArrayList<>();
        }

        List<RawDomainAttribute> domainModel = new ArrayList<>();
        for (SimplifiedFormField field : fields) {
            RawDomainAttribute attribute = transformField(field);
            if (attribute != null) {
                domainModel.add(attribute);
            }
        }

        return RawFormMetadata.builder()
                .modelName(deriveRootModelName(formName, targetDtoClassName))
                .domainModel(RawDomainModel.builder().attributes(domainModel).build())
                .build();
    }

    private RawDomainAttribute transformField(SimplifiedFormField field) {
        if (field == null || field.getModelName() == null) {
            return null;
        }

        String attributeName = toLowerCamelCase(field.getModelName());

        Boolean isVisible = field.getVisible() != null ? field.getVisible() : true;
        Boolean isCollection = field.getIsCollection() != null ? field.getIsCollection() : false;
        Boolean isReference = field.getNestedFields() != null && !field.getNestedFields().isEmpty();

        RawDomainAttribute.RawDomainAttributeBuilder builder = RawDomainAttribute.builder()
                .modelName(field.getModelName())
                .attributeName(attributeName)
                .reference(isReference)
                .collection(isCollection);

        if (!isReference) {
            builder.visible(isVisible);
            builder.shortLabel(field.getShortLabel() != null ? field.getShortLabel() : field.getModelName());
            builder.longLabel(field.getLongLabel() != null ? field.getLongLabel() : "Enter " + field.getModelName());
        }

        // Transform validations
        if (field.getValidations() != null) {
            List<Map<String, Object>> rawValidations = new ArrayList<>();
            for (Map<String, ValidationParam> validationMap : field.getValidations()) {
                for (Map.Entry<String, ValidationParam> entry : validationMap.entrySet()) {
                    String validationType = entry.getKey();
                    ValidationParam paramObj = entry.getValue();

                    java.util.Map<String, Object> params = new java.util.HashMap<>();
                    if (paramObj.getValue() != null) {
                        params.put("value", paramObj.getValue());
                    }
                    if (paramObj.getPattern() != null) {
                        params.put("pattern", paramObj.getPattern());
                    }
                    if (paramObj.getMessage() != null) {
                        params.put("message", paramObj.getMessage());
                    }

                    java.util.Map<String, Object> validationMapInner = new java.util.HashMap<>();
                    validationMapInner.put(validationType, params);
                    rawValidations.add(validationMapInner);
                }
            }
            builder.validations(rawValidations);
        }

        // Recursively transform nested fields
        if (field.getNestedFields() != null) {
            List<RawDomainAttribute> nested = new ArrayList<>();
            for (SimplifiedFormField nestedField : field.getNestedFields()) {
                RawDomainAttribute nestedAttr = transformField(nestedField);
                if (nestedAttr != null) {
                    nested.add(nestedAttr);
                }
            }
            builder.domainModel(RawDomainModel.builder().attributes(nested).build());
        }

        return builder.build();
    }

    private Object convertParams(Object params) {
        if (params instanceof Map) {
            return params;
        }
        return params;
    }

    private String extractMessage(Object params) {
        if (params instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) params;
            Object message = map.get("message");
            return message != null ? message.toString() : null;
        }
        return null;
    }

    private String toLowerCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String lower = input.substring(0, 1).toLowerCase() + input.substring(1);
        // Handle cases like "alphaTwoCode" staying as is
        return lower;
    }

    private String deriveRootModelName(String formName, String targetDtoClassName) {
        // Use targetDtoClassName if provided, else derive from formName
        if (targetDtoClassName != null && !targetDtoClassName.isEmpty()) {
            // Extract simple class name from fully qualified name
            int lastDot = targetDtoClassName.lastIndexOf('.');
            return lastDot > 0 ? targetDtoClassName.substring(lastDot + 1) : targetDtoClassName;
        }
        // Derive from formName: e.g., "customerRegistration" -> "CustomerRegistration"
        if (formName != null && !formName.isEmpty()) {
            return Character.toUpperCase(formName.charAt(0)) + formName.substring(1);
        }
        return "FormDefinition";
    }
}

package com.example.dynamicform.interpreter;

import com.example.dynamicform.dto.FieldSpecRequest;
import com.example.dynamicform.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.dto.metadata.RawDomainModel;
import com.example.dynamicform.dto.metadata.RawFormMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


@Component
public class DtoIntrospector {


    public RawFormMetadata buildMetadata(List<FieldSpecRequest.FieldSpec> fieldSpecs, String rootModelName, Class<?> targetClass, Set<String> enabledReferenceModels, Map<String, Object> requestAttributes) {
        if (fieldSpecs == null || fieldSpecs.isEmpty()) {
            return RawFormMetadata.builder()
                    .modelName(rootModelName)
                    .domainModel(RawDomainModel.builder().attributes(new ArrayList<>()).build())
                    .build();
        }

        Map<String, RawDomainAttribute> topLevelFields = new LinkedHashMap<>();
        Set<String> processedReferenceModels = new HashSet<>();

        for (FieldSpecRequest.FieldSpec spec : fieldSpecs) {
            if (spec.getReferenceModel() == null || (enabledReferenceModels != null && !enabledReferenceModels.contains(spec.getReferenceModel()))) {
                continue;
            }
            processedReferenceModels.add(spec.getReferenceModel());

            String[] segments = spec.getReferenceModel().split("/");
            if (segments.length == 0) {
                continue;
            }

            String topLevelName = segments[0];

            try {
                Field topField = targetClass.getDeclaredField(topLevelName);
                Class<?> topFieldType = topField.getType();
                boolean isCollection = List.class.isAssignableFrom(topFieldType);
                Class<?> elementClass = isCollection ? getCollectionElementType(topField) : topFieldType;

                boolean isReference = !isCollection && isComplexType(topFieldType);
                boolean visible = (enabledReferenceModels == null) ? spec.getVisible() : (requestAttributes != null && requestAttributes.containsKey(topLevelName));

                RawDomainAttribute.RawDomainAttributeBuilder builder = RawDomainAttribute.builder()
                        .attributeType(elementClass != null ? elementClass.getSimpleName() : topFieldType.getSimpleName())
                        .attributeName(topLevelName)
                        .referenceModel(buildReferenceModel(rootModelName, topLevelName, isReference, elementClass != null ? elementClass : topFieldType))
                        .reference(isReference)
                        .collection(isCollection);

                if (!isReference) {
                    builder.visible(visible);
                }

                RawDomainAttribute topLevelAttr = topLevelFields.computeIfAbsent(topLevelName, k -> builder.build());

                String parentRefModel = elementClass != null ? elementClass.getSimpleName() : topFieldType.getSimpleName();

                if (segments.length > 1) {
                    buildNestedFields(topLevelAttr, Arrays.copyOfRange(segments, 1, segments.length), 0, topFieldType, spec, null, null, parentRefModel);
                } else {
                    if (hasValidations(spec.getValidations())) {
                        List<Map<String, Object>> rawValidations = convertValidations(spec.getValidations());
                        topLevelAttr.setValidations(rawValidations);
                    }
                }
            } catch (NoSuchFieldException e) {
                continue;
            }
        }

        // Add default attributes for enabled reference models not specified in request
        if (enabledReferenceModels != null) {
            for (String enabled : enabledReferenceModels) {
                if (!processedReferenceModels.contains(enabled)) {
                    addDefaultAttributeForReferenceModel(enabled, topLevelFields, targetClass, rootModelName);
                }
            }
        }

        return RawFormMetadata.builder()
                .moduleName("Customer Management")
                .artifactName("CustomerRegister")
                .version("1.0.0")
                .modelName(rootModelName)
                .domainModel(RawDomainModel.builder().attributes(new ArrayList<>(topLevelFields.values())).build())
                .build();
    }

    private void buildNestedFields(RawDomainAttribute parent, String[] segments, int index, Class<?> currentClass, FieldSpecRequest.FieldSpec leafSpec, Set<String> enabledReferenceModels, Map<String, Object> requestAttributes, String parentReferenceModel) {
        if (index >= segments.length) {
            return;
        }

        String segment = segments[index];
        boolean isLast = index == segments.length - 1;

        if (parent.getDomainModel() == null) {
            parent.setDomainModel(RawDomainModel.builder().attributes(new ArrayList<>()).build());
        }

        RawDomainAttribute child = findChildByName(parent, segment);
        if (child == null) {
            try {
                Field field = currentClass.getDeclaredField(segment);
                Class<?> fieldType = field.getType();
                boolean isCollection = List.class.isAssignableFrom(fieldType);
                Class<?> elementClass = isCollection ? getCollectionElementType(field) : fieldType;
                boolean isReference = !isCollection && isComplexType(fieldType);
                boolean visible = leafSpec.getVisible() != null ? leafSpec.getVisible() : ((enabledReferenceModels == null) ? true : (requestAttributes != null && requestAttributes.containsKey(parent.getAttributeName())));

                RawDomainAttribute.RawDomainAttributeBuilder builder = RawDomainAttribute.builder()
                        .attributeType(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                        .attributeName(field.getName())
                        .reference(isReference)
                        .collection(isCollection);

                if (leafSpec.getShortLabel() != null) {
                    builder.shortLabel(leafSpec.getShortLabel());
                }
                if (leafSpec.getLongLabel() != null) {
                    builder.longLabel(leafSpec.getLongLabel());
                }

                if (!isReference) {
                    builder.visible(visible);
                }

                if (!isReference) {
                    builder.shortLabel(leafSpec.getShortLabel() != null ? leafSpec.getShortLabel() : capitalize(field.getName()));
                    builder.longLabel(leafSpec.getLongLabel() != null ? leafSpec.getLongLabel() : "Enter " + field.getName());
                } else {
                    builder.modelName(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                            .association(true)
                            .referenceModel(parentReferenceModel + "/" + (elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName()));
                }

                child = builder.build();
                parent.getDomainModel().getAttributes().add(child);
            } catch (NoSuchFieldException e) {
                return;
            }
        }

        if (isLast && child != null && hasValidations(leafSpec.getValidations())) {
            List<Map<String, Object>> rawValidations = convertValidations(leafSpec.getValidations());
            child.setValidations(rawValidations);
        }

        if (!isLast && child != null) {
            Class<?> childClass = getFieldClass(currentClass, child.getAttributeName());
            if (childClass != null) {
                String childReferenceModel = child.getReference() ? child.getReferenceModel() : parentReferenceModel;
                buildNestedFields(child, segments, index + 1, childClass, leafSpec, enabledReferenceModels, requestAttributes, childReferenceModel);
            }
        }
    }

    private RawDomainAttribute buildAttributeFromField(Field field, Boolean visible, String shortLabel, String longLabel) {
        Class<?> fieldType = field.getType();
        boolean isCollection = List.class.isAssignableFrom(fieldType);
        Class<?> elementClass = isCollection ? getCollectionElementType(field) : fieldType;
        boolean isReference = !isCollection && isComplexType(fieldType);

        RawDomainAttribute.RawDomainAttributeBuilder builder = RawDomainAttribute.builder()
                .attributeType(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                .attributeName(field.getName())
                .reference(isReference)
                .collection(isCollection);

        if (!isReference) {
            builder.visible(visible != null ? visible : true);
            if (shortLabel != null) {
                builder.shortLabel(shortLabel);
            }
            if (longLabel != null) {
                builder.longLabel(longLabel);
            }
        } else {
            builder.modelName(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                    .association(true)
                    .domainModel(RawDomainModel.builder().attributes(new ArrayList<>()).build());
        }

        return builder.build();
    }

    private boolean hasValidations(Object validations) {
        if (validations == null) return false;
        if (validations instanceof List<?> list) return !list.isEmpty();
        if (validations instanceof Map<?, ?> map) return !map.isEmpty();
        return false;
    }

    private List<Map<String, Object>> convertValidations(Object validations) {
        if (validations == null) return new ArrayList<>();

        if (validations instanceof String) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                validations = mapper.readValue((String) validations, Object.class);
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        List<Map<String, Object>> validationMaps = new ArrayList<>();

        if (validations instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map) {
                    validationMaps.add((Map<String, Object>) item);
                }
            }
        } else if (validations instanceof Map) {
            validationMaps.add((Map<String, Object>) validations);
        }

        // Combine all validation rules into a single map
        Map<String, Object> combinedValidations = new HashMap<>();
        for (Map<String, Object> map : validationMaps) {
            FieldSpecRequest.ValidationRule rule = parseValidationRule(map);
            if (rule != null) {
                Map<String, Object> params = new HashMap<>();
                if (rule.getValue() != null) {
                    params.put("value", rule.getValue());
                }
                if (rule.getPattern() != null) {
                    params.put("pattern", rule.getPattern());
                }
                if (rule.getMessage() != null) {
                    params.put("message", rule.getMessage());
                }
                combinedValidations.put(rule.getType(), params);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        if (!combinedValidations.isEmpty()) {
            result.add(combinedValidations);
        }
        return result;
    }

    private FieldSpecRequest.ValidationRule parseValidationRule(Map<String, Object> map) {
        FieldSpecRequest.ValidationRule rule = null;

        if (map.containsKey("type")) {
            // New format: { "type": "required", "value": true, "message": "..." }
            rule = FieldSpecRequest.ValidationRule.builder()
                    .type((String) map.get("type"))
                    .value((Boolean) map.get("value"))
                    .pattern((String) map.get("pattern"))
                    .message((String) map.get("message"))
                    .build();
        } else {
            // Old format: { "required": { "value": true, "message": "..." } }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String type = entry.getKey();
                @SuppressWarnings("unchecked")
                Map<String, Object> paramMap = (Map<String, Object>) entry.getValue();
                rule = FieldSpecRequest.ValidationRule.builder()
                        .type(type)
                        .value((Boolean) paramMap.get("value"))
                        .pattern((String) paramMap.get("pattern"))
                        .message((String) paramMap.get("message"))
                        .build();
                break;
            }
        }
        return rule;
    }

    private Class<?> getFieldClass(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return field.getType();
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private RawDomainAttribute findChildByName(RawDomainAttribute parent, String name) {
        if (parent.getDomainModel() == null || parent.getDomainModel().getAttributes() == null) {
            return null;
        }
        return parent.getDomainModel().getAttributes().stream()
                .filter(attr -> name.equals(attr.getAttributeName()))
                .findFirst()
                .orElse(null);
    }

    private Class<?> getCollectionElementType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type[] typeArgs = pt.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                return (Class<?>) typeArgs[0];
            }
        }
        return null;
    }

    private boolean isComplexType(Class<?> clazz) {
        // Simple types: primitives, wrappers, String, Enum, Date/time types
        return !(clazz.isPrimitive()
                || clazz.getName().startsWith("java.lang.")
                || Number.class.isAssignableFrom(clazz)
                || Boolean.class.equals(clazz)
                || Character.class.equals(clazz)
                || clazz.isEnum()
                || clazz.equals(java.util.Date.class)
                || clazz.equals(java.time.LocalDate.class)
                || clazz.equals(java.time.LocalDateTime.class)
                || clazz.equals(java.time.ZonedDateTime.class));
    }

    private void addDefaultAttributeForReferenceModel(String referenceModel, Map<String, RawDomainAttribute> topLevelFields, Class<?> rootClass, String rootModelName) {
        String[] segments = referenceModel.split("/");
        String topLevelName = segments[0];
        RawDomainAttribute topAttr = topLevelFields.get(topLevelName);
        if (topAttr == null) {
            topAttr = buildDefaultAttributeTree(segments, 0, rootClass, rootModelName);
            if (topAttr != null) {
                topLevelFields.put(topLevelName, topAttr);
            }
        } else {
            addDefaultNestedAttribute(topAttr, segments, 1, getFieldClass(rootClass, topLevelName));
        }
    }

    private RawDomainAttribute buildDefaultAttributeTree(String[] segments, int index, Class<?> currentClass, String currentRefModel) {
        if (index >= segments.length) return null;
        String segment = segments[index];
        try {
            Field field = currentClass.getDeclaredField(segment);
            Class<?> fieldType = field.getType();
            boolean isCollection = List.class.isAssignableFrom(fieldType);
            Class<?> elementClass = isCollection ? getCollectionElementType(field) : fieldType;
            boolean isReference = !isCollection && isComplexType(fieldType);

            RawDomainAttribute.RawDomainAttributeBuilder builder = RawDomainAttribute.builder()
                    .attributeName(field.getName())
                    .attributeType(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                    .reference(isReference)
                    .collection(isCollection);

            if (!isReference) {
                builder.visible(false);
            } else {
                String refModel = currentRefModel + "/" + (elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName());
                builder.modelName(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                        .association(true)
                        .referenceModel(refModel)
                        .domainModel(RawDomainModel.builder().attributes(new ArrayList<>()).build());
                RawDomainAttribute child = buildDefaultAttributeTree(segments, index + 1, elementClass != null ? elementClass : fieldType, refModel);
                if (child != null) {
                    builder.build().getDomainModel().getAttributes().add(child);
                }
            }
            return builder.build();
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private void addDefaultNestedAttribute(RawDomainAttribute parent, String[] segments, int index, Class<?> currentClass) {
        if (index >= segments.length || currentClass == null) return;
        String segment = segments[index];
        RawDomainAttribute child = parent.getDomainModel().getAttributes().stream()
                .filter(a -> segment.equals(a.getAttributeName()))
                .findFirst()
                .orElse(null);
        if (child == null) {
            try {
                Field field = currentClass.getDeclaredField(segment);
                Class<?> fieldType = field.getType();
                boolean isCollection = List.class.isAssignableFrom(fieldType);
                Class<?> elementClass = isCollection ? getCollectionElementType(field) : fieldType;
                boolean isReference = !isCollection && isComplexType(fieldType);

                RawDomainAttribute.RawDomainAttributeBuilder builder = RawDomainAttribute.builder()
                        .attributeName(field.getName())
                        .attributeType(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                        .reference(isReference)
                        .collection(isCollection);

                if (!isReference) {
                    builder.visible(false)
                            .shortLabel(capitalize(field.getName()))
                            .longLabel("Enter " + field.getName());
                } else {
                    String refModel = parent.getReferenceModel() + "/" + (elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName());
                    builder.modelName(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                            .association(true)
                            .referenceModel(refModel)
                            .domainModel(RawDomainModel.builder().attributes(new ArrayList<>()).build());
                }
                child = builder.build();
                parent.getDomainModel().getAttributes().add(child);
                if (isReference) {
                    addDefaultNestedAttribute(child, segments, index + 1, elementClass != null ? elementClass : fieldType);
                }
            } catch (NoSuchFieldException e) {
                // ignore
            }
        } else if (child.getReference()) {
            addDefaultNestedAttribute(child, segments, index + 1, getFieldClass(currentClass, child.getAttributeName()));
        }
    }

    private String buildReferenceModel(String rootModelName, String attributeName, boolean isReference, Class<?> clazz) {
        if (isReference) {
            return clazz.getSimpleName();
        }
        return null; // primitives don't have referenceModel
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(str.charAt(0)));
        for (int i = 1; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                sb.append(' ');
            }
            sb.append(Character.toLowerCase(str.charAt(i)));
        }
        return sb.toString();
    }

    private RawDomainModel buildNestedForUnspecified(Class<?> clazz) {
        List<RawDomainAttribute> attributes = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            RawDomainAttribute attr = buildAttributeFromField(field, false, null, null);
            attributes.add(attr);
        }
        return RawDomainModel.builder().attributes(attributes).build();
    }
}

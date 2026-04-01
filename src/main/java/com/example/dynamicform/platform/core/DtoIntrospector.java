package com.example.dynamicform.platform.core;

import com.example.dynamicform.platform.dto.FieldSpecRequest;
import com.example.dynamicform.platform.dto.metadata.RawDomainAttribute;
import com.example.dynamicform.platform.dto.metadata.RawDomainModel;
import com.example.dynamicform.platform.dto.metadata.RawFormMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


@Component
public class DtoIntrospector {

    private static final ObjectMapper sharedMapper = new ObjectMapper();

    public RawFormMetadata buildMetadata(List<FieldSpecRequest.FieldSpec> fieldSpecs, String rootModelName, Class<?> targetClass, Set<String> enabledReferenceModels, Map<String, Object> requestAttributes) {
        if (fieldSpecs == null || fieldSpecs.isEmpty()) {
            return RawFormMetadata.builder()
                    .modelName(rootModelName)
                    .domainModel(buildNestedForUnspecified(targetClass, ""))
                    .build();
        }

        Map<String, RawDomainAttribute> topLevelFields = new LinkedHashMap<>();
        Set<String> processedReferenceModels = new HashSet<>();

        for (FieldSpecRequest.FieldSpec spec : fieldSpecs) {
            String refModel = spec.getReferenceModel();
            if (refModel == null) continue;

            // If filtering is enabled, skip models not in the allowed set
            if (enabledReferenceModels != null && !enabledReferenceModels.contains(refModel)) {
                continue;
            }
            processedReferenceModels.add(refModel);

        String[] segments = spec.getReferenceModel().split("/");
        if (segments.length == 0) {
            continue;
        }
        List<Map<String, Object>> normalizedValidations = normalizeValidations(spec.getValidations());

        String topLevelName = segments[0];

            try {
                Field topField = targetClass.getDeclaredField(topLevelName);
                Class<?> topFieldType = topField.getType();
                boolean isCollection = List.class.isAssignableFrom(topFieldType);
                Class<?> elementClass = isCollection ? getCollectionElementType(topField) : topFieldType;
                Class<?> effectiveClass = elementClass != null ? elementClass : topFieldType;
                boolean isReference = isComplexType(effectiveClass);
                boolean visible = (enabledReferenceModels == null) ? (spec.getVisible() != null ? spec.getVisible() : true) : (requestAttributes != null && requestAttributes.containsKey(topLevelName));
                String topLevelReferenceModel = topLevelName;

                RawDomainAttribute.RawDomainAttributeBuilder builder = RawDomainAttribute.builder()
                        .attributeType(elementClass != null ? elementClass.getSimpleName() : topFieldType.getSimpleName())
                        .modelName(effectiveClass.getSimpleName())
                        .attributeName(topLevelName)
                        .referenceModel(topLevelReferenceModel)
                        .reference(isReference)
                        .collection(isCollection)
                        .visible(visible);

                if (!isReference) {
                    builder.shortLabel(spec.getShortLabel() != null ? spec.getShortLabel() : "");
                    builder.shortLabelI18n(spec.getShortLabelI18n());
                    builder.longLabel(spec.getLongLabel() != null ? spec.getLongLabel() : "");
                    builder.longLabelI18n(spec.getLongLabelI18n());
                }

                RawDomainAttribute topLevelAttr = topLevelFields.computeIfAbsent(topLevelName, k -> builder.build());
                if (isReference && topLevelAttr.getDomainModel() == null) {
                    topLevelAttr.setDomainModel(buildNestedForUnspecified(effectiveClass, topLevelReferenceModel));
                }

                if (segments.length > 1) {
                    buildNestedFields(topLevelAttr, Arrays.copyOfRange(segments, 1, segments.length), 0, topFieldType, spec, normalizedValidations, null, null, topLevelReferenceModel);
                } else {
                    if (!normalizedValidations.isEmpty()) {
                        topLevelAttr.setValidations(normalizedValidations);
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

    private void buildNestedFields(RawDomainAttribute parent,
                                   String[] segments,
                                   int index,
                                   Class<?> currentClass,
                                   FieldSpecRequest.FieldSpec leafSpec,
                                   List<Map<String, Object>> normalizedValidations,
                                   Set<String> enabledReferenceModels,
                                   Map<String, Object> requestAttributes,
                                   String parentReferenceModel) {
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
                Class<?> effectiveClass = elementClass != null ? elementClass : fieldType;
                boolean isReference = isComplexType(effectiveClass);
                boolean visible = leafSpec.getVisible() != null ? leafSpec.getVisible() : ((enabledReferenceModels == null) ? true : (requestAttributes != null && requestAttributes.containsKey(parent.getAttributeName())));

                RawDomainAttribute.RawDomainAttributeBuilder builder = RawDomainAttribute.builder()
                        .attributeType(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                        .modelName(effectiveClass.getSimpleName())
                        .attributeName(field.getName())
                        .referenceModel(parentReferenceModel + "/" + field.getName())
                        .reference(isReference)
                        .collection(isCollection)
                        .visible(visible);

                if (!isReference) {
                    if (leafSpec.getShortLabel() != null) {
                        builder.shortLabel(leafSpec.getShortLabel());
                    } else {
                        builder.shortLabel("");
                    }
                    builder.shortLabelI18n(leafSpec.getShortLabelI18n());
                    if (leafSpec.getLongLabel() != null) {
                        builder.longLabel(leafSpec.getLongLabel());
                    } else {
                        builder.longLabel("");
                    }
                    builder.longLabelI18n(leafSpec.getLongLabelI18n());
                } else {
                    builder.modelName(effectiveClass.getSimpleName())
                            .association(true)
                            .referenceModel(parentReferenceModel + "/" + field.getName())
                            .domainModel(buildNestedForUnspecified(effectiveClass, parentReferenceModel + "/" + field.getName()));
                }

                child = builder.build();
                parent.getDomainModel().getAttributes().add(child);
            } catch (NoSuchFieldException e) {
                return;
            }
        }

        if (isLast && child != null && !normalizedValidations.isEmpty()) {
            child.setValidations(normalizedValidations);
        }

        if (!isLast && child != null) {
            Class<?> childClass = getFieldClass(currentClass, child.getAttributeName());
            if (childClass != null) {
                buildNestedFields(child, segments, index + 1, childClass, leafSpec, normalizedValidations, enabledReferenceModels, requestAttributes, child.getReferenceModel());
            }
        }
    }

    private RawDomainAttribute buildAttributeFromField(Field field, Boolean visible, String shortLabel, String longLabel) {
        Class<?> fieldType = field.getType();
        boolean isCollection = List.class.isAssignableFrom(fieldType);
        Class<?> elementClass = isCollection ? getCollectionElementType(field) : fieldType;
        Class<?> effectiveClass = elementClass != null ? elementClass : fieldType;
        boolean isReference = isComplexType(effectiveClass);

        RawDomainAttribute.RawDomainAttributeBuilder builder = RawDomainAttribute.builder()
                .attributeType(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                .modelName(effectiveClass.getSimpleName())
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
            builder.modelName(effectiveClass.getSimpleName())
                    .association(true)
                    .domainModel(buildNestedForUnspecified(effectiveClass, field.getName()));
        }

        return builder.build();
    }

    private boolean hasValidations(Object validations) {
        if (validations == null) return false;
        if (validations instanceof List<?> list) return !list.isEmpty();
        if (validations instanceof Map<?, ?> map) return !map.isEmpty();
        return false;
    }

    private List<Map<String, Object>> normalizeValidations(Object validations) {
        return hasValidations(validations) ? convertValidations(validations) : Collections.emptyList();
    }

    private List<Map<String, Object>> convertValidations(Object validations) {
        if (validations == null) return Collections.emptyList();

        Map<String, Object> combinedValidations = new HashMap<>();

        if (validations instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    processValidationRule(combinedValidations, (Map<String, Object>) item);
                }
            }
        } else if (validations instanceof Map<?, ?> map) {
            processValidationRule(combinedValidations, (Map<String, Object>) map);
        } else if (validations instanceof String str && !str.isBlank()) {
            try {
                Object parsed = sharedMapper.readValue(str, Object.class);
                return convertValidations(parsed);
            } catch (Exception ignored) {}
        }

        return combinedValidations.isEmpty() ? Collections.emptyList() : List.of(combinedValidations);
    }

    private void processValidationRule(Map<String, Object> target, Map<String, Object> ruleMap) {
        if (ruleMap.containsKey("type")) {
            String type = (String) ruleMap.get("type");
            Map<String, Object> params = new HashMap<>();
            for (Map.Entry<String, Object> entry : ruleMap.entrySet()) {
                if (!"type".equals(entry.getKey()) && entry.getValue() != null) {
                    params.put(entry.getKey(), entry.getValue());
                }
            }
            target.put(type, params);
        } else {
            for (Map.Entry<String, Object> entry : ruleMap.entrySet()) {
                if (entry.getValue() instanceof Map<?, ?> paramMap) {
                    target.put(entry.getKey(), paramMap);
                }
            }
        }
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
            topAttr = buildDefaultAttributeTree(segments, 0, rootClass, "");
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
            Class<?> effectiveClass = elementClass != null ? elementClass : fieldType;
            boolean isReference = isComplexType(effectiveClass);

            RawDomainAttribute.RawDomainAttributeBuilder builder = RawDomainAttribute.builder()
                    .attributeName(field.getName())
                    .attributeType(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                    .modelName(effectiveClass.getSimpleName())
                    .referenceModel(currentRefModel.isBlank() ? field.getName() : currentRefModel + "/" + field.getName())
                    .reference(isReference)
                    .collection(isCollection);

            if (!isReference) {
                builder.visible(false);
            } else {
                String refModel = currentRefModel.isBlank() ? field.getName() : currentRefModel + "/" + field.getName();
                builder.modelName(effectiveClass.getSimpleName())
                        .association(true)
                        .referenceModel(refModel)
                        .domainModel(buildNestedForUnspecified(effectiveClass, refModel));
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
                Class<?> effectiveClass = elementClass != null ? elementClass : fieldType;
                boolean isReference = isComplexType(effectiveClass);

                RawDomainAttribute.RawDomainAttributeBuilder builder = RawDomainAttribute.builder()
                        .attributeName(field.getName())
                        .attributeType(elementClass != null ? elementClass.getSimpleName() : fieldType.getSimpleName())
                        .modelName(effectiveClass.getSimpleName())
                        .referenceModel(parent.getReferenceModel() + "/" + field.getName())
                        .reference(isReference)
                        .collection(isCollection);

                if (!isReference) {
                    builder.visible(false)
                            .shortLabel("")
                            .longLabel("");
                } else {
                    String refModel = parent.getReferenceModel() + "/" + field.getName();
                    builder.modelName(effectiveClass.getSimpleName())
                            .association(true)
                            .referenceModel(refModel)
                            .domainModel(buildNestedForUnspecified(effectiveClass, refModel));
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

    private RawDomainModel buildNestedForUnspecified(Class<?> clazz, String parentReferenceModel) {
        List<RawDomainAttribute> attributes = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            RawDomainAttribute attr = buildAttributeFromField(field, false, null, null);
            if (parentReferenceModel != null && !parentReferenceModel.isBlank()) {
                attr.setReferenceModel(parentReferenceModel + "/" + field.getName());
            } else {
                attr.setReferenceModel(field.getName());
            }
            attributes.add(attr);
        }
        return RawDomainModel.builder().attributes(attributes).build();
    }
}

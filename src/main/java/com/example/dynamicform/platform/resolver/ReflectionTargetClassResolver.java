package com.example.dynamicform.platform.resolver;

import org.springframework.stereotype.Component;

@Component
public class ReflectionTargetClassResolver implements TargetClassResolver {

    @Override
    public Class<?> resolve(String targetClassName) {
        if (targetClassName == null || targetClassName.isBlank()) {
            throw new IllegalArgumentException("Target DTO class name is required");
        }
        try {
            return Class.forName(targetClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Target DTO class not found: " + targetClassName, e);
        }
    }
}

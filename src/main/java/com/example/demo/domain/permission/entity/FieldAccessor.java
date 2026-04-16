package com.example.demo.domain.permission.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段访问器（缓存优化反射）
 * 避免重复反射开销，提升字段处理性能
 */
public class FieldAccessor {

    private static final ConcurrentHashMap<String, FieldAccessor> CACHE = new ConcurrentHashMap<>();

    private final Field field;
    private Method getter;
    private Method setter;

    private FieldAccessor(Field field) {
        this.field = field;
        field.setAccessible(true);

        String fieldName = field.getName();
        String capitalized = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        // 获取 getter 方法
        Method foundGetter = null;
        try {
            foundGetter = field.getDeclaringClass().getMethod("get" + capitalized);
        } catch (NoSuchMethodException e) {
            // getter not found, will use field access
        }
        this.getter = foundGetter;

        // 获取 setter 方法
        Method foundSetter = null;
        try {
            foundSetter = field.getDeclaringClass().getMethod("set" + capitalized, field.getType());
        } catch (NoSuchMethodException e) {
            // setter not found, will use field access
        }
        this.setter = foundSetter;
    }

    /**
     * 获取字段访问器（缓存）
     */
    public static FieldAccessor get(Class<?> clazz, String fieldName) {
        String key = clazz.getName() + "." + fieldName;
        return CACHE.computeIfAbsent(key, k -> {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                return new FieldAccessor(field);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Field not found: " + fieldName + " in " + clazz.getName(), e);
            }
        });
    }

    /**
     * 获取字段值
     */
    public Object getValue(Object target) {
        try {
            if (getter != null) {
                return getter.invoke(target);
            }
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field value: " + field.getName(), e);
        }
    }

    /**
     * 设置字段值
     */
    public void setValue(Object target, Object value) {
        try {
            if (setter != null) {
                setter.invoke(target, value);
            } else {
                field.set(target, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field value: " + field.getName(), e);
        }
    }

    /**
     * 获取字段类型
     */
    public Class<?> getFieldType() {
        return field.getType();
    }

    /**
     * 获取字段名
     */
    public String getFieldName() {
        return field.getName();
    }

    /**
     * 清除缓存（用于测试）
     */
    public static void clearCache() {
        CACHE.clear();
    }
}
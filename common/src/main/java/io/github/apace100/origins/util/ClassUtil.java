package io.github.apace100.origins.util;

public final class ClassUtil {
    @SuppressWarnings("unchecked")
    public static <T> Class<T> castClass(Class<?> aClass) {
        return (Class<T>)aClass;
    }
}

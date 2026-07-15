package com.vn.baseapis.utils;

import java.util.function.Function;

public class ValueUtils {
    @SafeVarargs
    public static <T> T coalesce(T... values) {
        if (values == null) return null;
        for (T value : values) {
            if (value != null) return value;
        }
        return null;
    }

    public static <T, R> R getOrNull(T obj, Function<T, R> getter) {
        return obj == null ? null : getter.apply(obj);
    }

}

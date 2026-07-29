package com.vn.baseapis.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
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

    public static Long parseLong(String value) {
        if (StringUtils.isBlank(value)) return null;
        String t = value.trim();
        int dot = t.indexOf('.');
        return Long.parseLong(dot >= 0 ? t.substring(0, dot) : t);
    }

    public static Integer parseInt(String s) {
        if (StringUtils.isBlank(s)) return null;
        return (int) Double.parseDouble(s.trim());
    }

    public static BigDecimal parseBigdecimal(String s) {
        if (StringUtils.isBlank(s)) return null;
        return new BigDecimal(s.trim());
    }

    public static Boolean parseBool(String s) {
        if (StringUtils.isBlank(s)) return null;
        return "TRUE".equalsIgnoreCase(s.trim()) || "1".equals(s.trim());
    }

    public static Float parseFloat(String s) {
        if (StringUtils.isBlank(s)) return null;
        return Float.parseFloat(s.trim());
    }
}

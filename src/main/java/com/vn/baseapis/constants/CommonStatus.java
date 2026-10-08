package com.vn.baseapis.constants;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
public enum CommonStatus {
    ACTIVE(1), INACTIVE(-1), DELETE(-3);
    private final int value;

    CommonStatus(int value) {
        this.value = value;
    }

    public static CommonStatus find(String name) {
        if (StringUtils.isBlank(name)) return null;
        return Arrays.stream(CommonStatus.values())
                .filter(status -> status.name().equalsIgnoreCase(name))
                .findAny()
                .orElse(null);
    }

    public static CommonStatus find(Integer value) {
        if (value == null) return null;
        return Arrays.stream(CommonStatus.values())
                .filter(status -> status.getValue() == value)
                .findAny()
                .orElse(null);
    }
}

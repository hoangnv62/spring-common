package com.vn.baseapis.constants;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
public enum AuthoritiesConstants {
    ROLE_ADMIN(1),
    ROLE_USER(2);
    private final int value;

    AuthoritiesConstants(int value) {
        this.value = value;
    }

    public static AuthoritiesConstants find(String name) {
        if (StringUtils.isBlank(name)) return null;
        return Arrays.stream(AuthoritiesConstants.values())
                .filter(authority -> authority.name().equalsIgnoreCase(name))
                .findAny()
                .orElse(null);
    }

    public static AuthoritiesConstants find(int value) {
        return Arrays.stream(AuthoritiesConstants.values())
                .filter(authority -> authority.getValue() == value)
                .findAny()
                .orElse(null);
    }
}

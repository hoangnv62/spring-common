package com.vn.baseapis.constants;

import lombok.Getter;

@Getter
public enum CommonStatus {
    ACTIVE(1), INACTIVE(-1), DELETE(-3);
    private final int value;

    CommonStatus(int value) {
        this.value = value;
    }
}

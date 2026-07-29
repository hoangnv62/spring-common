package com.vn.baseapis.dto.request;

import java.math.BigDecimal;

public record UserImportDTO(
        Long id,
        String fullName,
        String email,
        Integer age,
        BigDecimal balance,
        boolean active
) {
}

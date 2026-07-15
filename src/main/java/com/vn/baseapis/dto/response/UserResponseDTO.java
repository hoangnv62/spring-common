package com.vn.baseapis.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record UserResponseDTO(
        Long id,
        String fullName,
        String email,
        int age,
        BigDecimal balance,
        boolean active,
        LocalDateTime createdAt
) {
}

package com.vn.baseapis.dto.request;

import java.math.BigDecimal;

/**
 * DTO đại diện một dòng người dùng đọc được khi import file Excel (dữ liệu đầu vào).
 * Tách khỏi {@code UserResponseDTO} vì đây là dữ liệu vào, không phải dữ liệu trả ra.
 */
public record UserImportDTO(
        Long id,
        String fullName,
        String email,
        int age,
        BigDecimal balance,
        boolean active
) {
}

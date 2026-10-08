package com.vn.baseapis.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequestDTO(
        @NotBlank(message = "Email không được để trống")
        String email,
        @NotBlank(message = "Tên không được để trống")
        String fullName,
        @NotBlank(message = "Trạng thái không được để trống")
        String status,
        @NotBlank(message = "Vai trò không được để trống")
        String role
) {
}

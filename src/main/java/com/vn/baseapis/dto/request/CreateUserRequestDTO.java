package com.vn.baseapis.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequestDTO(
        @NotBlank(message = "Email không được để trống")
        String email,
        @NotBlank(message = "Tên không được để trống")
        String fullName,
        @NotBlank(message = "Mật khẩu không được để trống")
        String password,
        @NotBlank(message = "Trạng thái không được để trống")
        String status,
        @NotBlank(message = "Vai trò không được để trống")
        String role
) {
}

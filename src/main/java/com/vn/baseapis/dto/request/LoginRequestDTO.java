package com.vn.baseapis.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "email không được để trống")
        String email,

        @NotBlank(message = "password không được để trống")
        String password
) {
}

package com.vn.baseapis.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.baseapis.constants.ApiResponseCode;
import com.vn.baseapis.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Trả về 403 dạng JSON {@link ApiErrorResponse} khi user đã xác thực nhưng không đủ quyền,
 * thay cho trang lỗi HTML mặc định của container.
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ApiErrorResponse body = new ApiErrorResponse(
                ApiResponseCode.FORBIDDEN.getCode(),
                ApiResponseCode.FORBIDDEN.getError(),
                "Không có quyền truy cập");
        objectMapper.writeValue(response.getWriter(), body);
    }
}

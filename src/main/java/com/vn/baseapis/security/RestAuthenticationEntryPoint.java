package com.vn.baseapis.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.baseapis.constants.ApiResponseCode;
import com.vn.baseapis.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Trả về 401 dạng JSON {@link ApiErrorResponse} khi request chưa xác thực truy cập endpoint được bảo vệ,
 * thay cho hành vi mặc định của Spring Security (redirect tới trang login / trang lỗi HTML của container).
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ApiErrorResponse body = new ApiErrorResponse(
                ApiResponseCode.UNAUTHORIZED.getCode(),
                ApiResponseCode.UNAUTHORIZED.getError(),
                "Yêu cầu chưa được xác thực");
        objectMapper.writeValue(response.getWriter(), body);
    }
}

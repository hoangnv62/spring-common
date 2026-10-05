package com.vn.baseapis.controllers;

import com.vn.baseapis.config.IpRateLimited;
import com.vn.baseapis.domain.User;
import com.vn.baseapis.dto.request.LoginRequestDTO;
import com.vn.baseapis.dto.request.RefreshTokenRequest;
import com.vn.baseapis.dto.response.TokenResponse;
import com.vn.baseapis.repository.UserRepository;
import com.vn.baseapis.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    /**
     * Đăng nhập bằng email/password, trả về cặp access/refresh token.
     * Trả 401 nếu sai thông tin đăng nhập.
     * Giới hạn 5 lần/phút mỗi IP để chặn dò mật khẩu (brute force).
     */
    @PostMapping("/login")
    @IpRateLimited(limit = 5, durationSeconds = 60)
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Đổi refresh token lấy cặp access/refresh token mới.
     * Trả 401 (UnauthenticatedException) nếu refresh token không hợp lệ, hết hạn hoặc sai loại.
     * Giới hạn nới hơn login vì client hợp lệ có thể refresh định kỳ.
     */
    @PostMapping("/refresh")
    @IpRateLimited(limit = 20, durationSeconds = 60)
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }
}

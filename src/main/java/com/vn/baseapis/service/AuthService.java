package com.vn.baseapis.service;

import com.vn.baseapis.constants.ApiResponseCode;
import com.vn.baseapis.constants.AuthoritiesConstants;
import com.vn.baseapis.constants.CommonStatus;
import com.vn.baseapis.domain.User;
import com.vn.baseapis.dto.request.LoginRequestDTO;
import com.vn.baseapis.dto.response.TokenResponse;
import com.vn.baseapis.exception.BusinessException;
import com.vn.baseapis.repository.UserRepository;
import com.vn.baseapis.security.jwt.TokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Đăng nhập bằng email/password. Ném {@link com.vn.baseapis.exception.UnauthenticatedException}
     * nếu sai thông tin đăng nhập.
     */
    public TokenResponse login(LoginRequestDTO request) {
        User user = findActiveUserByEmail(request.email());
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ApiResponseCode.ENTITY_NOT_FOUND, "Mật khẩu không chính xác");
        }
        log.info("Đăng nhập thành công userId={}", user.getId());
        AuthoritiesConstants authority = AuthoritiesConstants.find(user.getRole());
        if (authority == null) throw new BusinessException(ApiResponseCode.INTERNAL_SERVER_ERROR);
        return issueTokens(user.getId(), user.getEmail(), authority.name());
    }

    /**
     * Đổi refresh token hợp lệ lấy cặp token mới (rotation: cấp lại cả access lẫn refresh token).
     * Ném {@link com.vn.baseapis.exception.UnauthenticatedException} nếu refresh token không hợp lệ/hết hạn/sai loại.
     *
     * <p>Lưu ý: quyền hạn (authority) hiện được lấy lại từ chính refresh token. Khi có tầng lưu trữ user,
     * nên nạp lại authority từ DB tại đây để phản ánh quyền mới nhất thay vì tin giá trị cũ trong token.
     */
    public TokenResponse refresh(String refreshToken) {
        Claims claims = tokenProvider.parseRefreshToken(refreshToken);
        Long userId = tokenProvider.getUserId(claims);
        log.info("Refresh token cho userId={}", userId);
        return issueTokens(userId, tokenProvider.getEmail(claims), tokenProvider.getAuthority(claims));
    }

    private User findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndStatus(email, CommonStatus.ACTIVE.getValue())
                .orElseThrow(() -> new BusinessException(ApiResponseCode.ENTITY_NOT_FOUND, "Không tìm thấy người dùng"));
    }

    private TokenResponse issueTokens(Long userId, String email, String authority) {
        return new TokenResponse(
                tokenProvider.generateAccessToken(userId, email, authority),
                tokenProvider.generateRefreshToken(userId, email, authority),
                TokenResponse.BEARER,
                tokenProvider.getAccessTokenValiditySeconds(),
                tokenProvider.getRefreshTokenValiditySeconds());
    }
}

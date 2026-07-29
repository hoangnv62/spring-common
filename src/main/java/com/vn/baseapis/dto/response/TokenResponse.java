package com.vn.baseapis.dto.response;

/**
 * Kết quả cấp phát token cho client.
 * {@code expiresIn} tính bằng giây (chuẩn OAuth2).
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
    public static final String BEARER = "Bearer";
}

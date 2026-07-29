package com.vn.baseapis.security.jwt;

import com.vn.baseapis.constants.TokenType;
import com.vn.baseapis.exception.UnauthenticatedException;
import com.vn.baseapis.security.AuthenticationToken;
import com.vn.baseapis.security.IBEUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class TokenProvider {
    private static final String AUTHORITIES_KEY = "auth";
    private static final String CREATED_KEY = "created";
    private static final String USER_ID_KEY = "user_id";
    private static final String EMAIL_KEY = "email";
    private static final String TOKEN_TYPE = "type";
    private static final String INVALID_JWT_TOKEN = "Invalid JWT token.";
    @Value("${app.security.authentication.jwt.base64-secret}")
    private String secretKey;
    @Value("${app.security.authentication.jwt.token-validity-in-seconds}")
    private Long accessTokenValidDuration;
    @Value("${app.security.authentication.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenValidDuration;
    private SecretKey key;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
        jwtParser = Jwts.parser().verifyWith(key).build();
    }

    public String generateAccessToken(Long userId, String email, String authority) {
        return buildToken(userId, email, authority, accessTokenValidDuration, TokenType.ACCESS_TOKEN);
    }

    public String generateRefreshToken(Long userId, String email, String authority) {
        return buildToken(userId, email, authority, refreshTokenValidDuration, TokenType.REFRESH_TOKEN);
    }

    private String buildToken(Long userId, String email, String authority, long validitySeconds, TokenType type) {
        Date expired = new Date(System.currentTimeMillis() + validitySeconds * 1000);
        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTHORITIES_KEY, authority);
        claims.put(CREATED_KEY, Instant.now().getEpochSecond());
        claims.put(USER_ID_KEY, userId);
        claims.put(EMAIL_KEY, email);
        claims.put(TOKEN_TYPE, type.name());
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .expiration(expired)
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String token) throws UnauthenticatedException {
        if (StringUtils.isBlank(token)) return null;
        try {
            // Chỉ access token mới được dùng để xác thực request; refresh token bị từ chối ở đây.
            Claims claims = parseClaims(token, TokenType.ACCESS_TOKEN);

            Object userIdClaim = claims.get(USER_ID_KEY);
            if (userIdClaim == null || StringUtils.isBlank(userIdClaim.toString())) {
                log.info("Invalid JWT: user_id null");
                throw new UnauthenticatedException();
            }
            long userId = Long.parseLong(userIdClaim.toString());

            String authoritiesStr = claims.getOrDefault(AUTHORITIES_KEY, "").toString();
            Collection<? extends GrantedAuthority> authorities = Arrays.stream(authoritiesStr.split(","))
                    .filter(auth -> !auth.trim().isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            IBEUser principle = new IBEUser(userId, claims.getSubject(), "", authorities);
            AuthenticationToken authenticationToken = new AuthenticationToken(principle, "", authorities, token);
            authenticationToken.setDetails("pre_auth");
            authenticationToken.setUserId(userId);
            if (!authoritiesStr.isBlank()) {
                authenticationToken.setRoles(new HashSet<>(Arrays.asList(authoritiesStr.split(","))));
            }
            return authenticationToken;
        } catch (UnauthenticatedException e) {
            throw e;
        } catch (ExpiredJwtException | SignatureException | UnsupportedJwtException | MalformedJwtException e) {
            log.trace(INVALID_JWT_TOKEN, e);
            throw new UnauthenticatedException();
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage(), e);
            throw new UnauthenticatedException();
        }
    }

    /**
     * Parse + verify chữ ký của token, đồng thời bắt buộc đúng loại ({@code type}).
     * Ném {@link UnauthenticatedException} nếu token sai loại; các lỗi JWT khác để caller bắt.
     */
    private Claims parseClaims(String token, TokenType expectedType) {
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
        Object tokenType = claims.get(TOKEN_TYPE);
        if (!expectedType.name().equals(tokenType)) {
            log.info("Invalid JWT: expect {} nhưng nhận {}", expectedType, tokenType);
            throw new UnauthenticatedException();
        }
        return claims;
    }

    /**
     * Xác thực refresh token (đúng chữ ký, chưa hết hạn, đúng loại REFRESH_TOKEN) và trả claims.
     * Dùng cho luồng /auth/refresh để cấp lại access token.
     */
    public Claims parseRefreshToken(String refreshToken) throws UnauthenticatedException {
        if (StringUtils.isBlank(refreshToken)) {
            throw new UnauthenticatedException();
        }
        try {
            return parseClaims(refreshToken, TokenType.REFRESH_TOKEN);
        } catch (UnauthenticatedException e) {
            throw e;
        } catch (ExpiredJwtException | SignatureException | UnsupportedJwtException | MalformedJwtException e) {
            log.trace(INVALID_JWT_TOKEN, e);
            throw new UnauthenticatedException();
        } catch (Exception e) {
            log.error("Refresh token validation error: {}", e.getMessage(), e);
            throw new UnauthenticatedException();
        }
    }

    public Long getUserId(Claims claims) {
        return Long.parseLong(String.valueOf(claims.get(USER_ID_KEY)));
    }

    public String getEmail(Claims claims) {
        Object email = claims.get(EMAIL_KEY);
        return email != null ? email.toString() : claims.getSubject();
    }

    public String getAuthority(Claims claims) {
        return claims.getOrDefault(AUTHORITIES_KEY, "").toString();
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenValidDuration;
    }

    public long getRefreshTokenValiditySeconds() {
        return refreshTokenValidDuration;
    }
}

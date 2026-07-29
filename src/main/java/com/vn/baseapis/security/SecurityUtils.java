package com.vn.baseapis.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    public static Optional<Long> getCurrentUserIdLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication()).map(authentication -> {
            if (authentication instanceof AuthenticationToken authenticationToken) {
                return authenticationToken.getUserId();
            }
            return null;
        });
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (authentication.getPrincipal() instanceof String principal) {
            return principal;
        }
        return null;
    }
}

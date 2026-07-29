package com.vn.baseapis.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.util.Collection;
import java.util.Set;

@Getter
@Setter
public class AuthenticationToken extends UsernamePasswordAuthenticationToken {
    private String token;
    private Set<String> roles;
    private Long userId;
    private String username;

    public AuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public AuthenticationToken(Object principal, Object credentials, String token) {
        super(principal, credentials);
        this.token = token;
    }

    public AuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public AuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String token) {
        super(principal, credentials, authorities);
        this.token = token;
    }
}

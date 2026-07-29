package com.vn.baseapis.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Cung cấp "người thao tác" cho JPA Auditing (điền vào các field @CreatedBy / @LastModifiedBy).
 * Tên bean mặc định là "springSecurityAuditorAware" — khớp với auditorAwareRef trong DatabaseConfiguration.
 */
@Component
public class SpringSecurityAuditorAware implements AuditorAware<Long> {

    private static final Long SYSTEM_ID = 0L;

    @Override
    @NonNull
    public Optional<Long> getCurrentAuditor() {
        return Optional.of(SecurityUtils.getCurrentUserIdLogin().orElse(SYSTEM_ID));
    }
}


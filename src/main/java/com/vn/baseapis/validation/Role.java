package com.vn.baseapis.validation;

import com.vn.baseapis.utils.DateTimeUtils;
import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RoleValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface Role {
    String message() default "Vai trò không hợp lệ";

    String pattern();

    Class<?>[] groups() default {};
}

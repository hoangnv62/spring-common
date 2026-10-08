package com.vn.baseapis.validation;

import com.vn.baseapis.constants.AuthoritiesConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class RoleValidator implements ConstraintValidator<Role, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) return true;
        return AuthoritiesConstants.find(value) != null;
    }
}

package com.vn.baseapis.validation;

import com.vn.baseapis.constants.CommonStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class CommonStatusValidator implements ConstraintValidator<MyStatus, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) return true;
        return CommonStatus.find(value) != null;
    }
}

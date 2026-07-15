package com.vn.baseapis.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validator cho {@link MyDateTime}: parse chuỗi theo {@code pattern} rồi format ngược lại để so sánh.
 * Cách round-trip này bắt được cả sai định dạng lẫn ngày giờ không có thật.
 */
public class DateTimeValidator implements ConstraintValidator<MyDateTime, String> {

    private DateTimeFormatter formatter;

    @Override
    public void initialize(MyDateTime annotation) {
        this.formatter = DateTimeFormatter.ofPattern(annotation.pattern());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // để @NotNull/@NotBlank lo phần bắt buộc
        }
        String trimmed = value.trim();
        try {
            LocalDateTime parsed = LocalDateTime.parse(trimmed, formatter);
            return formatter.format(parsed).equals(trimmed);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}

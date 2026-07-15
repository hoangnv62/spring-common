package com.vn.baseapis.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validator cho {@link MyDate}: parse chuỗi theo {@code pattern} rồi format ngược lại để so sánh.
 * Cách round-trip này bắt được cả sai định dạng lẫn ngày không có thật (ví dụ 31/02).
 */
public class DateValidator implements ConstraintValidator<MyDate, String> {

    private DateTimeFormatter formatter;

    @Override
    public void initialize(MyDate annotation) {
        this.formatter = DateTimeFormatter.ofPattern(annotation.pattern());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // để @NotNull/@NotBlank lo phần bắt buộc
        }
        String trimmed = value.trim();
        try {
            LocalDate parsed = LocalDate.parse(trimmed, formatter);
            // Nếu định dạng lại không khớp chuỗi gốc => ngày bị chuẩn hoá (vd 31/02 -> 29/02) => sai.
            return formatter.format(parsed).equals(trimmed);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}

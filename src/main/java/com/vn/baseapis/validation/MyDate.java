package com.vn.baseapis.validation;

import com.vn.baseapis.utils.DateTimeUtils;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Kiểm tra một {@link String} có phải là ngày hợp lệ theo định dạng cho trước hay không.
 *
 * <p>Mặc định định dạng {@code dd/MM/yyyy}. Việc kiểm tra vừa xét đúng định dạng (kể cả padding
 * số 0, ví dụ {@code "5/7/2026"} bị coi là sai) vừa xét ngày có thật (loại {@code "31/02/2026"}).</p>
 *
 * <p>Giá trị {@code null} hoặc chuỗi rỗng/toàn khoảng trắng được coi là hợp lệ — hãy kết hợp
 * {@code @NotNull}/{@code @NotBlank} nếu trường là bắt buộc.</p>
 *
 * <pre>{@code
 * public record CreatePromotionRequest(
 *         @Date String startDate,                       // dd/MM/yyyy
 *         @Date(pattern = "yyyy-MM-dd") String endDate  // đổi định dạng nếu cần
 * ) {}
 * }</pre>
 */
@Documented
@Constraint(validatedBy = DateValidator.class)
@Target({FIELD, METHOD, PARAMETER, CONSTRUCTOR, ANNOTATION_TYPE, RECORD_COMPONENT, TYPE_USE})
@Retention(RUNTIME)
public @interface MyDate {

    String message() default "Ngày không hợp lệ, định dạng yêu cầu: {pattern}";

    String pattern() default DateTimeUtils.DATE_FORMAT;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

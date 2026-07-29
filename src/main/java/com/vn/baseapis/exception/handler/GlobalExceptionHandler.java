package com.vn.baseapis.exception.handler;

import com.vn.baseapis.constants.ApiResponseCode;
import com.vn.baseapis.dto.response.ApiErrorResponse;
import com.vn.baseapis.exception.*;
import com.vn.baseapis.utils.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

/**
 * Xử lý tập trung mọi exception thoát ra khỏi controller và trả về định dạng {@link ApiErrorResponse} thống nhất.
 * Handler cụ thể luôn được Spring ưu tiên hơn handler tổng quát, nên {@link #handleUnexpected} là chốt chặn cuối.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    // ------------------------------------------------------------------
    // Business / domain exceptions
    // ------------------------------------------------------------------

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.error("handleBusinessException(): {}", ex.toString());
        Object[] params = (ex.getParams() != null) ? ex.getParams().toArray() : null;
        String desc = resolveDescription(ex, request, params);
        return ResponseEntity.status(Integer.parseInt(ex.getCode()))
                .body(new ApiErrorResponse(ex.getCode(), ex.getMessage(), desc));
    }

    @ExceptionHandler(InternalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleInternalException(InternalException ex, HttpServletRequest request) {
        log.error("handleInternalException(): {}", ex.toString());
        return new ApiErrorResponse(ex.getCode(), ex.getMessage(), resolveDescription(ex, request, null));
    }

    @ExceptionHandler(CallApiException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ApiErrorResponse handleCallApiException(CallApiException ex, HttpServletRequest request) {
        log.error("handleCallApiException(): {}", ex.toString());
        return new ApiErrorResponse(ex.getCode(), ex.getMessage(), resolveDescription(ex, request, null));
    }

    // ------------------------------------------------------------------
    // Validation & request binding (400 / 413 / 415 / 405)
    // ------------------------------------------------------------------

    /**
     * Bao gồm cả {@link MethodArgumentNotValidException} (là con của {@link BindException}).
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBindException(BindException ex) {
        log.warn("handleBindException(): {}", ex.getMessage());
        String desc = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return error(ApiResponseCode.BAD_REQUEST, desc);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("handleConstraintViolation(): {}", ex.getMessage());
        String desc = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return error(ApiResponseCode.BAD_REQUEST, desc);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("handleHttpMessageNotReadable(): {}", ex.getMessage());
        return error(ApiResponseCode.BAD_REQUEST, "error json format");
    }

    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleDateTimeParse(DateTimeParseException ex) {
        log.warn("handleDateTimeParse(): {}", ex.getMessage());
        return error(ApiResponseCode.BAD_REQUEST, "Định dạng ngày/giờ không hợp lệ: " + ex.getParsedString());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("handleMethodArgumentTypeMismatch(): {}", ex.getMessage());
        String desc = String.format("Tham số '%s' có giá trị không hợp lệ: '%s'", ex.getName(), ex.getValue());
        return error(ApiResponseCode.BAD_REQUEST, desc);
    }

    /**
     * Bắt mọi lỗi thiếu param/part/header/cookie/path-variable (con của {@link ServletRequestBindingException}).
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleServletRequestBinding(ServletRequestBindingException ex) {
        log.warn("handleServletRequestBinding(): {}", ex.getMessage());
        return error(ApiResponseCode.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ApiErrorResponse handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("handleMaxUploadSizeExceeded(): {}", ex.getMessage());
        return error(ApiResponseCode.PAYLOAD_TOO_LARGE, "Kích thước file vượt quá giới hạn cho phép");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiErrorResponse handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("handleMediaTypeNotSupported(): {}", ex.getMessage());
        return error(ApiResponseCode.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiErrorResponse handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("handleMethodNotSupported(): {}", ex.getMessage());
        return error(ApiResponseCode.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Security (401 / 403)
    // ------------------------------------------------------------------

    /**
     * Gộp mọi lỗi xác thực (sai thông tin đăng nhập, chưa đăng nhập, ...).
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleAuthenticationException(AuthenticationException ex) {
        log.warn("handleAuthenticationException(): {}", ex.getMessage());
        return error(ApiResponseCode.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn");
    }

    @ExceptionHandler(UnauthenticatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleUnauthenticated(UnauthenticatedException ex) {
        log.warn("handleUnauthenticated(): {}", ex.getMessage());
        String desc = (ex.getMessage() != null) ? ex.getMessage() : "Phiên đăng nhập không hợp lệ";
        return error(ApiResponseCode.UNAUTHORIZED, desc);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiErrorResponse handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("handleAccessDeniedException(): {}", ex.getMessage());
        return error(ApiResponseCode.FORBIDDEN, "Forbidden");
    }

    // ------------------------------------------------------------------
    // Not found (404)
    // ------------------------------------------------------------------

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNotFound(Exception ex) {
        log.warn("handleNotFound(): {}", ex.getMessage());
        return new ApiErrorResponse(ApiResponseCode.RESOURCE_NOT_FOUND);
    }

    // ------------------------------------------------------------------
    // Rate limit (429)
    // ------------------------------------------------------------------

    @ExceptionHandler(TooManyRequestsException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ApiErrorResponse handleTooManyRequests(TooManyRequestsException ex) {
        log.warn("handleTooManyRequests(): {}", ex.getMessage());
        return error(ApiResponseCode.TOO_MANY_REQUESTS, ex.getMessage());
    }

    // ------------------------------------------------------------------
    // Fallback (500) — chốt chặn cuối cho mọi exception chưa được xử lý riêng.
    // ------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleUnexpected(Exception ex) {
        log.error("handleUnexpected(): {}", ExceptionUtils.getStackTrace(ex));
        return new ApiErrorResponse(ApiResponseCode.INTERNAL_SERVER_ERROR);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static ApiErrorResponse error(ApiResponseCode code, String description) {
        return new ApiErrorResponse(code.getCode(), code.getError(), description);
    }

    private String resolveDescription(BaseException ex, HttpServletRequest request, Object[] params) {
        if (ex.getMessageDescription() != null) {
            return ex.getMessageDescription();
        }
        return (params != null && params.length > 0)
                ? CommonUtils.getMessage(messageSource, request, ex.getMessage(), params)
                : CommonUtils.getMessage(messageSource, request, ex.getMessage());
    }

}

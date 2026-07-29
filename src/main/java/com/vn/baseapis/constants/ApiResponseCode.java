package com.vn.baseapis.constants;

import com.vn.baseapis.dto.response.IApiResponse;

public enum ApiResponseCode implements IApiResponse {
    SUCCESS("200", "SUCCESS"),
    BAD_REQUEST("400", "BAD_REQUEST"),
    UNAUTHORIZED("401", "UNAUTHORIZED"),
    FORBIDDEN("403", "FORBIDDEN"),
    RESOURCE_NOT_FOUND("404", "RESOURCE_NOT_FOUND"),
    ENTITY_NOT_FOUND("404", "ENTITY_NOT_FOUND"),
    METHOD_NOT_ALLOWED("405", "METHOD_NOT_ALLOWED"),
    PAYLOAD_TOO_LARGE("413", "PAYLOAD_TOO_LARGE"),
    UNSUPPORTED_MEDIA_TYPE("415", "UNSUPPORTED_MEDIA_TYPE"),
    TOO_MANY_REQUESTS("429", "TOO_MANY_REQUESTS"),
    INTERNAL_SERVER_ERROR("500", "INTERNAL_SERVER_ERROR"),

    INVALID_OAUTH_CODE("445", "INVALID_OAUTH_CODE"),
    NOT_ACTIVE("403", "NOT_ACTIVE"),
            ;

    private final String code;
    private final String error;

    ApiResponseCode(String code, String error) {
        this.code = code;
        this.error = error;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getError() {
        return error;
    }

}

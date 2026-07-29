package com.vn.baseapis.exception;

import com.vn.baseapis.constants.ApiResponseCode;
import com.vn.baseapis.dto.response.IApiResponse;

public class InternalException extends BaseException {
    public InternalException() {
        super(ApiResponseCode.INTERNAL_SERVER_ERROR.getCode(), ApiResponseCode.INTERNAL_SERVER_ERROR.getError());
    }

    public InternalException(String code, String message) {
        super(code, message);
    }

    public InternalException(String code, String message, String messageDescription) {
        super(code, message, messageDescription);
    }

    public InternalException(IApiResponse apiResponse) {
        super(apiResponse);
    }
}

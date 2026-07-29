package com.vn.baseapis.exception;

import com.vn.baseapis.dto.response.IApiResponse;

public class CallApiException extends BaseException {
    public CallApiException() {
    }

    public CallApiException(String code, String message) {
        super(code, message);
    }

    public CallApiException(String code, String message, String messageDescription) {
        super(code, message, messageDescription);
    }

    public CallApiException(IApiResponse apiResponse) {
        super(apiResponse);
    }
}


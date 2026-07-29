package com.vn.baseapis.exception;

import com.vn.baseapis.dto.response.IApiResponse;
import lombok.Getter;
import lombok.Setter;

@Setter
public class BaseException extends RuntimeException {
    @Getter
    protected String code;
    protected String message;
    @Getter
    protected String messageDescription;

    public BaseException() {
    }

    public BaseException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseException(IApiResponse apiResponse) {
        this.code = apiResponse.getCode();
        this.message = apiResponse.getError();
    }

    public BaseException(String code, String message, String messageDescription) {
        super(message);
        this.messageDescription = messageDescription;
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "BaseException{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", messageDescription='" + messageDescription + '\'' +
                '}';
    }
}

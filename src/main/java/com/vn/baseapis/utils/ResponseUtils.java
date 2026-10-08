package com.vn.baseapis.utils;

import com.vn.baseapis.constants.ApiResponseCode;
import com.vn.baseapis.dto.response.CommonResponseDTO;

public class ResponseUtils {
    public static CommonResponseDTO success() {
        return CommonResponseDTO.builder()
                .status(ApiResponseCode.SUCCESS.getCode())
                .response(ApiResponseCode.SUCCESS.getError())
                .build();
    }
}

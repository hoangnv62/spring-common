package com.vn.baseapis.dto.request;

import com.vn.baseapis.validation.MyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExamRequestDTO (
        @NotBlank(message = "Tên bài thi không được để trống")
        String name,
        @NotBlank(message = "Mô tả không được để trống")
        String description,
        @NotNull(message ="Thời gian làm bài không được để trống")
        Integer durationInMinutes,
        @NotBlank(message = "Trạng thái không được để trống")
        @MyStatus
        String status
){
}

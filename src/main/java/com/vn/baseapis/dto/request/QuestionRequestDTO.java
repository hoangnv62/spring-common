package com.vn.baseapis.dto.request;

import com.vn.baseapis.validation.MyStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuestionRequestDTO (
        @NotBlank(message = "Nội dung câu hỏi không được để trống")
        String content,

        @NotBlank(message = "Trạng thái không được để trống")
        @MyStatus
        String status,

        @Valid
        @NotEmpty(message = "Danh sách câu trả lời không được để trống")
        List<AnswerRequestDTO> answers
){
}

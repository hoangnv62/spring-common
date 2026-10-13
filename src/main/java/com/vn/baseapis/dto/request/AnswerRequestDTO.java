package com.vn.baseapis.dto.request;

import com.vn.baseapis.validation.MyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerRequestDTO {
    Long id;
    @NotBlank(message = "Nội dung câu trả lời không được để trống")
    String content;
    boolean isCorrected = false;
}

package com.vn.baseapis.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResponseDTO {
    Long id;
    String content;
    String status;
    List<AnswerResponseDTO> answers;
}

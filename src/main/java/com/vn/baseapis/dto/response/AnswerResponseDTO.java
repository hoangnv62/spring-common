package com.vn.baseapis.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerResponseDTO {
    Long id;
    String content;
    boolean isCorrected;
}

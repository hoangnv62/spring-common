package com.vn.baseapis.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamResponseDTO {
    Long id;
    String name;
    String description;
    Integer durationInMinutes;
    String status;
}

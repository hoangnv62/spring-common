package com.vn.baseapis.dto.response;

import java.time.Instant;

public record DateRangeResponseDTO(Instant dateFrom, Instant dateTo) {
}

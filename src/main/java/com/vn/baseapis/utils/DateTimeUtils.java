package com.vn.baseapis.utils;

import com.vn.baseapis.constants.ApiResponseCode;
import com.vn.baseapis.dto.response.DateRangeResponseDTO;
import com.vn.baseapis.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DateTimeUtils {
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    private static final String ZONE_ID_7 = "UTC+07:00"; // Asia/Ho_Chi_Minh

    private static final DateTimeFormatter vnFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.of("vi", "VN"))
            .withZone(ZoneId.of(ZONE_ID_7));

    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    public static final ZoneId zoneId7 = ZoneId.of(ZONE_ID_7);

    public static LocalDate toLocalDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(zoneId7).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, zoneId7);
    }

    public static Instant toInstantStart(String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return null;
        }

        // Parse the string to LocalDate
        LocalDate date = LocalDate.parse(dateString, dateFormatter);

        // Combine with time and timezone
        ZonedDateTime zonedDateTime = date.atTime(LocalTime.MIN).atZone(zoneId7);

        // Convert LocalDateTime to Instant (assuming UTC offset)
        return zonedDateTime.toInstant();
    }

    public static DateRangeResponseDTO toDateRange(String dateFrom, String dateTo) {
        Instant dateFromInstant = toInstantStart(dateFrom);
        Instant dateToInstant = toInstantEnd(dateTo);
        if (dateFromInstant != null && dateToInstant != null && dateFromInstant.isAfter(dateToInstant))
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "Thời gian bắt đầu phải nằm trước thời gian kết thúc");
        return new DateRangeResponseDTO(dateFromInstant, dateToInstant);
    }

    public static Instant toInstantEnd(String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return null;
        }

        // Parse the string to LocalDate
        LocalDate date = LocalDate.parse(dateString, dateFormatter);
        LocalTime localTime = LocalTime.of(23, 59, 59);

        // Combine with time and timezone
        ZonedDateTime zonedDateTime = date.atTime(localTime).atZone(zoneId7);

        // Convert LocalDateTime to Instant (assuming UTC offset)
        return zonedDateTime.toInstant();
    }

    public static Instant toInstant(String dateTimeStringZoneUTC7) {
        if (dateTimeStringZoneUTC7 == null || StringUtils.isBlank(dateTimeStringZoneUTC7)) {
            return null;
        }

        // Parse the string to LocalDateTime using the dateTimeFormatter
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStringZoneUTC7, dateTimeFormatter);

        // Combine with timezone UTC+7
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId7);
        return zonedDateTime.toInstant();
    }

    public static LocalDate toLocalDate(String dateString) {
        return StringUtils.isBlank(dateString) ? null : LocalDate.parse(dateString, dateFormatter);
    }

    public static LocalDateTime toLocalDateTime(String dateString) {
        return StringUtils.isBlank(dateString) ? null : LocalDateTime.parse(dateString, dateTimeFormatter);
    }

    public static long localDateToMillis(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.of(String.valueOf(zoneId7))).toInstant().toEpochMilli();
    }

    public static String convertInstantToDateString(Instant instant) {
        // Convert to ZonedDateTime with UTC+7
        ZonedDateTime zonedDateTime = instant.atZone(zoneId7);
        // Format the date
        return zonedDateTime.format(dateFormatter);
    }


}

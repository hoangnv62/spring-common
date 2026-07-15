package com.vn.baseapis.service.io.importer.user;

import com.vn.baseapis.dto.request.UserImportDTO;
import com.vn.baseapis.service.io.importer.ExcelRow;
import com.vn.baseapis.service.io.importer.ExcelRowMapper;

import java.math.BigDecimal;

/**
 * Map một dòng Excel thành {@link UserImportDTO}. Không có state nên thread-safe, tái sử dụng được.
 */
public class UserExcelRowMapper implements ExcelRowMapper<UserImportDTO> {
    private static final int ID_COL = 0;
    private static final int FULL_NAME_COL = 1;
    private static final int EMAIL_COL = 2;
    private static final int AGE_COL = 3;
    private static final int BALANCE_COL = 4;
    private static final int STATUS_COL = 5;

    @Override
    public UserImportDTO map(ExcelRow row) {
        String idStr = row.get(ID_COL);
        if (idStr == null || idStr.isBlank()) {
            return null; // bỏ qua dòng không có mã
        }
        Long id = parseLong(idStr); // "1" hoặc "1.0" đều được, không mất chính xác với id lớn
        String fullName = row.get(FULL_NAME_COL);
        String email = row.get(EMAIL_COL);
        int age = parseInt(row.get(AGE_COL));
        BigDecimal balance = parseBig(row.get(BALANCE_COL));
        boolean active = parseBool(row.get(STATUS_COL));
        return new UserImportDTO(id, fullName, email, age, balance, active);
    }

    private static long parseLong(String s) {
        // POI có thể trả ô số nguyên dưới dạng "1" hoặc "1.0"; cắt phần thập phân rồi parse thẳng
        // qua long để tránh mất chính xác với id lớn (> 2^53) khi đi qua double.
        String t = s.trim();
        int dot = t.indexOf('.');
        return Long.parseLong(dot >= 0 ? t.substring(0, dot) : t);
    }

    private static int parseInt(String s) {
        if (s == null || s.isBlank()) return 0;
        return (int) Double.parseDouble(s.trim());
    }

    private static BigDecimal parseBig(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(s.trim());
    }

    private static boolean parseBool(String s) {
        if (s == null) return false;
        return "TRUE".equalsIgnoreCase(s.trim()) || "1".equals(s.trim());
    }
}

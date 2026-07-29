package com.vn.baseapis.service.io.importer.user;

import com.vn.baseapis.dto.request.UserImportDTO;
import com.vn.baseapis.service.io.importer.ExcelRow;
import com.vn.baseapis.service.io.importer.ExcelRowMapper;
import com.vn.baseapis.utils.ValueUtils;

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
        Long id = ValueUtils.parseLong(idStr); // "1" hoặc "1.0" đều được, không mất chính xác với id lớn
        String fullName = row.get(FULL_NAME_COL);
        String email = row.get(EMAIL_COL);
        Integer age = ValueUtils.parseInt(row.get(AGE_COL));
        BigDecimal balance = ValueUtils.parseBigdecimal(row.get(BALANCE_COL));
        boolean active = ValueUtils.coalesce(ValueUtils.parseBool(row.get(STATUS_COL)), false);
        return new UserImportDTO(id, fullName, email, age, balance, active);
    }
}

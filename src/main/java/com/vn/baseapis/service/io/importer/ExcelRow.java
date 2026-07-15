package com.vn.baseapis.service.io.importer;

import java.util.List;

/**
 * Đại diện một dòng dữ liệu đọc được từ file Excel, cung cấp giá trị ô theo chỉ số cột (0-based).
 *
 * <p>Giá trị ô luôn ở dạng {@link String} đã được POI format sẵn (ví dụ ô ngày trả về "15/07/2026",
 * ô số trả về "12345.67"). Ô trống trả về {@code null}. Việc parse sang kiểu mong muốn
 * (số, ngày...) do {@link ExcelRowMapper} đảm nhiệm.</p>
 */
public final class ExcelRow {

    private final int rowNum;
    private final List<String> cells;

    ExcelRow(int rowNum, List<String> cells) {
        this.rowNum = rowNum;
        this.cells = cells;
    }

    /** Số thứ tự dòng trong file Excel (1-based, đúng như hiển thị trên phần mềm). */
    public int rowNum() {
        return rowNum;
    }

    /** Số ô có trong dòng (tính tới cột cuối cùng có dữ liệu). */
    public int size() {
        return cells.size();
    }

    /**
     * Lấy giá trị ô tại cột {@code col} (0-based) dưới dạng chuỗi.
     *
     * @return chuỗi giá trị, hoặc {@code null} nếu ô trống / cột vượt quá phạm vi
     */
    public String get(int col) {
        if (col < 0 || col >= cells.size()) {
            return null;
        }
        return cells.get(col);
    }
}

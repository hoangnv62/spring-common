package com.vn.baseapis.service.io.importer;

/**
 * Ánh xạ một {@link ExcelRow} (dòng trong file Excel) thành object {@code T}.
 *
 * <p>Trả về {@code null} để bỏ qua dòng đó (ví dụ dòng trống hoặc không hợp lệ) — importer sẽ
 * không thêm nó vào kết quả.</p>
 *
 * @param <T> kiểu object đích
 */
@FunctionalInterface
public interface ExcelRowMapper<T> {

    T map(ExcelRow row);
}


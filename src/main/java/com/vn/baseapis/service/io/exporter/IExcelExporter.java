package com.vn.baseapis.service.io.exporter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Hợp đồng chung cho việc export một danh sách sang file Excel (.xlsx).
 *
 * <p>Cho phép caller (controller, service...) phụ thuộc vào abstraction này thay vì lớp cụ thể,
 * nhờ đó dễ hoán đổi giữa {@link BaseExcelExporter} (dữ liệu nhỏ) và
 * {@link StreamExcelExporter} (dữ liệu lớn) mà không đổi code phía dùng.</p>
 *
 * @param <T> kiểu của một bản ghi (row)
 *
 */
public interface IExcelExporter<T> {

    /**
     * Ghi dữ liệu ra một {@link OutputStream} dưới dạng file .xlsx.
     * Không đóng {@code out} — trách nhiệm đóng thuộc về caller.
     *
     * @param data dữ liệu cần export (có thể là {@code List} hoặc {@code Iterable} lười)
     * @param out  luồng đích để ghi file
     */
    void export(Iterable<? extends T> data, OutputStream out) throws IOException;
}

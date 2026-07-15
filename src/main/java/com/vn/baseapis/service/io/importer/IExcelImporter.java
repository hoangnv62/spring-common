package com.vn.baseapis.service.io.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Hợp đồng chung cho việc import file Excel (.xlsx) và chuyển mỗi dòng thành một object {@code T}.
 *
 * @param <T> kiểu của một bản ghi (row) sau khi map
 */
public interface IExcelImporter<T> {

    /**
     * Đọc file theo kiểu streaming: mỗi dòng dữ liệu sau khi map sẽ được đẩy vào {@code consumer}.
     * Phù hợp file lớn hoặc khi muốn xử lý theo lô (batch insert DB) mà không giữ hết trong RAM.
     * Không đóng {@code in} — trách nhiệm đóng thuộc về caller.
     *
     * @param in       luồng đọc file .xlsx
     * @param consumer nơi nhận từng bản ghi đã map
     */
    void read(InputStream in, Consumer<? super T> consumer) throws IOException;

    /**
     * Đọc toàn bộ file và gom thành một {@link List}.
     * Tiện khi số dòng vừa phải; với dữ liệu cực lớn nên cân nhắc {@link #read} để xử lý theo lô.
     */
    default List<T> importAll(InputStream in) throws IOException {
        List<T> list = new ArrayList<>();
        read(in, list::add);
        return list;
    }
}

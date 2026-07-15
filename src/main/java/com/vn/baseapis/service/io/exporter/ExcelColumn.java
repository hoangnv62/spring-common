package com.vn.baseapis.service.io.exporter;

import java.util.function.Function;

/**
 * Định nghĩa một cột trong file Excel: tiêu đề (header) và cách lấy giá trị từ một bản ghi.
 *
 * <p>Kiểu giá trị trả về từ {@code valueExtractor} sẽ được exporter tự động ánh xạ sang ô Excel:
 * {@link String}, {@link Number}, {@link Boolean}, {@link java.time.LocalDate},
 * {@link java.time.LocalDateTime}, {@link java.util.Date}; các kiểu khác dùng {@code toString()};
 * {@code null} tạo ô trống.</p>
 *
 * @param <T> kiểu của một bản ghi (row)
 */
public final class ExcelColumn<T> {

    /** Độ rộng cột chưa được đặt tường minh (để exporter tự quyết định). */
    public static final int WIDTH_UNSET = -1;

    private final String header;
    private final Function<? super T, ?> valueExtractor;
    private int width = WIDTH_UNSET;

    private ExcelColumn(String header, Function<? super T, ?> valueExtractor) {
        if (header == null) {
            throw new IllegalArgumentException("header của cột không được null");
        }
        if (valueExtractor == null) {
            throw new IllegalArgumentException("valueExtractor của cột '" + header + "' không được null");
        }
        this.header = header;
        this.valueExtractor = valueExtractor;
    }

    /**
     * Tạo một cột.
     *
     * @param header         tiêu đề hiển thị trên dòng đầu
     * @param valueExtractor hàm lấy giá trị của cột này từ một bản ghi
     */
    public static <T> ExcelColumn<T> of(String header, Function<? super T, ?> valueExtractor) {
        return new ExcelColumn<>(header, valueExtractor);
    }

    /**
     * Đặt độ rộng cột cố định (tính theo số ký tự, ~1 ký tự = bề rộng một chữ số).
     * Nếu không đặt, {@code BaseExcelExporter} sẽ auto-size, còn {@code StreamExcelExporter}
     * dùng độ rộng mặc định.
     */
    public ExcelColumn<T> width(int chars) {
        if (chars <= 0) {
            throw new IllegalArgumentException("width phải > 0, nhận được: " + chars);
        }
        this.width = chars;
        return this;
    }

    public String header() {
        return header;
    }

    /** Lấy giá trị của cột từ một bản ghi (có thể trả về {@code null}). */
    public Object valueOf(T row) {
        return valueExtractor.apply(row);
    }

    /** Độ rộng cột theo ký tự, hoặc {@link #WIDTH_UNSET} nếu chưa đặt. */
    public int width() {
        return width;
    }
}

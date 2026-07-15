package com.vn.baseapis.service.io.exporter;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.List;

/**
 * Exporter cho danh sách <b>lớn</b> (khoảng ~100.000 bản ghi hoặc hơn).
 *
 * <p>Dùng {@link SXSSFWorkbook} (streaming): tại mỗi thời điểm chỉ giữ một cửa sổ nhỏ các dòng
 * trong RAM ({@value #DEFAULT_WINDOW_SIZE} dòng), phần còn lại được flush ra file tạm nên
 * bộ nhớ gần như không tăng theo số dòng. Đổi lại không auto-size cột (mặc định dùng độ rộng
 * cố định; có thể chỉ định qua {@link ExcelColumn#width(int)}).</p>
 *
 * <p><b>Lưu ý về nguồn dữ liệu:</b> để thật sự tiết kiệm RAM, hãy truyền một {@code Iterable}
 * lười (ví dụ đọc theo trang từ DB hoặc {@code Stream::iterator}) thay vì nạp toàn bộ 100k
 * bản ghi vào một {@code List} trước khi export.</p>
 *
 * <pre>{@code
 * ExcelExporter<Order> exporter = new StreamExcelExporter<>(List.of(
 *         ExcelColumn.of("Mã", Order::getId),
 *         ExcelColumn.of("Khách hàng", Order::getCustomer).width(30),
 *         ExcelColumn.of("Ngày đặt", Order::getCreatedAt)
 * ), "Đơn hàng");
 *
 * ExcelDownload.to(response, "don-hang", exporter, orderStream::iterator);
 * }</pre>
 *
 * @param <T> kiểu của một bản ghi (row)
 */
public class StreamExcelExporter<T> extends AbstractExcelExporter<T> {

    /** Số dòng giữ trong RAM trước khi flush ra đĩa. */
    public static final int DEFAULT_WINDOW_SIZE = 100;

    private final int windowSize;

    public StreamExcelExporter(List<ExcelColumn<T>> columns) {
        this(columns, DEFAULT_SHEET_NAME, DEFAULT_WINDOW_SIZE);
    }

    public StreamExcelExporter(List<ExcelColumn<T>> columns, String sheetName) {
        this(columns, sheetName, DEFAULT_WINDOW_SIZE);
    }

    /**
     * @param windowSize số dòng giữ trong RAM (lớn hơn thì nhanh hơn nhưng tốn RAM hơn)
     */
    public StreamExcelExporter(List<ExcelColumn<T>> columns, String sheetName, int windowSize) {
        super(columns, sheetName);
        if (windowSize <= 0) {
            throw new IllegalArgumentException("windowSize phải > 0, nhận được: " + windowSize);
        }
        this.windowSize = windowSize;
    }

    @Override
    protected Workbook createWorkbook() {
        SXSSFWorkbook workbook = new SXSSFWorkbook(windowSize);
        workbook.setCompressTempFiles(true); // nén file tạm để tiết kiệm dung lượng đĩa
        return workbook;
    }

    @Override
    protected boolean autoSizeColumns() {
        return false;
    }
}

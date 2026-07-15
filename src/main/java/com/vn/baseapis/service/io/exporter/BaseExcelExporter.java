package com.vn.baseapis.service.io.exporter;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

/**
 * Exporter cho danh sách <b>nhỏ</b> (khoảng ~1000 bản ghi trở xuống).
 *
 * <p>Dùng {@link XSSFWorkbook} — giữ toàn bộ workbook trong RAM nên cho phép auto-size cột
 * và thao tác linh hoạt. Với dữ liệu lớn (hàng chục nghìn dòng) hãy dùng
 * {@link StreamExcelExporter} để tránh {@code OutOfMemoryError}.</p>
 *
 * @param <T> kiểu của một bản ghi (row)
 */
public class BaseExcelExporter<T> extends AbstractExcelExporter<T> {

    public BaseExcelExporter(List<ExcelColumn<T>> columns) {
        super(columns);
    }

    public BaseExcelExporter(List<ExcelColumn<T>> columns, String sheetName) {
        super(columns, sheetName);
    }

    @Override
    protected Workbook createWorkbook() {
        return new XSSFWorkbook();
    }

    @Override
    protected boolean autoSizeColumns() {
        return true;
    }
}

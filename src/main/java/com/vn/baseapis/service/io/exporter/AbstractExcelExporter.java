package com.vn.baseapis.service.io.exporter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Khung xử lý chung cho việc export danh sách sang file Excel (.xlsx) dựa trên Apache POI.
 *
 * <p>Lớp con quyết định loại {@link Workbook}:
 * <ul>
 *   <li>{@link BaseExcelExporter} — {@code XSSFWorkbook} (giữ toàn bộ trong RAM), phù hợp
 *       danh sách nhỏ (~1000 bản ghi trở xuống), có auto-size cột.</li>
 *   <li>{@link StreamExcelExporter} — {@code SXSSFWorkbook} (streaming, flush ra đĩa), phù hợp
 *       danh sách lớn (~100.000 bản ghi), dùng độ rộng cột cố định để giữ RAM ở mức thấp.</li>
 * </ul>
 *
 * @param <T> kiểu của một bản ghi (row)
 */
public abstract class AbstractExcelExporter<T> implements IExcelExporter<T> {

    /**
     * Độ rộng cột mặc định (số ký tự) khi cột không đặt width và không auto-size.
     */
    protected static final int DEFAULT_COLUMN_WIDTH_CHARS = 20;

    /**
     * Tên sheet mặc định khi caller không truyền.
     */
    protected static final String DEFAULT_SHEET_NAME = "Data";

    protected final List<ExcelColumn<T>> columns;
    private final String sheetName;

    protected AbstractExcelExporter(List<ExcelColumn<T>> columns) {
        this(columns, DEFAULT_SHEET_NAME);
    }

    protected AbstractExcelExporter(List<ExcelColumn<T>> columns, String sheetName) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Danh sách cột (columns) không được rỗng");
        }
        this.columns = List.copyOf(columns);
        this.sheetName = (sheetName == null || sheetName.isBlank()) ? DEFAULT_SHEET_NAME : sheetName;
    }

    /**
     * Lớp con tạo workbook phù hợp (XSSF hoặc SXSSF).
     */
    protected abstract Workbook createWorkbook();

    /**
     * Lớp con quyết định có auto-size cột hay không (streaming thì không nên).
     */
    protected abstract boolean autoSizeColumns();

    /**
     * Hook mở rộng: ghi phần header tuỳ biến phía <b>trên</b> dòng tiêu đề cột
     * (tiêu đề lớn gộp ô, logo, dòng metadata như ngày xuất/bộ lọc, header nhiều tầng...).
     *
     * <p>Ghi bắt đầu từ dòng 0 và <b>trả về số dòng đã ghi</b> để exporter đặt dòng tiêu đề cột,
     * dữ liệu và freeze-pane ngay bên dưới. Mặc định trả 0 (không có header tuỳ biến — giữ nguyên
     * hành vi cũ). Lưu ý ghi các dòng theo thứ tự từ trên xuống (0, 1, 2...) để tương thích streaming.
     *
     * @param sheet    sheet đang ghi
     * @param workbook workbook (để tạo {@code CellStyle}/{@code Font} khi cần)
     * @return số dòng đã ghi phía trên dòng tiêu đề cột (>= 0)
     */
    protected int writeCustomHeader(Sheet sheet, Workbook workbook) {
        return 0;
    }

    @Override
    public void export(Iterable<? extends T> data, OutputStream out) throws IOException {
        // try-with-resources: với SXSSF, close() cũng tự xoá các file tạm trên đĩa
        try (Workbook workbook = createWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            ExcelStyles styles = new ExcelStyles(workbook);

            int headerRowIndex = writeCustomHeader(sheet, workbook);
            if (headerRowIndex < 0) {
                throw new IllegalStateException("writeCustomHeader phải trả về số dòng >= 0");
            }

            writeHeaderRow(sheet, headerRowIndex, styles);

            int rowIndex = headerRowIndex + 1;
            if (data != null) {
                for (T item : data) {
                    writeDataRow(sheet, rowIndex++, item, styles);
                }
            }

            sheet.createFreezePane(0, headerRowIndex + 1); // ghim phần header khi cuộn
            applyColumnWidths(sheet);

            workbook.write(out);
            out.flush();
        }
    }

    // ---------------------------------------------------------------------
    // Nội bộ
    // ---------------------------------------------------------------------

    private void writeHeaderRow(Sheet sheet, int rowIndex, ExcelStyles styles) {
        Row header = sheet.createRow(rowIndex);
        for (int c = 0; c < columns.size(); c++) {
            Cell cell = header.createCell(c);
            cell.setCellValue(columns.get(c).header());
            cell.setCellStyle(styles.header());
        }
    }

    private void writeDataRow(Sheet sheet, int rowIndex, T item, ExcelStyles styles) {
        Row row = sheet.createRow(rowIndex);
        for (int c = 0; c < columns.size(); c++) {
            Cell cell = row.createCell(c);
            setCellValue(cell, columns.get(c).valueOf(item), styles);
        }
    }

    private void applyColumnWidths(Sheet sheet) {
        for (int c = 0; c < columns.size(); c++) {
            int width = columns.get(c).width();
            if (width > 0) {
                sheet.setColumnWidth(c, charsToWidthUnits(width));
            } else if (autoSizeColumns()) {
                sheet.autoSizeColumn(c);
            } else {
                sheet.setColumnWidth(c, charsToWidthUnits(DEFAULT_COLUMN_WIDTH_CHARS));
            }
        }
    }

    private static int charsToWidthUnits(int chars) {
        // POI tính độ rộng theo đơn vị 1/256 ký tự; giới hạn tối đa 255 ký tự.
        return Math.min(chars, 255) * 256;
    }

    private void setCellValue(Cell cell, Object value, ExcelStyles styles) {
        switch (value) {
            case null -> cell.setBlank();
            case String s -> cell.setCellValue(s);
            case Number n -> cell.setCellValue(n.doubleValue());
            case Boolean b -> cell.setCellValue(b);
            case LocalDate d -> {
                cell.setCellValue(d);
                cell.setCellStyle(styles.date());
            }
            case LocalDateTime dt -> {
                cell.setCellValue(dt);
                cell.setCellStyle(styles.dateTime());
            }
            case Date d -> {
                cell.setCellValue(d);
                cell.setCellStyle(styles.dateTime());
            }
            default -> cell.setCellValue(value.toString());
        }
    }

    private static final class ExcelStyles {
        private final CellStyle header;
        private final CellStyle date;
        private final CellStyle dateTime;

        ExcelStyles(Workbook workbook) {
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            this.header = workbook.createCellStyle();
            this.header.setFont(headerFont);
            this.header.setAlignment(HorizontalAlignment.CENTER);
            this.header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            this.header.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            short dateFmt = workbook.createDataFormat().getFormat("dd/mm/yyyy");
            this.date = workbook.createCellStyle();
            this.date.setDataFormat(dateFmt);

            short dateTimeFmt = workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss");
            this.dateTime = workbook.createCellStyle();
            this.dateTime.setDataFormat(dateTimeFmt);
        }

        CellStyle header() {
            return header;
        }

        CellStyle date() {
            return date;
        }

        CellStyle dateTime() {
            return dateTime;
        }
    }
}

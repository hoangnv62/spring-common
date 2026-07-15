package com.vn.baseapis.service.io.exporter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Tiện ích ghi các dòng header tuỳ biến (tiêu đề lớn, dòng metadata...) trong
 * {@link AbstractExcelExporter#writeCustomHeader(Sheet, Workbook)}.
 *
 * <p>Mỗi hàm ghi một dòng bắt đầu tại {@code startRow} và trả về <b>số dòng đã ghi</b> để caller
 * cộng dồn offset:</p>
 *
 * <pre>{@code
 * @Override
 * protected int writeCustomHeader(Sheet sheet, Workbook wb) {
 *     int r = 0;
 *     r += ExcelHeaders.title(sheet, wb, r, "DANH SÁCH NGƯỜI DÙNG", columns.size());
 *     r += ExcelHeaders.line(sheet, wb, r, "Ngày xuất: 15/07/2026", columns.size());
 *     return r; // = 2
 * }
 * }</pre>
 */
public final class ExcelHeaders {

    private ExcelHeaders() {
    }

    /**
     * Ghi một dòng tiêu đề lớn: gộp toàn bộ các cột, in đậm cỡ 16, canh giữa.
     *
     * @return số dòng đã ghi (luôn là 1)
     */
    public static int title(Sheet sheet, Workbook workbook, int startRow, String text, int columnCount) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return mergedLine(sheet, startRow, text, columnCount, style, 26);
    }

    /**
     * Ghi một dòng thông tin phụ (metadata): gộp toàn bộ các cột, chữ thường canh trái.
     *
     * @return số dòng đã ghi (luôn là 1)
     */
    public static int line(Sheet sheet, Workbook workbook, int startRow, String text, int columnCount) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return mergedLine(sheet, startRow, text, columnCount, style, -1);
    }

    private static int mergedLine(Sheet sheet, int startRow, String text, int columnCount,
                                  CellStyle style, int heightInPoints) {
        Row row = sheet.createRow(startRow);
        if (heightInPoints > 0) {
            row.setHeightInPoints(heightInPoints);
        }
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(style);
        if (columnCount > 1) {
            sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, 0, columnCount - 1));
        }
        return 1;
    }
}

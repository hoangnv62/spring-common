package com.vn.baseapis.service.io.exporter;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Helper cho tầng web: xuất Excel thẳng vào {@link HttpServletResponse} để người dùng tải về.
 *
 * <p>Tách riêng khỏi {@link IExcelExporter} để việc tạo file Excel không phụ thuộc vào servlet/HTTP
 * (Single Responsibility). Class này chỉ lo đặt header và nối exporter với luồng của response.</p>
 *
 * <pre>{@code
 * ExcelExporter<User> exporter = new BaseExcelExporter<>(columns).sheetName("Người dùng");
 * ExcelDownload.to(response, "danh-sach-nguoi-dung", exporter, users);
 * }</pre>
 */
public final class ExcelDownload {

    /**
     * Content-Type chuẩn cho file .xlsx.
     */
    public static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ExcelDownload() {
    }

    /**
     * Đặt header tải file rồi ghi Excel thẳng vào response.
     *
     * @param response response của request hiện tại
     * @param fileName tên file hiển thị khi tải (tự thêm đuôi ".xlsx" nếu thiếu)
     * @param exporter exporter dùng để tạo file
     * @param data     dữ liệu cần export
     */
    public static <T> void to(HttpServletResponse response,
                              String fileName,
                              IExcelExporter<T> exporter,
                              Iterable<? extends T> data) throws IOException {
        String safeName = normalizeFileName(fileName);
        String encoded = URLEncoder.encode(safeName, StandardCharsets.UTF_8).replace("+", "%20");

        response.setContentType(XLSX_CONTENT_TYPE);
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + safeName + "\"; filename*=UTF-8''" + encoded);

        exporter.export(data, response.getOutputStream());
    }

    private static String normalizeFileName(String fileName) {
        String name = (fileName == null || fileName.isBlank()) ? "export" : fileName.trim();
        if (!name.toLowerCase().endsWith(".xlsx")) {
            name = name + ".xlsx";
        }
        return name;
    }
}

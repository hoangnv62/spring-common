package com.vn.baseapis.controllers;

import com.vn.baseapis.config.IpRateLimited;
import com.vn.baseapis.service.io.exporter.user.UserExportService;
import com.vn.baseapis.service.io.importer.user.UserImportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Controller demo để thử tải file Excel qua trình duyệt.
 *
 * <ul>
 *   <li>GET /api/users/export/small — ~1000 bản ghi (BaseExcelExporter)</li>
 *   <li>GET /api/users/export/large — ~100.000 bản ghi (StreamExcelExporter)</li>
 *   <li>GET /api/users/export/custom-header — có tiêu đề lớn + metadata phía trên bảng</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users/export")
@RequiredArgsConstructor
public class UserExportController {

    private final UserExportService userExportService;
    private final UserImportService userImportService;

    /** Export nhẹ (~1000 bản ghi) nên giới hạn rộng hơn /large. */
    @GetMapping("/small")
    @IpRateLimited(limit = 10, durationSeconds = 60)
    public void exportSmall(HttpServletResponse response) throws IOException {
        userExportService.exportSmall(response);
    }

    /** Export ~100.000 bản ghi, tốn nhiều CPU/RAM/băng thông nên siết chặt nhất. */
    @GetMapping("/large")
    @IpRateLimited(limit = 2, durationSeconds = 60)
    public void exportLarge(HttpServletResponse response) throws IOException {
        userExportService.exportLarge(response);
    }

    @GetMapping("/custom-header")
    @IpRateLimited(limit = 10, durationSeconds = 60)
    public void exportWithCustomHeader(HttpServletResponse response) throws IOException {
        userExportService.exportWithCustomHeader(response);
    }

    /**
     * Upload file .xlsx và import thành danh sách. Trả về tổng số + vài bản ghi mẫu để kiểm chứng.
     * Giới hạn 5 lần/phút mỗi IP vì mỗi lần import phải parse cả file.
     */
    @PostMapping("/import")
    @IpRateLimited(limit = 5, durationSeconds = 60)
    public ResponseEntity<Void> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        userImportService.importUsers(file.getInputStream());
        return ResponseEntity.noContent().build();
    }
}

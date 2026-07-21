package com.vn.baseapis.controllers;

import com.vn.baseapis.dto.request.UserImportDTO;
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

    @GetMapping("/small")
    public void exportSmall(HttpServletResponse response) throws IOException {
        userExportService.exportSmall(response);
    }

    @GetMapping("/large")
    public void exportLarge(HttpServletResponse response) throws IOException {
        userExportService.exportLarge(response);
    }

    @GetMapping("/custom-header")
    public void exportWithCustomHeader(HttpServletResponse response) throws IOException {
        userExportService.exportWithCustomHeader(response);
    }

    /** Upload file .xlsx và import thành danh sách. Trả về tổng số + vài bản ghi mẫu để kiểm chứng. */
    @PostMapping("/import")
    public ResponseEntity<Void> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        List<UserImportDTO> users = userImportService.importUsers(file.getInputStream());
        return ResponseEntity.noContent().build();
    }
}

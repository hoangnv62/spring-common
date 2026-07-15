package com.vn.baseapis.service.io.importer.user;

import com.vn.baseapis.dto.request.UserImportDTO;
import com.vn.baseapis.service.io.exporter.user.UserExportService;
import com.vn.baseapis.service.io.importer.StreamExcelImporter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Service demo import file Excel người dùng (đối xứng với {@link UserExportService}).
 * Dùng {@link StreamExcelImporter} để đọc file lớn mà không tốn RAM.
 */
@Service
public class UserImportService {

    /**
     * Mapper không có state nên tái sử dụng một instance, tránh tạo mới mỗi request.
     */
    private static final UserExcelRowMapper USER_MAPPER = new UserExcelRowMapper();

    public List<UserImportDTO> importUsers(InputStream in) throws IOException {
        // headerRows = 1: bỏ qua đúng dòng tiêu đề cột của file "small"/"large".
        StreamExcelImporter<UserImportDTO> importer = new StreamExcelImporter<>(USER_MAPPER);
        return importer.importAll(in);
    }

}

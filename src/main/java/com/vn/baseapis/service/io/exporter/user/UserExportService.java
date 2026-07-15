package com.vn.baseapis.service.io.exporter.user;

import com.vn.baseapis.service.io.exporter.BaseExcelExporter;
import com.vn.baseapis.service.io.exporter.ExcelColumn;
import com.vn.baseapis.service.io.exporter.ExcelDownload;
import com.vn.baseapis.service.io.exporter.ExcelHeaders;
import com.vn.baseapis.service.io.exporter.IExcelExporter;
import com.vn.baseapis.service.io.exporter.StreamExcelExporter;
import com.vn.baseapis.dto.response.UserResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service demo cho việc export danh sách người dùng ra Excel, dùng dữ liệu giả (fake).
 *
 * <p>Minh hoạ hai kịch bản:
 * <ul>
 *   <li>{@link #exportSmall} — danh sách nhỏ (~1000 bản ghi) qua {@link BaseExcelExporter}.</li>
 *   <li>{@link #exportLarge} — danh sách lớn (~100.000 bản ghi) qua {@link StreamExcelExporter},
 *       dùng nguồn dữ liệu lười để giữ RAM ở mức thấp.</li>
 * </ul>
 */
@Service
public class UserExportService {

    private static final String ID_COLUMN = "Mã";
    private static final String NAME_COLUMN = "Họ tên";
    private static final String EMAIL_COLUMN = "Email";
    private static final String AGE_COLUMN = "Tuổi";
    private static final String BALANCE_COLUMN = "Số dư";
    private static final String ACTIVE_COLUMN = "Kích hoạt";
    private static final String DATE_CREATED_COLUMN = "Ngày tạo";

    /**
     * Định nghĩa cột dùng chung cho cả hai kịch bản export.
     */
    private static final List<ExcelColumn<UserResponseDTO>> COLUMNS = List.of(
            ExcelColumn.of(ID_COLUMN, UserResponseDTO::id).width(8),
            ExcelColumn.of(NAME_COLUMN, UserResponseDTO::fullName).width(25),
            ExcelColumn.of(EMAIL_COLUMN, UserResponseDTO::email).width(30),
            ExcelColumn.of(AGE_COLUMN, UserResponseDTO::age).width(8),
            ExcelColumn.of(BALANCE_COLUMN, UserResponseDTO::balance).width(15),
            ExcelColumn.of(ACTIVE_COLUMN, UserResponseDTO::active).width(12),
            ExcelColumn.of(DATE_CREATED_COLUMN, UserResponseDTO::createdAt).width(20)
    );

    /**
     * Export ~1000 bản ghi (giữ toàn bộ trong RAM).
     */
    public void exportSmall(HttpServletResponse response) throws IOException {
        List<UserResponseDTO> users = fakeUsers(1_000).toList();
        IExcelExporter<UserResponseDTO> exporter = new BaseExcelExporter<>(COLUMNS, "Người dùng");
        ExcelDownload.to(response, "users-small", exporter, users);
    }

    /**
     * Export ~100.000 bản ghi (streaming, nguồn dữ liệu lười).
     */
    public void exportLarge(HttpServletResponse response) throws IOException {
        // Không gọi toList() — để Stream sinh từng bản ghi khi exporter duyệt, tránh nạp hết vào RAM.
        Iterable<UserResponseDTO> users = () -> fakeUsers(100_000).iterator();
        IExcelExporter<UserResponseDTO> exporter = new StreamExcelExporter<>(COLUMNS, "Người dùng");
        ExcelDownload.to(response, "users-large", exporter, users);
    }

    /**
     * Export có header tuỳ biến: dòng tiêu đề lớn gộp ô + dòng metadata phía trên bảng.
     * Chỉ cần override {@code writeCustomHeader}; phần tiêu đề cột, dữ liệu và freeze-pane
     * exporter tự dịch xuống theo số dòng trả về.
     */
    public void exportWithCustomHeader(HttpServletResponse response) throws IOException {
        List<UserResponseDTO> users = fakeUsers(1_000).toList();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        IExcelExporter<UserResponseDTO> exporter =
                new BaseExcelExporter<>(COLUMNS, "Người dùng") {
                    @Override
                    protected int writeCustomHeader(Sheet sheet, Workbook workbook) {
                        int cols = COLUMNS.size();
                        int r = 0;
                        r += ExcelHeaders.title(sheet, workbook, r, "DANH SÁCH NGƯỜI DÙNG", cols);
                        r += ExcelHeaders.line(sheet, workbook, r, "Ngày xuất: " + today, cols);
                        r += ExcelHeaders.line(sheet, workbook, r, "Tổng số: " + users.size() + " bản ghi", cols);
                        return r; // dòng tiêu đề cột sẽ nằm ở dòng thứ r
                    }
                };
        ExcelDownload.to(response, "users-custom-header", exporter, users);
    }

    // ---------------------------------------------------------------------
    // Sinh dữ liệu giả (deterministic theo id nên không cần Random)
    // ---------------------------------------------------------------------

    private static final String[] HO = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Vũ", "Đặng", "Bùi", "Đỗ", "Hồ"};
    private static final String[] DEM = {"Văn", "Thị", "Hữu", "Đức", "Minh", "Quang", "Thanh", "Ngọc"};
    private static final String[] TEN = {"An", "Bình", "Cường", "Dung", "Giang", "Hà", "Khánh", "Linh", "Nam", "Phúc"};

    /**
     * Sinh một Stream lười gồm {@code count} người dùng giả, id chạy từ 1..count.
     */
    private Stream<UserResponseDTO> fakeUsers(int count) {
        LocalDateTime base = LocalDateTime.now();
        return IntStream.rangeClosed(1, count).mapToObj(i -> fakeUser(i, base));
    }

    private UserResponseDTO fakeUser(long id, LocalDateTime base) {
        String fullName = HO[(int) (id * 7 % HO.length)]
                + " " + DEM[(int) (id * 3 % DEM.length)]
                + " " + TEN[(int) (id % TEN.length)];
        String email = "user" + id + "@example.com";
        int age = 18 + (int) (id % 50);
        BigDecimal balance = BigDecimal.valueOf(id * 123_457L % 1_000_000L, 2); // 2 chữ số thập phân
        boolean active = id % 2 == 0;
        LocalDateTime createdAt = base.minusMinutes(id);
        return new UserResponseDTO(id, fullName, email, age, balance, active, createdAt);
    }
}

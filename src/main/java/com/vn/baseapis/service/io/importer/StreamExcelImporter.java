package com.vn.baseapis.service.io.importer;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Importer cho file Excel <b>lớn</b> (hàng chục đến hàng trăm nghìn dòng).
 *
 * <p>Dùng event model (SAX) của Apache POI: đọc và parse tuần tự từng dòng trong XML của sheet,
 * không dựng toàn bộ workbook trong RAM như {@code WorkbookFactory.create}. Nhờ đó bộ nhớ gần như
 * không tăng theo số dòng — mỗi lần chỉ giữ dữ liệu của một dòng.</p>
 *
 * <p>Mỗi dòng dữ liệu được đưa vào {@link ExcelRowMapper} để chuyển thành object {@code T}.
 * Giá trị ô luôn ở dạng chuỗi đã format sẵn (xem {@link ExcelRow}).</p>
 *
 * <pre>{@code
 * ExcelRowMapper<User> mapper = row -> new User(
 *         Long.valueOf(row.get(0)),
 *         row.get(1),
 *         row.get(2));
 *
 * var importer = new StreamExcelImporter<>(mapper); // bỏ qua 1 dòng tiêu đề
 *
 * // Cách 1: gom hết vào List
 * List<User> users = importer.importAll(inputStream);
 *
 * // Cách 2: xử lý theo lô, không giữ hết trong RAM
 * importer.read(inputStream, user -> batch.add(user));
 * }</pre>
 *
 * @param <T> kiểu object đích
 */
public class StreamExcelImporter<T> implements IExcelImporter<T> {

    /** Số dòng tiêu đề bỏ qua ở đầu sheet, mặc định 1 (chỉ dòng tiêu đề cột). */
    public static final int DEFAULT_HEADER_ROWS = 1;

    private final ExcelRowMapper<T> mapper;
    private final int headerRows;
    private final int sheetIndex;

    public StreamExcelImporter(ExcelRowMapper<T> mapper) {
        this(mapper, DEFAULT_HEADER_ROWS, 0);
    }

    public StreamExcelImporter(ExcelRowMapper<T> mapper, int headerRows) {
        this(mapper, headerRows, 0);
    }

    /**
     * @param mapper     hàm map một dòng thành object {@code T}
     * @param headerRows số dòng đầu cần bỏ qua (tiêu đề lớn + metadata + tiêu đề cột)
     * @param sheetIndex sheet cần đọc (0-based)
     */
    public StreamExcelImporter(ExcelRowMapper<T> mapper, int headerRows, int sheetIndex) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper không được null");
        }
        if (headerRows < 0) {
            throw new IllegalArgumentException("headerRows phải >= 0, nhận được: " + headerRows);
        }
        if (sheetIndex < 0) {
            throw new IllegalArgumentException("sheetIndex phải >= 0, nhận được: " + sheetIndex);
        }
        this.mapper = mapper;
        this.headerRows = headerRows;
        this.sheetIndex = sheetIndex;
    }

    @Override
    public void read(InputStream in, Consumer<? super T> consumer) throws IOException {
        try (OPCPackage pkg = OPCPackage.open(in)) {
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();

            XMLReader parser = XMLHelper.newXMLReader();
            SheetContentsHandler rowHandler = new RowHandler(consumer);
            parser.setContentHandler(new XSSFSheetXMLHandler(styles, strings, rowHandler, false));

            Iterator<InputStream> sheets = reader.getSheetsData();
            int idx = 0;
            while (sheets.hasNext()) {
                try (InputStream sheet = sheets.next()) {
                    if (idx == sheetIndex) {
                        parser.parse(new InputSource(sheet));
                        return;
                    }
                }
                idx++;
            }
            throw new IOException("Không tìm thấy sheet ở vị trí " + sheetIndex);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Không đọc được file Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Handler SAX: gom các ô của một dòng rồi map thành object khi dòng kết thúc.
     * POI gọi các phương thức này tuần tự theo thứ tự dòng/ô trong file.
     */
    private final class RowHandler implements SheetContentsHandler {

        private final Consumer<? super T> consumer;
        private List<String> current;

        RowHandler(Consumer<? super T> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void startRow(int rowNum) {
            current = new ArrayList<>();
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            // Ô trống bị lược khỏi XML nên phải chèn null vào các cột bị khuyết dựa trên toạ độ ô.
            int col = (cellReference == null) ? current.size() : new CellReference(cellReference).getCol();
            while (current.size() < col) {
                current.add(null);
            }
            current.add(formattedValue);
        }

        @Override
        public void endRow(int rowNum) {
            if (rowNum < headerRows) {
                return; // bỏ qua dòng tiêu đề (rowNum 0-based)
            }
            if (isAllBlank(current)) {
                return; // bỏ qua dòng trống hoàn toàn
            }
            T mapped = mapper.map(new ExcelRow(rowNum + 1, current));
            if (mapped != null) {
                consumer.accept(mapped);
            }
        }

        private boolean isAllBlank(List<String> cells) {
            for (String c : cells) {
                if (c != null && !c.isBlank()) {
                    return false;
                }
            }
            return true;
        }
    }
}

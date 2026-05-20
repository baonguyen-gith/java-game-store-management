package otkhongluong.gamestoremanagement.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportUtil {

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FILE_FMT =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // ============================================================
    //  Mở hộp thoại chọn nơi lưu — View gọi trực tiếp
    // ============================================================
    public static String chooseFilePath(JComponent parent,
                                         String extension,
                                         String description) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file " + extension.toUpperCase());
        fc.setSelectedFile(new File("export_" +
            LocalDateTime.now().format(FILE_FMT) + "." + extension));
        fc.setFileFilter(
            new javax.swing.filechooser.FileNameExtensionFilter(
                description, extension));
        if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (!path.endsWith("." + extension)) path += "." + extension;
            return path;
        }
        return null;
    }

    // ============================================================
    //  Font tiếng Việt
    // ============================================================
    private static PdfFont getFont() throws IOException {
        // Windows
        return PdfFontFactory.createFont(
            "c:/windows/fonts/arial.ttf",
            "Identity-H",
            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        // Mac/Linux: copy arial.ttf vào src/main/resources/ rồi dùng:
        // return PdfFontFactory.createFont(
        //     ExportUtil.class.getResourceAsStream("/arial.ttf").readAllBytes(),
        //     "Identity-H",
        //     PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
    }

    // ============================================================
    //  EXPORT HÓA ĐƠN PDF
    //  Tham số khớp đúng với Invoice model thực tế:
    //  - Invoice chỉ có tongTien (không có discount/finalTotal riêng)
    //  - ChiTietHoaDon có tenGame, loaiSanPham, soLuong, donGia
    // ============================================================
    public static void exportInvoicePDF(
            String filePath,
            int maHD,
            String tenKhachHang,
            String soDienThoai,
            LocalDateTime ngayLap,
            String trangThai,
            List<String[]> items,   // [tenGame, loaiSP, soLuong, donGia, thanhTien]
            double tongTien) throws IOException {

        PdfWriter writer  = new PdfWriter(filePath);
        PdfDocument pdf   = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(40, 40, 40, 40);

        PdfFont font = getFont();

        // Tiêu đề
        document.add(new Paragraph("GAME STORE MANAGEMENT")
            .setFont(font).setFontSize(18).setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.DARK_GRAY));

        document.add(new Paragraph("HÓA ĐƠN BÁN HÀNG")
            .setFont(font).setFontSize(14).setBold()
            .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(" "));

        // Thông tin hóa đơn
        document.add(new Paragraph(
            "Mã hóa đơn : HD" + String.format("%04d", maHD))
            .setFont(font).setFontSize(11));
        document.add(new Paragraph(
            "Khách hàng : " + (tenKhachHang != null ? tenKhachHang : "Khách lẻ"))
            .setFont(font).setFontSize(11));
        document.add(new Paragraph(
            "SĐT        : " + (soDienThoai != null ? soDienThoai : "---"))
            .setFont(font).setFontSize(11));
        document.add(new Paragraph(
            "Ngày lập   : " + (ngayLap != null ? ngayLap.format(DT_FMT) : "---"))
            .setFont(font).setFontSize(11));
        document.add(new Paragraph(
            "Trạng thái : " + (trangThai != null ? trangThai : "---"))
            .setFont(font).setFontSize(11));

        document.add(new Paragraph(" "));

        // Bảng chi tiết: tenGame | loaiSP | SL | Đơn giá | Thành tiền
        float[] cols = {3f, 1.2f, 0.8f, 1.8f, 1.8f};
        Table table = new Table(UnitValue.createPercentArray(cols))
            .useAllAvailableWidth();

        for (String h : new String[]{
                "Tên Game", "Loại SP", "SL", "Đơn giá", "Thành tiền"}) {
            table.addHeaderCell(new Cell()
                .add(new Paragraph(h).setFont(font).setFontSize(10).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        }

        for (String[] row : items) {
            for (int i = 0; i < row.length; i++) {
                table.addCell(new Cell()
                    .add(new Paragraph(row[i]).setFont(font).setFontSize(9))
                    .setTextAlignment(i <= 1
                        ? TextAlignment.LEFT : TextAlignment.RIGHT));
            }
        }

        document.add(table);
        document.add(new Paragraph(" "));

        // Tổng tiền
        document.add(new Paragraph(
            "TỔNG TIỀN: " + FormatUtil.formatTien(tongTien))
            .setFont(font).setFontSize(14).setBold()
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontColor(ColorConstants.RED));

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Cảm ơn quý khách!")
            .setFont(font).setFontSize(11).setBold()
            .setTextAlignment(TextAlignment.CENTER));

        document.close();
    }

    // ============================================================
    //  EXPORT PHIẾU THUÊ PDF
    //  Khớp với RentalOrder + CTPhieuThue thực tế:
    //  - CTPhieuThue có maCD, tenGame, donGiaThue, trangThai
    //  - RentalOrder có tienCoc, tienPhat, trangThai
    // ============================================================
    public static void exportRentalPDF(
            String filePath,
            int maPT,
            String tenKhachHang,
            String soDienThoai,
            String tenNhanVien,
            LocalDateTime ngayThue,
            LocalDateTime ngayTraDuKien,
            LocalDateTime ngayTraThucTe,
            double tienCoc,
            double tienPhat,
            String trangThai,
            List<String[]> items,   // [maCD, tenGame, trangThaiCD, donGiaThue]
            double tongTienThue) throws IOException {

        PdfWriter writer  = new PdfWriter(filePath);
        PdfDocument pdf   = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(40, 40, 40, 40);

        PdfFont font = getFont();

        // Tiêu đề
        document.add(new Paragraph("GAME STORE MANAGEMENT")
            .setFont(font).setFontSize(18).setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.DARK_GRAY));

        document.add(new Paragraph("PHIẾU THUÊ")
            .setFont(font).setFontSize(14).setBold()
            .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(" "));

        // Thông tin phiếu thuê
        document.add(new Paragraph(
            "Mã phiếu thuê: PT" + String.format("%04d", maPT))
            .setFont(font).setFontSize(11));
        document.add(new Paragraph(
            "Khách hàng   : " + (tenKhachHang != null ? tenKhachHang : "---"))
            .setFont(font).setFontSize(11));
        document.add(new Paragraph(
            "SĐT          : " + (soDienThoai != null ? soDienThoai : "---"))
            .setFont(font).setFontSize(11));
        document.add(new Paragraph(
            "Nhân viên    : " + (tenNhanVien != null ? tenNhanVien : "---"))
            .setFont(font).setFontSize(11));
        document.add(new Paragraph(
            "Ngày thuê    : " + (ngayThue != null ? ngayThue.format(DT_FMT) : "---"))
            .setFont(font).setFontSize(11));
        document.add(new Paragraph(
            "Ngày trả DK  : " + (ngayTraDuKien != null ? ngayTraDuKien.format(DT_FMT) : "---"))
            .setFont(font).setFontSize(11));
        document.add(new Paragraph(
            "Ngày trả TT  : " + (ngayTraThucTe != null ? ngayTraThucTe.format(DT_FMT) : "Chưa trả"))
            .setFont(font).setFontSize(11));
        document.add(new Paragraph(
            "Trạng thái   : " + (trangThai != null ? trangThai : "---"))
            .setFont(font).setFontSize(11));

        document.add(new Paragraph(" "));

        // Bảng chi tiết đĩa CD
        float[] cols = {1.2f, 3f, 1.5f, 2f};
        Table table = new Table(UnitValue.createPercentArray(cols))
            .useAllAvailableWidth();

        for (String h : new String[]{"Mã CD", "Tên Game", "Tình trạng", "Đơn giá thuê"}) {
            table.addHeaderCell(new Cell()
                .add(new Paragraph(h).setFont(font).setFontSize(10).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        }

        for (String[] row : items) {
            for (int i = 0; i < row.length; i++) {
                table.addCell(new Cell()
                    .add(new Paragraph(row[i]).setFont(font).setFontSize(9))
                    .setTextAlignment(i == 3
                        ? TextAlignment.RIGHT : TextAlignment.LEFT));
            }
        }

        document.add(table);
        document.add(new Paragraph(" "));

        // Tổng kết tiền
        document.add(new Paragraph(
            "Tiền thuê    : " + FormatUtil.formatTien(tongTienThue))
            .setFont(font).setFontSize(11)
            .setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph(
            "Tiền cọc     : " + FormatUtil.formatTien(tienCoc))
            .setFont(font).setFontSize(11)
            .setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph(
            "Tiền phạt    : " + FormatUtil.formatTien(tienPhat))
            .setFont(font).setFontSize(11)
            .setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph(
            "TỔNG CỘNG   : " +
            FormatUtil.formatTien(tongTienThue + tienPhat))
            .setFont(font).setFontSize(14).setBold()
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontColor(ColorConstants.RED));

        document.close();
    }

    // ============================================================
    //  EXPORT BÁO CÁO DOANH THU EXCEL
    //  Khớp với ReportDAO.getMonthlyRows() trả Object[]:
    //  [ngay(String), soHD(int), doanhThuBan(double), doanhThuThue(double)]
    //  và getYearlyRows() trả:
    //  [thang(String), soHD(int), dtBan(double), soPT(int), dtThue(double)]
    // ============================================================
    public static void exportMonthlyExcel(
            String filePath,
            int month, int year,
            List<Object[]> rows,    // từ ReportDAO.getMonthlyRows()
            double totalBan,
            double totalThue) throws IOException {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Doanh thu thang " + month + "-" + year);

            // Styles
            CellStyle titleSt  = makeTitleStyle(wb);
            CellStyle headerSt = makeHeaderStyle(wb);
            CellStyle dataSt   = makeDataStyle(wb);
            CellStyle moneySt  = makeMoneyStyle(wb);
            CellStyle totalSt  = makeTotalStyle(wb);

            // Tiêu đề
            Row r0 = sheet.createRow(0);
            setCell(r0, 0, "BÁO CÁO DOANH THU THÁNG " + month + "/" + year, titleSt);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            sheet.createRow(1); // trống

            // Header
            Row hr = sheet.createRow(2);
            String[] headers = {"Ngày", "Số HĐ", "DT Bán (VNĐ)", "DT Thuê (VNĐ)", "Tổng (VNĐ)"};
            for (int i = 0; i < headers.length; i++)
                setCell(hr, i, headers[i], headerSt);

            // Data — rows từ getMonthlyRows(): [ngay, soHD, dtBan, dtThue]
            int rowIdx = 3;
            for (Object[] row : rows) {
                Row r = sheet.createRow(rowIdx++);
                setCell(r, 0, String.valueOf(row[0]), dataSt);            // ngày
                setCellNum(r, 1, ((Number) row[1]).doubleValue(), dataSt);// soHD
                setCellNum(r, 2, ((Number) row[2]).doubleValue(), moneySt);// dtBan
                setCellNum(r, 3, ((Number) row[3]).doubleValue(), moneySt);// dtThue
                setCellNum(r, 4,
                    ((Number) row[2]).doubleValue() +
                    ((Number) row[3]).doubleValue(), moneySt);             // tổng
            }

            // Tổng cộng
            Row tr = sheet.createRow(rowIdx);
            setCell(tr, 0, "TỔNG CỘNG", headerSt);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 1));
            setCellNum(tr, 2, totalBan,  totalSt);
            setCellNum(tr, 3, totalThue, totalSt);
            setCellNum(tr, 4, totalBan + totalThue, totalSt);

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        }
    }

    public static void exportYearlyExcel(
            String filePath,
            int year,
            List<Object[]> rows,    // từ ReportDAO.getYearlyRows()
            double totalBan,
            double totalThue) throws IOException {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Doanh thu năm " + year);

            CellStyle titleSt  = makeTitleStyle(wb);
            CellStyle headerSt = makeHeaderStyle(wb);
            CellStyle dataSt   = makeDataStyle(wb);
            CellStyle moneySt  = makeMoneyStyle(wb);
            CellStyle totalSt  = makeTotalStyle(wb);

            Row r0 = sheet.createRow(0);
            setCell(r0, 0, "BÁO CÁO DOANH THU NĂM " + year, titleSt);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            sheet.createRow(1);

            Row hr = sheet.createRow(2);
            String[] headers = {
                "Tháng", "Số HĐ", "DT Bán (VNĐ)", "Số PT", "DT Thuê (VNĐ)", "Tổng (VNĐ)"
            };
            for (int i = 0; i < headers.length; i++)
                setCell(hr, i, headers[i], headerSt);

            // Data — rows từ getYearlyRows(): [thang, soHD, dtBan, soPT, dtThue]
            int rowIdx = 3;
            for (Object[] row : rows) {
                Row r = sheet.createRow(rowIdx++);
                setCell(r, 0, String.valueOf(row[0]), dataSt);
                setCellNum(r, 1, ((Number) row[1]).doubleValue(), dataSt);
                setCellNum(r, 2, ((Number) row[2]).doubleValue(), moneySt);
                setCellNum(r, 3, ((Number) row[3]).doubleValue(), dataSt);
                setCellNum(r, 4, ((Number) row[4]).doubleValue(), moneySt);
                setCellNum(r, 5,
                    ((Number) row[2]).doubleValue() +
                    ((Number) row[4]).doubleValue(), moneySt);
            }

            Row tr = sheet.createRow(rowIdx);
            setCell(tr, 0, "TỔNG CỘNG", headerSt);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 2));
            setCellNum(tr, 3, totalBan,  totalSt);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 3, 4));
            setCellNum(tr, 5, totalBan + totalThue, totalSt);

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        }
    }

    // ── Helpers Excel ──────────────────────────────────────────
    private static void setCell(Row row, int col, String val, CellStyle st) {
        org.apache.poi.ss.usermodel.Cell c = row.createCell(col);
        c.setCellValue(val);
        c.setCellStyle(st);
    }

    private static void setCellNum(Row row, int col, double val, CellStyle st) {
        org.apache.poi.ss.usermodel.Cell c = row.createCell(col);
        c.setCellValue(val);
        c.setCellStyle(st);
    }

    private static CellStyle makeTitleStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 15);
        st.setFont(f);
        st.setAlignment(HorizontalAlignment.CENTER);
        return st;
    }

    private static CellStyle makeHeaderStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setBold(true);
        st.setFont(f);
        st.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        st.setAlignment(HorizontalAlignment.CENTER);
        setBorder(st);
        return st;
    }

    private static CellStyle makeDataStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle();
        setBorder(st);
        return st;
    }

    private static CellStyle makeMoneyStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle();
        setBorder(st);
        DataFormat fmt = wb.createDataFormat();
        st.setDataFormat(fmt.getFormat("#,##0"));
        st.setAlignment(HorizontalAlignment.RIGHT);
        return st;
    }

    private static CellStyle makeTotalStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.RED.getIndex());
        st.setFont(f);
        DataFormat fmt = wb.createDataFormat();
        st.setDataFormat(fmt.getFormat("#,##0"));
        st.setAlignment(HorizontalAlignment.RIGHT);
        setBorder(st);
        return st;
    }

    private static void setBorder(CellStyle st) {
        st.setBorderTop(BorderStyle.THIN);
        st.setBorderBottom(BorderStyle.THIN);
        st.setBorderLeft(BorderStyle.THIN);
        st.setBorderRight(BorderStyle.THIN);
    }
}
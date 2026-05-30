package otkhongluong.gamestoremanagement.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
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

    private static final DateTimeFormatter DT_FMT   =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT  =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_FMT  =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // Màu chủ đạo (tím)
    private static final DeviceRgb COLOR_HEADER = new DeviceRgb(155, 135, 245);
    private static final DeviceRgb COLOR_ROW    = new DeviceRgb(245, 242, 255);
    private static final DeviceRgb COLOR_TOTAL  = new DeviceRgb(220, 53, 69);
    private static final DeviceRgb COLOR_ACCENT = new DeviceRgb(130, 90, 230);

    // ============================================================
    //  Mở hộp thoại chọn nơi lưu
    // ============================================================
    public static String chooseFilePath(JComponent parent,
                                        String extension,
                                        String description) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu file " + extension.toUpperCase());
        fc.setSelectedFile(new File("export_" +
            LocalDateTime.now().format(FILE_FMT) + "." + extension));
        fc.setFileFilter(
            new javax.swing.filechooser.FileNameExtensionFilter(description, extension));
        if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (!path.endsWith("." + extension)) path += "." + extension;
            return path;
        }
        return null;
    }

    // ============================================================
    //  Font tiếng Việt (Arial nhúng)
    // ============================================================
    private static PdfFont getFont() throws IOException {
        try {
            // Windows
            return PdfFontFactory.createFont(
                "c:/windows/fonts/arial.ttf",
                "Identity-H",
                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (IOException e) {
            // Mac / Linux: để arial.ttf trong resources
            byte[] bytes = ExportUtil.class
                .getResourceAsStream("/arial.ttf").readAllBytes();
            return PdfFontFactory.createFont(
                bytes, "Identity-H",
                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        }
    }

    // ============================================================
    //  EXPORT HÓA ĐƠN PDF  (signature mới — có NV + điểm giảm)
    //
    //  items: [tenGame, loaiSP, soLuong, donGia, thanhTien]
    // ============================================================
    public static void exportInvoicePDF(
            String filePath,
            int    maHD,
            String tenKhachHang,
            String soDienThoai,
            int    maNV,            // THÊM MỚI
            String tenNhanVien,     // THÊM MỚI
            LocalDateTime ngayLap,
            String trangThai,
            List<String[]> items,
            double tongGoc,         // tổng giá gốc (trước giảm)
            double diemSuDung,      // số điểm đã dùng  (0 nếu không có)
            double tienGiam,        // tiền được giảm từ điểm (0 nếu không có)
            double tongPhaiTra      // tongGoc - tienGiam
    ) throws IOException {

        PdfWriter  writer   = new PdfWriter(filePath);
        PdfDocument pdf     = new PdfDocument(writer);
        Document   document = new Document(pdf, PageSize.A4);
        document.setMargins(36, 36, 36, 36);

        PdfFont font = getFont();

        // ── Tiêu đề ──────────────────────────────────────────────
        document.add(new Paragraph("GAME STORE MANAGEMENT")
            .setFont(font).setFontSize(16).setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(COLOR_ACCENT));

        document.add(new Paragraph("HÓA ĐƠN BÁN HÀNG")
            .setFont(font).setFontSize(13).setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(4));

        // Đường kẻ ngang
        document.add(new LineSeparator(
            new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f))
            .setMarginBottom(8));

        // ── Thông tin chung (2 cột: trái/phải) ───────────────────
        float[] infoWidths = {UnitValue.createPercentValue(50).getValue(),
                              UnitValue.createPercentValue(50).getValue()};
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
            .useAllAvailableWidth().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

        // Cột trái: khách hàng
        Cell leftCell = new Cell()
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .add(infoRow(font, "Mã HĐ",      "HD" + String.format("%04d", maHD)))
            .add(infoRow(font, "Khách hàng", nvl(tenKhachHang, "Khách lẻ")))
            .add(infoRow(font, "SĐT",        nvl(soDienThoai,  "---")));

        // Cột phải: nhân viên + ngày
        Cell rightCell = new Cell()
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .add(infoRow(font, "Mã NV",      maNV > 0 ? "NV" + String.format("%03d", maNV) : "---"))
            .add(infoRow(font, "Nhân viên",  nvl(tenNhanVien, "---")))
            .add(infoRow(font, "Ngày lập",   ngayLap != null ? ngayLap.format(DT_FMT) : "---"))
            .add(infoRow(font, "Trạng thái", nvl(trangThai, "---")));

        infoTable.addCell(leftCell);
        infoTable.addCell(rightCell);
        document.add(infoTable);
        document.add(new Paragraph(" ").setFontSize(4));

        // ── Bảng chi tiết ────────────────────────────────────────
        float[] cols = {3f, 1.2f, 0.7f, 1.8f, 1.8f};
        Table table = new Table(UnitValue.createPercentArray(cols)).useAllAvailableWidth();

        String[] headers = {"Tên Game", "Loại SP", "SL", "Đơn giá (VNĐ)", "Thành tiền (VNĐ)"};
        for (String h : headers) {
            table.addHeaderCell(headerCell(h, font));
        }

        boolean shade = false;
        for (String[] row : items) {
            DeviceRgb bg = shade ? COLOR_ROW : null;
            table.addCell(dataCell(row[0], font, TextAlignment.LEFT,  bg));
            table.addCell(dataCell(row[1], font, TextAlignment.CENTER,bg));
            table.addCell(dataCell(row[2], font, TextAlignment.CENTER,bg));
            table.addCell(dataCell(row[3], font, TextAlignment.RIGHT, bg));
            table.addCell(dataCell(row[4], font, TextAlignment.RIGHT, bg));
            shade = !shade;
        }
        document.add(table);
        document.add(new Paragraph(" ").setFontSize(6));

        // ── Tổng kết ─────────────────────────────────────────────
        Table sumTable = new Table(UnitValue.createPercentArray(new float[]{3f, 2f}))
            .useAllAvailableWidth().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

        // Tổng gốc
        sumTable.addCell(sumLabelCell("Tổng giá gốc:", font));
        sumTable.addCell(sumValueCell(FormatUtil.formatTien(tongGoc), font, false));

        // Điểm giảm — chỉ hiện nếu có dùng điểm
        if (diemSuDung > 0) {
            sumTable.addCell(sumLabelCell(
                "Điểm sử dụng: " + (int) diemSuDung + " điểm", font));
            sumTable.addCell(sumValueCell("- " + FormatUtil.formatTien(tienGiam), font, false));
        }

        // Dòng tổng phải trả — luôn hiện
        sumTable.addCell(sumLabelCell("TỔNG PHẢI TRẢ:", font).setBold());
        sumTable.addCell(sumValueCell(FormatUtil.formatTien(tongPhaiTra), font, true));

        document.add(sumTable);

        document.add(new Paragraph(" ").setFontSize(6));
        document.add(new LineSeparator(
            new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f))
            .setMarginBottom(8));
        document.add(new Paragraph("Cảm ơn quý khách! Hẹn gặp lại.")
            .setFont(font).setFontSize(10).setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(COLOR_ACCENT)
            .setMarginTop(6));

        document.close();
    }

    // ============================================================
    //  EXPORT PHIẾU THUÊ PDF  (signature mới — đầy đủ các khoản)
    //
    //  items: [maCD, tenGame, trangThaiCD, donGiaThue]
    // ============================================================
    public static void exportRentalPDF(
            String filePath,
            int    maPT,
            String tenKhachHang,
            String soDienThoai,     // FIX: trước bị null
            int    maNV,            // THÊM MỚI
            String tenNhanVien,     // FIX: trước bị null
            LocalDateTime ngayThue,
            LocalDateTime ngayTraDuKien,
            LocalDateTime ngayTraThucTe,
            double tienCoc,
            double tienThueBanDau,  // tổng DonGiaThue
            double diemSuDung,      // số điểm đã trừ khi thuê  (0 nếu không)
            double tienGiamDiem,    // tiền được giảm từ điểm    (0 nếu không)
            double tienGiaHan,      // phí gia hạn đã đóng        (0 nếu không)
            double tienPhatTreHan,  // tiền phạt trễ hạn          (0 nếu không)
            double tienHuHong,      // chi phí hư hỏng            (0 nếu không)
            String trangThai,
            List<String[]> items
    ) throws IOException {

        PdfWriter  writer   = new PdfWriter(filePath);
        PdfDocument pdf     = new PdfDocument(writer);
        Document   document = new Document(pdf, PageSize.A4);
        document.setMargins(36, 36, 36, 36);

        PdfFont font = getFont();

        // ── Tiêu đề ──────────────────────────────────────────────
        document.add(new Paragraph("GAME STORE MANAGEMENT")
            .setFont(font).setFontSize(16).setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(COLOR_ACCENT));

        document.add(new Paragraph("PHIẾU THUÊ ĐĨA")
            .setFont(font).setFontSize(13).setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(4));

        document.add(new LineSeparator(
            new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f))
            .setMarginBottom(8));

        // ── Thông tin chung (2 cột) ───────────────────────────────
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1f, 1f}))
            .useAllAvailableWidth().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

        Cell leftCell = new Cell()
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .add(infoRow(font, "Mã phiếu",   "PT" + String.format("%04d", maPT)))
            .add(infoRow(font, "Khách hàng", nvl(tenKhachHang, "---")))
            .add(infoRow(font, "SĐT",        nvl(soDienThoai,  "---")))  // FIX
            .add(infoRow(font, "Trạng thái", nvl(trangThai,    "---")));

        Cell rightCell = new Cell()
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .add(infoRow(font, "Mã NV",     maNV > 0 ? "NV" + String.format("%03d", maNV) : "---"))  // THÊM
            .add(infoRow(font, "Nhân viên", nvl(tenNhanVien, "---")))    // FIX
            .add(infoRow(font, "Ngày thuê",    ngayThue       != null ? ngayThue.format(DT_FMT)       : "---"))
            .add(infoRow(font, "Ngày trả DK",  ngayTraDuKien  != null ? ngayTraDuKien.format(DT_FMT)  : "---"))
            .add(infoRow(font, "Ngày trả TT",  ngayTraThucTe  != null ? ngayTraThucTe.format(DT_FMT)  : "Chưa trả"));

        infoTable.addCell(leftCell);
        infoTable.addCell(rightCell);
        document.add(infoTable);
        document.add(new Paragraph(" ").setFontSize(4));

        // ── Bảng chi tiết đĩa ────────────────────────────────────
        float[] cols = {1.2f, 3.2f, 1.5f, 2f};
        Table table = new Table(UnitValue.createPercentArray(cols)).useAllAvailableWidth();

        for (String h : new String[]{"Mã CD", "Tên Game", "Tình trạng", "Đơn giá thuê (VNĐ)"}) {
            table.addHeaderCell(headerCell(h, font));
        }

        boolean shade = false;
        for (String[] row : items) {
            DeviceRgb bg = shade ? COLOR_ROW : null;
            table.addCell(dataCell(row[0], font, TextAlignment.LEFT,   bg));
            table.addCell(dataCell(row[1], font, TextAlignment.LEFT,   bg));
            table.addCell(dataCell(row[2], font, TextAlignment.CENTER, bg));
            table.addCell(dataCell(row[3], font, TextAlignment.RIGHT,  bg));
            shade = !shade;
        }
        document.add(table);
        document.add(new Paragraph(" ").setFontSize(6));

        // ── Tổng kết chi phí ─────────────────────────────────────
        Table sumTable = new Table(UnitValue.createPercentArray(new float[]{3f, 2f}))
            .useAllAvailableWidth().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

        // Tiền thuê gốc
        sumTable.addCell(sumLabelCell("Tiền thuê (gốc):", font));
        sumTable.addCell(sumValueCell(FormatUtil.formatTien(tienThueBanDau), font, false));

        // Điểm giảm (nếu có)
        if (diemSuDung > 0) {
            sumTable.addCell(sumLabelCell(
                "Giảm điểm (" + (int) diemSuDung + " điểm):", font));
            sumTable.addCell(sumValueCell("- " + FormatUtil.formatTien(tienGiamDiem), font, false));
        }

        // Tiền thuê sau giảm điểm
        double tienThueNetDiem = Math.max(0, tienThueBanDau - tienGiamDiem);
        if (diemSuDung > 0) {
            sumTable.addCell(sumLabelCell("Tiền thuê (sau giảm):", font));
            sumTable.addCell(sumValueCell(FormatUtil.formatTien(tienThueNetDiem), font, false));
        }

        // Tiền cọc
        sumTable.addCell(sumLabelCell("Tiền cọc:", font));
        sumTable.addCell(sumValueCell(FormatUtil.formatTien(tienCoc), font, false));

        // Phí phát sinh: gộp gia hạn + trễ hạn + hư hỏng thành 1 dòng (nếu có)
        double tienPhatSinh = tienGiaHan + tienPhatTreHan + tienHuHong;
        if (tienPhatSinh > 0) {
            // Xây nhãn động: liệt kê những khoản nào > 0
            StringBuilder label = new StringBuilder("Phí phát sinh (");
            if (tienGiaHan    > 0) label.append("Gia hạn");
            if (tienPhatTreHan > 0) {
                if (tienGiaHan > 0) label.append(" + ");
                label.append("Trễ hạn");
            }
            if (tienHuHong > 0) {
                if (tienGiaHan > 0 || tienPhatTreHan > 0) label.append(" + ");
                label.append("Hư hỏng");
            }
            label.append("):");
            sumTable.addCell(sumLabelCell(label.toString(), font));
            sumTable.addCell(sumValueCell(FormatUtil.formatTien(tienPhatSinh), font, false));
        }

        // Tổng phải thanh toán
        double tongCuoi = tienThueNetDiem + tienPhatSinh;
        sumTable.addCell(sumLabelCell("TỔNG THANH TOÁN:", font).setBold());
        sumTable.addCell(sumValueCell(FormatUtil.formatTien(tongCuoi), font, true));

        document.add(sumTable);

        document.add(new Paragraph(" ").setFontSize(6));
        document.add(new LineSeparator(
            new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f))
            .setMarginBottom(8));
        document.add(new Paragraph("Cảm ơn quý khách! Hẹn gặp lại.")
            .setFont(font).setFontSize(10).setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(COLOR_ACCENT)
            .setMarginTop(6));

        document.close();
    }

    // ============================================================
    //  EXPORT BÁO CÁO DOANH THU EXCEL — giữ nguyên, không đổi
    // ============================================================
    public static void exportMonthlyExcel(
            String filePath,
            int month, int year,
            List<Object[]> rows,
            double totalBan,
            double totalThue) throws IOException {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Doanh thu thang " + month + "-" + year);

            CellStyle titleSt  = makeTitleStyle(wb);
            CellStyle headerSt = makeHeaderStyle(wb);
            CellStyle dataSt   = makeDataStyle(wb);
            CellStyle moneySt  = makeMoneyStyle(wb);
            CellStyle totalSt  = makeTotalStyle(wb);

            Row r0 = sheet.createRow(0);
            setCell(r0, 0, "BÁO CÁO DOANH THU THÁNG " + month + "/" + year, titleSt);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
            sheet.createRow(1);

            Row hr = sheet.createRow(2);
            String[] headers = {"Ngày", "Số HĐ", "DT Bán (VNĐ)", "DT Thuê (VNĐ)", "Tổng (VNĐ)"};
            for (int i = 0; i < headers.length; i++) setCell(hr, i, headers[i], headerSt);

            int rowIdx = 3;
            for (Object[] row : rows) {
                Row r = sheet.createRow(rowIdx++);
                setCell(r, 0, String.valueOf(row[0]), dataSt);
                setCellNum(r, 1, ((Number) row[1]).doubleValue(), dataSt);
                setCellNum(r, 2, ((Number) row[2]).doubleValue(), moneySt);
                setCellNum(r, 3, ((Number) row[3]).doubleValue(), moneySt);
                setCellNum(r, 4, ((Number) row[2]).doubleValue()
                               + ((Number) row[3]).doubleValue(), moneySt);
            }

            Row tr = sheet.createRow(rowIdx);
            setCell(tr, 0, "TỔNG CỘNG", headerSt);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 1));
            setCellNum(tr, 2, totalBan,  totalSt);
            setCellNum(tr, 3, totalThue, totalSt);
            setCellNum(tr, 4, totalBan + totalThue, totalSt);

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            try (FileOutputStream fos = new FileOutputStream(filePath)) { wb.write(fos); }
        }
    }

    public static void exportYearlyExcel(
            String filePath,
            int year,
            List<Object[]> rows,
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
            String[] headers = {"Tháng","Số HĐ","DT Bán (VNĐ)","Số PT","DT Thuê (VNĐ)","Tổng (VNĐ)"};
            for (int i = 0; i < headers.length; i++) setCell(hr, i, headers[i], headerSt);

            int rowIdx = 3;
            for (Object[] row : rows) {
                Row r = sheet.createRow(rowIdx++);
                setCell(r, 0, String.valueOf(row[0]), dataSt);
                setCellNum(r, 1, ((Number) row[1]).doubleValue(), dataSt);
                setCellNum(r, 2, ((Number) row[2]).doubleValue(), moneySt);
                setCellNum(r, 3, ((Number) row[3]).doubleValue(), dataSt);
                setCellNum(r, 4, ((Number) row[4]).doubleValue(), moneySt);
                setCellNum(r, 5, ((Number) row[2]).doubleValue()
                               + ((Number) row[4]).doubleValue(), moneySt);
            }

            Row tr = sheet.createRow(rowIdx);
            setCell(tr, 0, "TỔNG CỘNG", headerSt);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 1));
            setCellNum(tr, 2, totalBan,  totalSt);
            setCellNum(tr, 4, totalThue, totalSt);
            setCellNum(tr, 5, totalBan + totalThue, totalSt);

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            try (FileOutputStream fos = new FileOutputStream(filePath)) { wb.write(fos); }
        }
    }

    // ============================================================
    //  iText helpers — cell builders
    // ============================================================

    /** Dòng info: "Label : value" */
    private static Paragraph infoRow(PdfFont font, String label, String value) {
        return new Paragraph()
            .add(new Text(label + " : ").setFont(font).setFontSize(10).setBold())
            .add(new Text(value).setFont(font).setFontSize(10))
            .setMarginBottom(2);
    }

    /** Header cell (nền tím nhạt, chữ trắng đậm) */
    private static Cell headerCell(String text, PdfFont font) {
        return new Cell()
            .add(new Paragraph(text).setFont(font).setFontSize(10).setBold()
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE))
            .setBackgroundColor(COLOR_HEADER)
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(5);
    }

    /** Data cell với nền xen kẽ tùy chọn */
    private static Cell dataCell(String text, PdfFont font,
                                 TextAlignment align, DeviceRgb bg) {
        Cell c = new Cell()
            .add(new Paragraph(text == null ? "" : text).setFont(font).setFontSize(9))
            .setTextAlignment(align)
            .setPaddingTop(4).setPaddingBottom(4)
            .setPaddingLeft(6).setPaddingRight(6)
            .setBorderLeft(new SolidBorder(0.3f))
            .setBorderRight(new SolidBorder(0.3f))
            .setBorderTop(new SolidBorder(0.3f))
            .setBorderBottom(new SolidBorder(0.3f));
        if (bg != null) c.setBackgroundColor(bg);
        return c;
    }

    /** Label tổng kết (căn phải) */
    private static Cell sumLabelCell(String text, PdfFont font) {
        return new Cell()
            .add(new Paragraph(text).setFont(font).setFontSize(10))
            .setTextAlignment(TextAlignment.RIGHT)
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setPaddingTop(2).setPaddingBottom(2);
    }

    /** Value tổng kết (căn phải, đỏ nếu là dòng tổng) */
    private static Cell sumValueCell(String text, PdfFont font, boolean highlight) {
        Paragraph p = new Paragraph(text).setFont(font)
            .setFontSize(highlight ? 12 : 10);
        if (highlight) {
            p.setBold().setFontColor(COLOR_TOTAL);
        }
        return new Cell()
            .add(p)
            .setTextAlignment(TextAlignment.RIGHT)
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setPaddingTop(2).setPaddingBottom(2);
    }

    private static String nvl(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    // ============================================================
    //  Helpers Excel — giữ nguyên
    // ============================================================
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
        st.setFont(f); st.setAlignment(HorizontalAlignment.CENTER);
        return st;
    }

    private static CellStyle makeHeaderStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setBold(true); st.setFont(f);
        st.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        st.setAlignment(HorizontalAlignment.CENTER);
        setBorder(st); return st;
    }

    private static CellStyle makeDataStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle(); setBorder(st); return st;
    }

    private static CellStyle makeMoneyStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle(); setBorder(st);
        DataFormat fmt = wb.createDataFormat();
        st.setDataFormat(fmt.getFormat("#,##0"));
        st.setAlignment(HorizontalAlignment.RIGHT); return st;
    }

    private static CellStyle makeTotalStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setBold(true); f.setColor(IndexedColors.RED.getIndex()); st.setFont(f);
        DataFormat fmt = wb.createDataFormat();
        st.setDataFormat(fmt.getFormat("#,##0"));
        st.setAlignment(HorizontalAlignment.RIGHT);
        setBorder(st); return st;
    }

    private static void setBorder(CellStyle st) {
        st.setBorderTop(BorderStyle.THIN);    st.setBorderBottom(BorderStyle.THIN);
        st.setBorderLeft(BorderStyle.THIN);   st.setBorderRight(BorderStyle.THIN);
    }
}
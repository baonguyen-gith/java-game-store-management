package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.dao.CustomerDAO;
import otkhongluong.gamestoremanagement.dao.EmployeeDAO;
import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.model.Employee;
import otkhongluong.gamestoremanagement.model.Invoice;
import otkhongluong.gamestoremanagement.model.CartItem;
import otkhongluong.gamestoremanagement.model.SpRow;
import otkhongluong.gamestoremanagement.model.RentDetailData;
import otkhongluong.gamestoremanagement.service.InvoiceService;
import otkhongluong.gamestoremanagement.util.ExportUtil;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * InvoiceController — điều phối mọi nghiệp vụ liên quan đến hóa đơn.
 *
 * Bao gồm phần đã có (query/filter/sort/page) +
 * các method mới để thay thế DAO/SQL trực tiếp trong các dialog:
 *   - InvoiceAddDialog  → loadGameCatalog, loadSpCatalog, findKH, createKH,
 *                         confirmPayment, getROMLink
 *   - InvoiceEditDialog → loadWorkingItems, loadSpCatalogForEdit, findKH,
 *                         findNV, saveEditInvoice
 *   - RentDetailDialog  → loadRentDetail
 *   - TransactionDetailDialog → getHoaDonById (đã có)
 */
public class InvoiceController {

    private final InvoiceService service;
    private final CustomerDAO    khDAO;
    private final EmployeeDAO    nvDAO;

    private int[] sortState = new int[7];
    private int sortCol = -1;
    private static final int PAGE_SIZE = 8;

    public InvoiceController() {
        this.service = new InvoiceService();
        this.khDAO   = new CustomerDAO();
        this.nvDAO   = new EmployeeDAO();
    }

    public InvoiceController(InvoiceService service) {
        this.service = service;
        this.khDAO   = new CustomerDAO();
        this.nvDAO   = new EmployeeDAO();
    }

    // ================================================================
    // PHẦN ĐÃ CÓ — giữ nguyên
    // ================================================================

    public static class InvoicePageResult {
        public final List<Invoice> rows;
        public final int currentPage, totalPages, totalRows;
        public final boolean fromDateError, toDateError;

        public InvoicePageResult(List<Invoice> rows, int cur, int total, int totalRows,
                                 boolean fromErr, boolean toErr) {
            this.rows = rows; this.currentPage = cur; this.totalPages = total;
            this.totalRows = totalRows; this.fromDateError = fromErr; this.toDateError = toErr;
        }

        public boolean hasDateError() { return fromDateError || toDateError; }
    }

    public int[] getSortState() { return sortState; }
    public int getSortCol()     { return sortCol; }

    public InvoicePageResult onSortChanged(int col, String from, String to, String kw, int page) {
        sortState[col] = (sortState[col] + 1) % 3;
        sortCol = sortState[col] == 0 ? -1 : col;
        boolean asc = sortState[col] != 2;
        return query(from, to, kw, sortCol, asc, 1);
    }

    public InvoicePageResult query(String from, String to, String keyword, int page) {
        boolean asc = sortCol < 0 || sortState[sortCol] != 2;
        return query(from, to, keyword, sortCol, asc, page);
    }

    public InvoicePageResult query(String fromStr, String toStr, String keyword,
                                   int sortCol, boolean asc, int page) {
        boolean fromErr = false, toErr = false;
        LocalDate from = null, to = null;
        try { if (!fromStr.trim().isEmpty()) from = LocalDate.parse(fromStr.trim()); }
        catch (Exception e) { fromErr = true; }
        try { if (!toStr.trim().isEmpty()) to = LocalDate.parse(toStr.trim()); }
        catch (Exception e) { toErr = true; }

        if (fromErr || toErr)
            return new InvoicePageResult(new ArrayList<>(), page, 1, 0, fromErr, toErr);

        List<Invoice> filtered = getFilteredInvoices(from, to, keyword, sortCol, asc);
        int total = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        if (page > total) page = total;
        if (page < 1) page = 1;
        int f = (page - 1) * PAGE_SIZE;
        int t = Math.min(f + PAGE_SIZE, filtered.size());
        return new InvoicePageResult(filtered.subList(f, t), page, total,
                                     filtered.size(), false, false);
    }

    public List<Invoice> getAllInvoices() { return service.getAllHoaDon(); }

    public Invoice getHoaDonById(int id)  { return service.getHoaDonById(id); }

    public boolean deleteInvoice(int id)  { return service.deleteHoaDon(id); }

    public List<Invoice> getFilteredInvoices(LocalDate from, LocalDate to,
                                              String keyword, int sortCol, boolean ascending) {
        List<Invoice> all = service.getAllHoaDon();
        List<Invoice> result = all.stream()
            .filter(hd -> matchesDateRange(hd, from, to))
            .filter(hd -> matchesKeyword(hd, keyword))
            .collect(Collectors.toList());
        if (sortCol >= 0) {
            Comparator<Invoice> cmp = buildComparator(sortCol);
            if (!ascending) cmp = cmp.reversed();
            result.sort(cmp);
        }
        return result;
    }

    // ================================================================
    // CATALOG — dùng cho InvoiceAddDialog + InvoiceEditDialog
    // ================================================================

    /** Danh sách game bán được. Mỗi Object[]: {MaGame, TenGame, TheLoai, NenTang} */
    public List<Object[]> loadGameCatalog() {
        return service.loadGameCatalog();
    }

    /**
     * SP (CD+ROM) của 1 game cho InvoiceAddDialog.
     * Mỗi Object[]: {MaSP, MaCD_or_-1, loaiSP, giaBan, thongTin, available, MaGame}
     */
    public List<Object[]> loadSpCatalog(int maGame) {
        return service.loadSpCatalog(maGame);
    }

    /**
     * SP (CD+ROM) của 1 game cho popup trong InvoiceEditDialog (kèm TenGame ở index 7).
     * Mỗi Object[]: {MaSP, MaCD_or_-1, loaiSP, giaBan, thongTin, available, MaGame, TenGame}
     */
    public List<Object[]> loadSpCatalogForEdit(int maGame, String tenGame) {
        return service.loadSpCatalogForEdit(maGame, tenGame);
    }

    // ================================================================
    // KHÁCH HÀNG — InvoiceAddDialog + InvoiceEditDialog
    // ================================================================

    public Customer findKHBySDT(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) return null;
        return khDAO.findBySDT(sdt.trim());
    }

    /**
     * Tạo khách hàng mới.
     * @return ActionResult; nếu success thì Customer mới đã được insert
     */
    public ActionResult createKH(String hoTen, String sdt, String email) {
        if (hoTen == null || hoTen.trim().isEmpty())
            return ActionResult.fail("Họ và tên không được để trống!");
        Customer existing = khDAO.findBySDT(sdt.trim());
        if (existing != null)
            return ActionResult.fail("Khách hàng với SĐT " + sdt + " đã tồn tại!");

        Customer kh = new Customer();
        kh.setHoTen(hoTen.trim());
        kh.setSdt(sdt.trim());
        kh.setEmail(email == null || email.trim().isEmpty() ? null : email.trim());
        kh.setDiemTichLuy(0);
        boolean ok = khDAO.insert(kh);
        return ok ? ActionResult.ok("OK") : ActionResult.fail("Tạo khách hàng thất bại!");
    }

    public Customer findKHAfterCreate(String sdt) {
        return khDAO.findBySDT(sdt);
    }

    // ================================================================
    // NHÂN VIÊN — InvoiceEditDialog
    // ================================================================

    public Employee findNVByMa(String raw) {
        if (raw == null) return null;
        String stripped = raw.trim().replaceAll("(?i)nv", "");
        if (stripped.isEmpty()) return null;
        try { return nvDAO.findById(Integer.parseInt(stripped)); }
        catch (NumberFormatException ex) { return null; }
    }

    // ================================================================
    // TẠO HÓA ĐƠN — InvoiceAddDialog
    // ================================================================

    /**
     * Xác nhận thanh toán tạo hóa đơn mới.
     * @param maKH         -1 nếu khách vãng lai
     * @param diemThucDung số điểm thực sự dùng
     * @param cart         giỏ hàng
     * @return ActionResult; nếu success thì message = "HĐ{id}"
     */
    public ActionResult confirmPayment(int maKH, int diemThucDung,
                               List<CartItem> cart) {
        String result = service.createInvoice(maKH, diemThucDung, cart);
        if (result.startsWith("OK:"))
            return ActionResult.ok(result.substring(3));
        else
            return ActionResult.fail(result.substring(4));
    }

    public String getROMLink(int maSP) {
        return service.getROMLink(maSP);
    }

    // ================================================================
    // SỬA HÓA ĐƠN — InvoiceEditDialog
    // ================================================================

    /** Load working items kèm MaCD thực tế cho InvoiceEditDialog. */
    public List<SpRow> loadWorkingItems(int maHD) {
        return service.loadWorkingItems(maHD);
    }

    /**
     * Lưu toàn bộ thay đổi sửa hóa đơn.
     * @param newKH       null = không đổi KH
     * @param newMaNV     null = không đổi NV
     * @param newNgayLap  null = không đổi ngày
     */
    public ActionResult saveEditInvoice(int maHD,
                                Customer newKH, Integer newMaNV,
                                LocalDate newNgayLap,
                                int maKHCu, double tongCu,
                                List<SpRow> removedItems,
                                List<SpRow> addedItems,
                                List<SpRow> workingItems) {
        String result = service.saveEditInvoice(
            maHD, newKH, newMaNV, newNgayLap,
            maKHCu, tongCu, removedItems, addedItems, workingItems
        );
        if (result.startsWith("OK:"))
            return ActionResult.ok(result.substring(3));
        else
            return ActionResult.fail(result.substring(4));
    }

    // ================================================================
    // CHI TIẾT PHIẾU THUÊ — RentDetailDialog
    // ================================================================

    /**
     * Load dữ liệu đầy đủ cho RentDetailDialog.
     * @return RentDetailData hoặc null nếu không tìm thấy
     */
    public RentDetailData loadRentDetail(int maPT) {
        return service.loadRentDetail(maPT);
    }

    // ================================================================
    // PARSE NGÀY — InvoiceEditDialog (Tab 1 validate)
    // ================================================================

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Parse ngày từ chuỗi dd/MM/yyyy. @return null nếu sai định dạng */
    public LocalDate parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return LocalDate.parse(raw.trim(), FMT); }
        catch (DateTimeParseException ex) { return null; }
    }

    // ================================================================
    // INNER: ActionResult
    // ================================================================

    public static class ActionResult {
        public final boolean success;
        public final String  message;

        private ActionResult(boolean success, String message) {
            this.success = success; this.message = message;
        }

        public static ActionResult ok(String msg)   { return new ActionResult(true,  msg); }
        public static ActionResult fail(String msg) { return new ActionResult(false, msg); }
    }

    // ================================================================
    // PRIVATE HELPERS (filter/sort cho InvoicePanel)
    // ================================================================

    private boolean matchesDateRange(Invoice hd, LocalDate from, LocalDate to) {
        if (from == null && to == null) return true;
        if (hd.getNgayLap() == null) return false;
        LocalDate ngayLap = hd.getNgayLap().toLocalDate();
        if (from != null && ngayLap.isBefore(from)) return false;
        if (to   != null && ngayLap.isAfter(to))   return false;
        return true;
    }

    private boolean matchesKeyword(Invoice hd, String keyword) {
        if (keyword == null || keyword.isEmpty()) return true;
        String row = String.join(" ",
            "HD" + String.format("%03d", hd.getMaHD()),
            "NV" + String.format("%03d", hd.getMaNV()),
            nvl(hd.getTenKhachHang()),
            nvl(hd.getSoDienThoai())
        ).toLowerCase();
        return row.contains(keyword.toLowerCase());
    }

    private Comparator<Invoice> buildComparator(int col) {
        switch (col) {
            case 0: return Comparator.comparingInt(Invoice::getMaHD);
            case 1: return Comparator.comparingInt(Invoice::getMaNV);
            case 2: return Comparator.comparing(hd -> nvl(hd.getTenKhachHang()));
            case 3: return Comparator.comparing(hd -> nvl(hd.getSoDienThoai()));
            case 4: return Comparator.comparing(hd -> hd.getNgayLap() != null
                        ? hd.getNgayLap() : java.time.LocalDateTime.MIN);
            case 5: return Comparator.comparingDouble(Invoice::getTongTien);
            default: return Comparator.comparingInt(Invoice::getMaHD);
        }
    }
    
    public void exportInvoicePDF(int maHD, String filePath) throws IOException {
        Object[] data    = service.getInvoiceExportData(maHD);
        Invoice invoice  = (Invoice) data[0];
        List<String[]> items = (List<String[]>) data[1];

        ExportUtil.exportInvoicePDF(
            filePath,
            invoice.getMaHD(),
            invoice.getTenKhachHang(),
            invoice.getSoDienThoai(),
            invoice.getNgayLap(),
            invoice.getTrangThai(),
            items,
            invoice.getTongTien()
        );
    }

    private String nvl(String s) { return s == null ? "" : s; }
}
package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.dao.CustomerDAO;
import otkhongluong.gamestoremanagement.dao.DiscDAO;
import otkhongluong.gamestoremanagement.dao.EmployeeDAO;
import otkhongluong.gamestoremanagement.model.CTPhieuThue;
import otkhongluong.gamestoremanagement.model.RentDetailData;
import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.model.Employee;
import otkhongluong.gamestoremanagement.model.RentalOrder;
import otkhongluong.gamestoremanagement.service.RentalService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RentController — điều phối mọi nghiệp vụ phiếu thuê.
 *
 * Phục vụ toàn bộ 5 dialog:
 *   RentAddDialog    → loadAvailableCD, findKHBySDT, createKH, updateKHCCCD, createPhieuThue, logDiemThue
 *   RentDetailDialog → loadRentDetail
 *   RentEditDialog   → findKHBySDT, findNVByMa, saveEditRental
 *   RentExtendDialog → searchRentals, getById, extendRental
 *   RentReturnDialog → searchRentals, getById, loadDiemDaTru, returnCD
 *
 * View không import bất kỳ DAO, Connection, SQL nào.
 */
public class RentController {

    /* ── Tầng dưới ── */
    private final RentalService service;
    private final CustomerDAO   khDAO;
    private final EmployeeDAO   nvDAO;
    private final DiscDAO       cdDAO;

    /* ── Phân trang & sắp xếp (InvoicePanel / RentPanel) ── */
    private int[] sortState = new int[7];
    private int   sortCol   = -1;
    private static final int PAGE_SIZE = 8;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ══════════════════════════════════════════════════════
       CONSTRUCTOR
    ══════════════════════════════════════════════════════ */
    public RentController() {
        this.service = new RentalService();
        this.khDAO   = new CustomerDAO();
        this.nvDAO   = new EmployeeDAO();
        this.cdDAO   = new DiscDAO();
    }

    /** Constructor dùng để inject service trong test. */
    public RentController(RentalService service) {
        this.service = service;
        this.khDAO   = new CustomerDAO();
        this.nvDAO   = new EmployeeDAO();
        this.cdDAO   = new DiscDAO();
    }

    /* ══════════════════════════════════════════════════════
       PHẦN ĐÃ CÓ — RentPanel (giữ nguyên, chỉnh tên method)
    ══════════════════════════════════════════════════════ */

    public static class RentPageResult {
        public final List<RentalOrder> rows;
        public final int  currentPage, totalPages, totalRows;
        public final boolean fromDateError, toDateError;

        public RentPageResult(List<RentalOrder> rows, int currentPage,
                              int totalPages, int totalRows,
                              boolean fromDateError, boolean toDateError) {
            this.rows          = rows;
            this.currentPage   = currentPage;
            this.totalPages    = totalPages;
            this.totalRows     = totalRows;
            this.fromDateError = fromDateError;
            this.toDateError   = toDateError;
        }
        public boolean hasDateError() { return fromDateError || toDateError; }
    }

    public int[] getSortState() { return sortState; }
    public int   getSortCol()   { return sortCol; }

    public RentPageResult onSortChanged(int col, String from, String to, String kw) {
        sortState[col] = (sortState[col] + 1) % 3;
        sortCol = sortState[col] == 0 ? -1 : col;
        for (int i = 0; i < sortState.length; i++)
            if (i != col) sortState[i] = 0;
        return query(from, to, kw, 1);
    }

    public RentPageResult query(String fromStr, String toStr, String keyword, int page) {
        boolean fromErr = false, toErr = false;
        LocalDate from = null, to = null;

        try { if (!fromStr.trim().isEmpty()) from = parseDate(fromStr); }
        catch (Exception e) { fromErr = true; }
        try { if (!toStr.trim().isEmpty())   to   = parseDate(toStr);   }
        catch (Exception e) { toErr = true; }

        if (fromErr || toErr)
            return new RentPageResult(new ArrayList<>(), page, 1, 0, fromErr, toErr);

        boolean asc = sortCol < 0 || sortState[sortCol] != 2;
        List<RentalOrder> filtered = getFilteredRentals(from, to, keyword, sortCol, asc);

        int total = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        page = Math.min(Math.max(page, 1), total);
        int f = (page - 1) * PAGE_SIZE;
        int t = Math.min(f + PAGE_SIZE, filtered.size());

        return new RentPageResult(filtered.subList(f, t), page, total,
                                  filtered.size(), false, false);
    }

    public List<RentalOrder> getAllRentals() { return service.getAll(); }

    public boolean deleteRental(int id) { return service.deletePhieuThue(id); }

    public List<RentalOrder> getFilteredRentals(LocalDate from, LocalDate to,
                                                 String keyword, int sortCol,
                                                 boolean ascending) {
        List<RentalOrder> result = service.getAll().stream()
            .filter(pt -> matchesDateRange(pt, from, to))
            .filter(pt -> matchesKeyword(pt, keyword))
            .collect(Collectors.toList());

        if (sortCol >= 0) {
            Comparator<RentalOrder> cmp = buildComparator(sortCol);
            if (!ascending) cmp = cmp.reversed();
            result.sort(cmp);
        }
        return result;
    }

    /* ══════════════════════════════════════════════════════
       PHẦN MỚI — dùng cho 5 dialog
    ══════════════════════════════════════════════════════ */

    // ──────────────────────────────────────────────────────
    // RentAddDialog
    // ──────────────────────────────────────────────────────

    /**
     * Load danh sách CD sẵn sàng cho thuê.
     * ▼ Thay: cdDAO.getAllAvailableCD() trực tiếp trong Dialog
     *   bằng: controller.loadAvailableCD()
     * Mỗi Object[]: {MaCD(int), TenGame(String), GiaThueNgay(double)}
     */
    public List<Object[]> loadAvailableCD() {
        return cdDAO.getAllAvailableCD();
    }

    /**
     * Tìm khách hàng theo SĐT.
     * ▼ Thay: khDAO.findBySDT(sdt) trực tiếp trong Dialog
     */
    public Customer findKHBySDT(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) return null;
        return khDAO.findBySDT(sdt.trim());
    }

    /**
     * Tạo khách hàng mới (có CCCD).
     * ▼ Thay: khDAO.insert(kh) + khDAO.findBySDT() trực tiếp trong Dialog
     * @return ActionResult; nếu success thì message = "OK", data = Customer mới
     */
    public ActionResult createKH(String hoTen, String sdt, String cccd) {
        if (hoTen == null || hoTen.trim().isEmpty())
            return ActionResult.fail("Họ và tên không được để trống!");
        if (cccd == null || !cccd.trim().matches("\\d{9}|\\d{12}"))
            return ActionResult.fail("CCCD phải có 9 hoặc 12 chữ số!");
        if (khDAO.findBySDT(sdt.trim()) != null)
            return ActionResult.fail("Khách hàng với SĐT " + sdt + " đã tồn tại!");

        Customer kh = new Customer();
        kh.setHoTen(hoTen.trim());
        kh.setSdt(sdt.trim());
        kh.setCccd(cccd.trim());
        kh.setDiemTichLuy(0);
        boolean ok = khDAO.insert(kh);
        return ok ? ActionResult.ok("Tạo khách hàng thành công!") : ActionResult.fail("Tạo khách hàng thất bại!");
    }

    /**
     * Cập nhật tên + CCCD cho khách hàng đã tồn tại nhưng chưa có CCCD.
     * ▼ Thay: existing.setXxx(); khDAO.update(existing) trực tiếp trong Dialog
     */
    public ActionResult updateKHCCCD(String sdt, String hoTen, String cccd) {
        Customer existing = khDAO.findBySDT(sdt.trim());
        if (existing == null)
            return ActionResult.fail("Không tìm thấy khách hàng SĐT: " + sdt);
        if (hoTen == null || hoTen.trim().isEmpty())
            return ActionResult.fail("Họ và tên không được để trống!");
        if (cccd == null || !cccd.trim().matches("\\d{9}|\\d{12}"))
            return ActionResult.fail("CCCD phải có 9 hoặc 12 chữ số!");

        existing.setHoTen(hoTen.trim());
        existing.setCccd(cccd.trim());
        boolean ok = khDAO.update(existing);
        return ok ? ActionResult.ok("Cập nhật CCCD thành công!") : ActionResult.fail("Cập nhật CCCD thất bại!");
    }

    /**
     * Tạo phiếu thuê mới + ghi log điểm trừ.
     * ▼ Thay: service.createPhieuThue(pt) + SQL INSERT DIEM_LICHSU trực tiếp trong Dialog
     * @param diemThucDung số điểm thực sự dùng (0 nếu không dùng)
     * @return ActionResult; nếu success thì message chứa thông tin phiếu
     */
    public ActionResult createPhieuThue(RentalOrder pt, int diemThucDung) {
        boolean ok = service.createPhieuThue(pt);
        if (!ok) return ActionResult.fail(
            "CD" + pt.getDanhSachChiTiet().get(0).getMaCD() + 
            " không còn sẵn sàng!\n" +
            "Có thể đã được bán hoặc thuê bởi giao dịch khác.\n" +
            "Vui lòng nhấn \"Làm mới\" ở Bước 1 để cập nhật danh sách.");

        // Trừ điểm + ghi log — uỷ quyền cho Service
        if (pt.getMaKH() > 0 && diemThucDung > 0) {
            String result = service.deductPointForRental(
                pt.getMaKH(), diemThucDung, pt.getMaPT());
            if (result != null) {
                // Không fail toàn bộ giao dịch, nhưng trả về cảnh báo
                return ActionResult.ok("Tạo phiếu thuê thành công nhưng trừ điểm thất bại:\n" + result);
            }
        }
        return ActionResult.ok("Tạo phiếu thuê thành công!");
    }

    // ──────────────────────────────────────────────────────
    // RentDetailDialog
    // ──────────────────────────────────────────────────────

    /**
     * Load đầy đủ dữ liệu cho RentDetailDialog.
     * ▼ Thay: DBConnection + raw SQL trực tiếp trong Dialog
     *   bằng: controller.loadRentDetail(maPT)
     * @return RentDetailData hoặc null nếu không tìm thấy
     */
    public RentDetailData loadRentDetail(int maPT) {
        return service.loadRentDetail(maPT);
    }

    // ──────────────────────────────────────────────────────
    // RentEditDialog
    // ──────────────────────────────────────────────────────

    /**
     * Tìm nhân viên theo mã (chuỗi "NV001" hoặc "1").
     * ▼ Thay: nvDAO.findById(maNV) trực tiếp trong Dialog
     */
    public Employee findNVByMa(String raw) {
        if (raw == null) return null;
        String stripped = raw.trim().replaceAll("(?i)nv", "");
        if (stripped.isEmpty()) return null;
        try { return nvDAO.findById(Integer.parseInt(stripped)); }
        catch (NumberFormatException ex) { return null; }
    }

    /**
     * Parse ngày từ chuỗi dd/MM/yyyy.
     * @return null nếu sai định dạng
     */
    public LocalDate parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try { return LocalDate.parse(raw.trim(), FMT); }
        catch (Exception ex) { return null; }
    }

    /**
     * Lưu thay đổi sửa phiếu thuê (đổi KH, NV, ngày trả).
     * ▼ Thay: ptDAO.updateKhachHangVaNhanVien + truDiem/congDiem + updateNgayTraVaDonGia
     *         trực tiếp trong Dialog
     * @return ActionResult
     */
    public ActionResult saveEditRental(int maPT,
                                       Customer newKH,   // null = không đổi KH
                                       Employee newNV,   // null = không đổi NV
                                       LocalDateTime newNgayTra, // null = không đổi ngày
                                       int maKHCu, int maNVCu) {
        String result = service.saveEditRental(maPT, newKH, newNV, newNgayTra, maKHCu, maNVCu);
        return result.startsWith("OK")
            ? ActionResult.ok(result.substring(3).trim())
            : ActionResult.fail(result.substring(4).trim());
    }

    // ──────────────────────────────────────────────────────
    // RentExtendDialog + RentReturnDialog — tìm kiếm
    // ──────────────────────────────────────────────────────

    /**
     * Tìm danh sách phiếu thuê đang hoạt động theo SĐT.
     * ▼ Thay: service.getAll() + stream filter trực tiếp trong cả 2 dialog
     */
    public List<RentalOrder> searchActiveRentalsBySdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) return new ArrayList<>();
        return service.getAll().stream()
            .filter(pt -> pt.getSoDienThoai() != null
                       && pt.getSoDienThoai().contains(sdt.trim())
                       && "DangThue".equalsIgnoreCase(pt.getTrangThai()))
            .collect(Collectors.toList());
    }

    /**
     * Lấy phiếu thuê đầy đủ theo mã.
     * ▼ Thay: service.getById(maPT) trực tiếp trong Dialog
     */
    public RentalOrder getById(int maPT) {
        return service.getById(maPT);
    }

    // ──────────────────────────────────────────────────────
    // RentExtendDialog
    // ──────────────────────────────────────────────────────

    /**
     * Gia hạn phiếu thuê.
     * ▼ Thay: service.extendRental(...) trực tiếp trong Dialog
     */
    public ActionResult extendRental(int maPT, int soNgay, double phatTre, double phiGiaHan) {
        boolean ok = service.extendRental(maPT, soNgay, phatTre, phiGiaHan);
        return ok ? ActionResult.ok("Gia hạn thành công!")
                  : ActionResult.fail("Gia hạn thất bại! Vui lòng thử lại.");
    }

    // ──────────────────────────────────────────────────────
    // RentReturnDialog
    // ──────────────────────────────────────────────────────

    /**
     * Load số điểm đã trừ khi tạo phiếu thuê (từ DIEM_LICHSU).
     * ▼ Thay: SQL SELECT trực tiếp trong Dialog
     */
    public int loadDiemDaTru(int maPT) {
        return service.loadDiemDaTru(maPT);
    }

    /**
     * Trả CD: cập nhật phiếu thuê + cộng điểm tích lũy + ghi log.
     * ▼ Thay: service.returnCD() + SQL UPDATE KHACHHANG + INSERT DIEM_LICHSU trực tiếp trong Dialog
     * @param diemTichLuy số điểm cộng cho KH (= tienThueGoc / 100_000)
     * @return ActionResult
     */
    public ActionResult returnCD(int maPT, LocalDateTime ngayTraThucTe,
                                 double chiPhiHuHong, int maKH, int diemTichLuy) {
        String result = service.returnCDFull(maPT, ngayTraThucTe, chiPhiHuHong, maKH, diemTichLuy);
        return result.startsWith("OK")
            ? ActionResult.ok(result.substring(3).trim())
            : ActionResult.fail(result.substring(4).trim());
    }

    /* ══════════════════════════════════════════════════════
       INNER: ActionResult
    ══════════════════════════════════════════════════════ */
    public static class ActionResult {
        public final boolean success;
        public final String  message;

        private ActionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ActionResult ok(String msg)   { return new ActionResult(true,  msg); }
        public static ActionResult fail(String msg) { return new ActionResult(false, msg); }
    }

    /* ══════════════════════════════════════════════════════
       PRIVATE HELPERS (filter / sort cho RentPanel)
    ══════════════════════════════════════════════════════ */
    private boolean matchesDateRange(RentalOrder pt, LocalDate from, LocalDate to) {
        if (from == null && to == null) return true;
        if (pt.getNgayThue() == null)   return false;
        LocalDate d = pt.getNgayThue().toLocalDate();
        if (from != null && d.isBefore(from)) return false;
        if (to   != null && d.isAfter(to))   return false;
        return true;
    }

    private boolean matchesKeyword(RentalOrder pt, String keyword) {
        if (keyword == null || keyword.isEmpty()) return true;
        String row = String.join(" ",
            "PT" + String.format("%04d", pt.getMaPT()),
            "NV" + String.format("%03d", pt.getMaNV()),
            nvl(pt.getTenKhachHang()),
            nvl(pt.getSoDienThoai()),
            pt.getNgayThue()      != null ? pt.getNgayThue().toString()      : "",
            pt.getNgayTraDuKien() != null ? pt.getNgayTraDuKien().toString() : "",
            nvl(pt.getTrangThai())
        ).toLowerCase();
        return row.contains(keyword.toLowerCase());
    }

    private Comparator<RentalOrder> buildComparator(int col) {
        switch (col) {
            case 0: return Comparator.comparingInt(RentalOrder::getMaPT);
            case 1: return Comparator.comparingInt(RentalOrder::getMaNV);
            case 2: return Comparator.comparing(pt -> nvl(pt.getTenKhachHang()));
            case 3: return Comparator.comparing(pt -> nvl(pt.getSoDienThoai()));
            case 4: return Comparator.comparing(pt ->
                        pt.getNgayThue() != null ? pt.getNgayThue() : LocalDateTime.MIN);
            case 5: return Comparator.comparing(pt ->
                        pt.getNgayTraDuKien() != null ? pt.getNgayTraDuKien() : LocalDateTime.MIN);
            case 6: return Comparator.comparing(pt -> nvl(pt.getTrangThai()));
            default: return Comparator.comparingInt(RentalOrder::getMaPT);
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }
    
}
package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Employee;
import otkhongluong.gamestoremanagement.service.EmployeeService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller trung gian giữa EmployeePanel / EmployeeDialog và EmployeeService.
 * ✅ Không giữ tham chiếu View, không hiển thị JOptionPane.
 * ✅ Trả về SaveResult để View tự xử lý thông báo.
 */
public class EmployeeController {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmployeeService service;

    // ✅ Không còn nhận JComponent view
    public EmployeeController() {
        this.service = new EmployeeService();
    }

    // ==================== LOAD ====================

    /** Lấy toàn bộ danh sách nhân viên từ DB */
    public List<Employee> loadAll() {
        return service.getAllNhanVien();
    }

    /** Lấy 1 nhân viên theo mã */
    public Employee getById(int maNV) {
        return service.getNhanVienById(maNV);
    }

    /** Lấy danh sách tên nhân viên (dùng cho ComboBox) */
    public List<String> getAllTenNhanVien() {
        return service.getAllTenNhanVien();
    }

    /** Lấy mã NV từ tên */
    public int getMaNVByName(String tenNV) {
        return service.getMaNVByName(tenNV);
    }

    /** Tìm kiếm tên nhân viên theo keyword */
    public List<String> searchByName(String keyword) {
        return service.searchByName(keyword);
    }

    // ==================== SEARCH / FILTER ====================

    /**
     * Lọc danh sách theo từ khóa.
     * Tìm theo: họ tên, mã NV formatted, SĐT, CCCD.
     */
    public List<Employee> filter(List<Employee> source, String keyword) {
        if (source == null) return Collections.emptyList();
        if (keyword == null || keyword.trim().isEmpty()) return source;

        String kw = keyword.trim().toLowerCase();
        return source.stream()
                .filter(nv -> nv.getHoTen().toLowerCase().contains(kw)
                        || nv.getMaNVFormatted().toLowerCase().contains(kw)
                        || (nv.getSdt()  != null && nv.getSdt().contains(kw))
                        || (nv.getCccd() != null && nv.getCccd().contains(kw)))
                .collect(Collectors.toList());
    }

    // ==================== SORT ====================

    /**
     * Sắp xếp danh sách in-place.
     * @param colIndex  0 = Mã NV | 3 = Ngày sinh | 5 = Ngày vào làm
     * @param ascending true = tăng dần / cũ nhất trước
     */
    public void sort(List<Employee> source, int colIndex, boolean ascending) {
        if (source == null || source.isEmpty()) return;
        source.sort((nv1, nv2) -> {
            int result = 0;
            switch (colIndex) {
                case 0:
                    result = Integer.compare(nv1.getMaNV(), nv2.getMaNV());
                    break;
                case 3:
                    if (nv1.getNgaySinh() != null && nv2.getNgaySinh() != null)
                        result = nv1.getNgaySinh().compareTo(nv2.getNgaySinh());
                    break;
                case 5:
                    if (nv1.getNgayVaoLam() != null && nv2.getNgayVaoLam() != null)
                        result = nv1.getNgayVaoLam().compareTo(nv2.getNgayVaoLam());
                    break;
            }
            return ascending ? result : -result;
        });
    }

    // ==================== PAGINATION ====================

    /**
     * ✅ Logic phân trang chuyển vào Controller.
     * @return subList tương ứng với trang hiện tại
     */
    public List<Employee> getPage(List<Employee> source, int page, int pageSize) {
        if (source == null || source.isEmpty()) return Collections.emptyList();
        int from = (page - 1) * pageSize;
        int to   = Math.min(from + pageSize, source.size());
        if (from >= source.size()) return Collections.emptyList();
        return source.subList(from, to);
    }

    /**
     * ✅ Tính tổng số trang – chuyển vào Controller.
     */
    public int getTotalPages(List<Employee> source, int pageSize) {
        if (source == null || source.isEmpty()) return 1;
        return (int) Math.ceil((double) source.size() / pageSize);
    }

    // ==================== SAVE ====================

    /**
     * ✅ Trả về SaveResult thay vì hiện JOptionPane.
     * View nhận kết quả và tự quyết định hiển thị thông báo gì.
     *
     * @param currentNhanVien null = thêm mới, non-null = cập nhật
     * @return SaveResult chứa trạng thái và thông báo lỗi nếu có
     */
    public SaveResult handleSave(Employee currentNhanVien,
                                  String hoTen, String sdt, String cccd,
                                  String ngaySinhStr, String ngayVaoLamStr) {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            return SaveResult.fail("Họ tên không được để trống!");
        }

        LocalDate ngaySinh   = null;
        LocalDate ngayVaoLam = null;
        try {
            if (ngaySinhStr != null && !ngaySinhStr.trim().isEmpty())
                ngaySinh = LocalDate.parse(ngaySinhStr.trim(), DTF);
            if (ngayVaoLamStr != null && !ngayVaoLamStr.trim().isEmpty())
                ngayVaoLam = LocalDate.parse(ngayVaoLamStr.trim(), DTF);
        } catch (DateTimeParseException e) {
            return SaveResult.fail("Sai định dạng ngày (dd/MM/yyyy)!");
        }

        if (currentNhanVien == null) {
            // Thêm mới
            Employee nv = new Employee(0, hoTen.trim(), sdt.trim(), ngaySinh, cccd.trim(), ngayVaoLam);
            boolean ok = service.addNhanVien(nv);
            return ok ? SaveResult.ok("Thêm nhân viên thành công!")
                      : SaveResult.fail("Lỗi thêm nhân viên!");
        } else {
            // Cập nhật
            currentNhanVien.setHoTen(hoTen.trim());
            currentNhanVien.setSdt(sdt.trim());
            currentNhanVien.setCccd(cccd.trim());
            currentNhanVien.setNgaySinh(ngaySinh);
            currentNhanVien.setNgayVaoLam(ngayVaoLam);
            boolean ok = service.updateNhanVien(currentNhanVien);
            return ok ? SaveResult.ok("Cập nhật nhân viên thành công!")
                      : SaveResult.fail("Lỗi cập nhật nhân viên!");
        }
    }

    // ==================== DELETE ====================

    /**
     * ✅ Chỉ thực hiện xóa, không hỏi confirm, không hiện dialog.
     * View tự hỏi confirm trước khi gọi hàm này.
     *
     * @return true nếu xóa thành công
     */
    public boolean handleDelete(int maNV) {
        return service.deleteNhanVien(maNV);
    }

    // ==================== INNER: SaveResult ====================

    /**
     * ✅ Value object trả về kết quả save để View hiển thị thông báo.
     */
    public static class SaveResult {
        public final boolean success;
        public final String  message;

        private SaveResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static SaveResult ok(String message)   { return new SaveResult(true,  message); }
        public static SaveResult fail(String message) { return new SaveResult(false, message); }
    }
    
    public static class PageResult<T> {
        public final List<T> data;
        public final int currentPage;
        public final int totalPages;

        public PageResult(List<T> data, int currentPage, int totalPages) {
            this.data = data;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }
    }
    
    public PageResult<Employee> getPage(
            List<Employee> all,
            String keyword,
            int page,
            int pageSize) {

        List<Employee> filtered = filter(all, keyword);

        int total = Math.max(1,
            (int) Math.ceil((double) filtered.size() / pageSize));

        if (page > total) page = total;
        if (page < 1) page = 1;

        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, filtered.size());

        return new PageResult<>(
            filtered.subList(from, to),
            page,
            total
        );
    }
}
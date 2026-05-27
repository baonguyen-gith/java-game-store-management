package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Employee;
import otkhongluong.gamestoremanagement.service.EmployeeService;
import otkhongluong.gamestoremanagement.util.FormatUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeController {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmployeeService service;

    public EmployeeController() {
        this.service = new EmployeeService();
    }

    // ==================== LOAD ====================

    public List<Employee> loadAll() {
        return service.getAllNhanVien();
    }

    public Employee getById(int maNV) {
        return service.getNhanVienById(maNV);
    }

    public List<String> getAllTenNhanVien() {
        return service.getAllTenNhanVien();
    }

    public int getMaNVByName(String tenNV) {
        return service.getMaNVByName(tenNV);
    }

    public List<String> searchByName(String keyword) {
        return service.searchByName(keyword);
    }

    // ==================== SEARCH / FILTER ====================

    public List<Employee> filter(List<Employee> source, String keyword) {
        if (source == null) return Collections.emptyList();
        if (keyword == null || keyword.trim().isEmpty()) return source;

        String kw = keyword.trim().toLowerCase();
        return source.stream()
                .filter(nv -> {
                    String maNVFormatted = FormatUtil.formatMaNV(nv.getMaNV()); // FIX
                    return nv.getHoTen().toLowerCase().contains(kw)
                            || maNVFormatted.toLowerCase().contains(kw)
                            || (nv.getSdt()  != null && nv.getSdt().contains(kw))
                            || (nv.getCccd() != null && nv.getCccd().contains(kw));
                })
                .collect(Collectors.toList());
    }

    // ==================== SORT ====================

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

    public List<Employee> getPage(List<Employee> source, int page, int pageSize) {
        if (source == null || source.isEmpty()) return Collections.emptyList();
        int from = (page - 1) * pageSize;
        int to   = Math.min(from + pageSize, source.size());
        if (from >= source.size()) return Collections.emptyList();
        return source.subList(from, to);
    }

    public int getTotalPages(List<Employee> source, int pageSize) {
        if (source == null || source.isEmpty()) return 1;
        return (int) Math.ceil((double) source.size() / pageSize);
    }

    // ==================== SAVE ====================

    public SaveResult handleSave(Employee currentNhanVien,
                                  String hoTen, String sdt, String cccd,
                                  String ngaySinhStr, String ngayVaoLamStr) {
        if (hoTen == null || hoTen.trim().isEmpty())
            return SaveResult.fail("Họ tên không được để trống!");

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

        if (ngaySinh != null && ngayVaoLam != null && ngaySinh.isAfter(ngayVaoLam)) {
            return SaveResult.fail("Ngày sinh không được lớn hơn ngày vào làm!");
        }

        if (currentNhanVien == null) {
            Employee nv = new Employee(0, hoTen.trim(), sdt.trim(), ngaySinh, cccd.trim(), ngayVaoLam);
            boolean ok = service.addNhanVien(nv);
            return ok ? SaveResult.ok("Thêm nhân viên thành công!")
                      : SaveResult.fail("Lỗi thêm nhân viên!");
        } else {
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

    public boolean handleDelete(int maNV) {
        return service.deleteNhanVien(maNV);
    }

    // ==================== INNER CLASSES ====================

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
            this.data        = data;
            this.currentPage = currentPage;
            this.totalPages  = totalPages;
        }
    }

    public PageResult<Employee> getPage(List<Employee> all, String keyword,
                                         int page, int pageSize) {
        List<Employee> filtered = filter(all, keyword);
        int total = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
        if (page > total) page = total;
        if (page < 1)     page = 1;
        int from = (page - 1) * pageSize;
        int to   = Math.min(from + pageSize, filtered.size());
        return new PageResult<>(filtered.subList(from, to), page, total);
    }
}
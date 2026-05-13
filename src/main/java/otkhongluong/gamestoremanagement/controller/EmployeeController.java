package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Employee;
import otkhongluong.gamestoremanagement.service.EmployeeService;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller trung gian giữa EmployeePanel / EmployeeDialog và EmployeeService.
 * Panel và Dialog chỉ gọi Controller, không gọi Service trực tiếp.
 */
public class EmployeeController {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmployeeService service;
    private final JComponent view; // EmployeePanel hoặc EmployeeDialog truyền vào

    public EmployeeController(JComponent view) {
        this.service = new EmployeeService();
        this.view = view;
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

    /** Lấy danh sách tên nhân viên (dùng cho ComboBox ở các panel khác) */
    public List<String> getAllTenNhanVien() {
        return service.getAllTenNhanVien();
    }

    /** Lấy mã NV từ tên (dùng khi tạo hóa đơn) */
    public int getMaNVByName(String tenNV) {
        return service.getMaNVByName(tenNV);
    }

    /** Tìm kiếm tên nhân viên theo keyword (dùng cho autocomplete) */
    public List<String> searchByName(String keyword) {
        return service.searchByName(keyword);
    }

    // ==================== SEARCH / FILTER ====================

    /**
     * Lọc danh sách theo từ khóa – tương đương getFilteredData() trong Panel.
     * Hỗ trợ tìm theo: họ tên, mã NV formatted, SĐT, CCCD.
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
     * Sắp xếp danh sách – tương đương sortData() trong Panel.
     * Thao tác trực tiếp trên list (in-place), Panel gọi renderPage() sau.
     *
     * @param source    danh sách gốc (allData)
     * @param colIndex  0 = Mã NV | 3 = Ngày sinh | 5 = Ngày vào làm
     * @param ascending true = tăng dần / cũ nhất trước
     */
    public void sort(List<Employee> source, int colIndex, boolean ascending) {
        if (source == null || source.isEmpty()) return;
        source.sort((nv1, nv2) -> {
            int result = 0;
            switch (colIndex) {
                case 0: // Mã NV – so sánh bằng int để 2 đứng trước 11
                    result = Integer.compare(nv1.getMaNV(), nv2.getMaNV());
                    break;
                case 3: // Ngày sinh
                    if (nv1.getNgaySinh() != null && nv2.getNgaySinh() != null) {
                        result = nv1.getNgaySinh().compareTo(nv2.getNgaySinh());
                    }
                    break;
                case 5: // Ngày vào làm
                    if (nv1.getNgayVaoLam() != null && nv2.getNgayVaoLam() != null) {
                        result = nv1.getNgayVaoLam().compareTo(nv2.getNgayVaoLam());
                    }
                    break;
                default:
                    result = 0;
            }
            return ascending ? result : -result;
        });
    }

    // ==================== SAVE (ADD hoặc UPDATE từ EmployeeDialog) ====================

    /**
     * Xử lý lưu từ EmployeeDialog – tương đương saveNhanVien() trong Dialog.
     * Bao gồm: validate họ tên, parse ngày, phân nhánh add/update, hiển thị thông báo.
     *
     * @param currentNhanVien null = chế độ thêm mới, non-null = chế độ cập nhật
     * @param hoTen           text từ txtHoTen
     * @param sdt             text từ txtSdt
     * @param cccd            text từ txtCccd
     * @param ngaySinhStr     text từ txtNgaySinh (dd/MM/yyyy hoặc rỗng)
     * @param ngayVaoLamStr   text từ txtNgayVaoLam (dd/MM/yyyy hoặc rỗng)
     * @return true nếu lưu thành công (Dialog gọi dispose() và onSuccess.run())
     */
    public boolean handleSave(Employee currentNhanVien,
                               String hoTen, String sdt, String cccd,
                               String ngaySinhStr, String ngayVaoLamStr) {
        // --- Validate họ tên (giống check trong saveNhanVien()) ---
        if (hoTen == null || hoTen.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Họ tên không được để trống!");
            return false;
        }

        // --- Parse ngày (giống try-catch trong saveNhanVien()) ---
        LocalDate ngaySinh   = null;
        LocalDate ngayVaoLam = null;
        try {
            if (ngaySinhStr != null && !ngaySinhStr.trim().isEmpty()) {
                ngaySinh = LocalDate.parse(ngaySinhStr.trim(), DTF);
            }
            if (ngayVaoLamStr != null && !ngayVaoLamStr.trim().isEmpty()) {
                ngayVaoLam = LocalDate.parse(ngayVaoLamStr.trim(), DTF);
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(view, "Sai định dạng ngày (dd/MM/yyyy)!");
            return false;
        }

        // --- Phân nhánh ADD / UPDATE (giống if(currentNhanVien == null) trong saveNhanVien()) ---
        if (currentNhanVien == null) {
            Employee nv = new Employee(0, hoTen.trim(), sdt.trim(), ngaySinh, cccd.trim(), ngayVaoLam);
            boolean ok = service.addNhanVien(nv);
            if (ok) {
                JOptionPane.showMessageDialog(view, "Thêm nhân viên thành công!");
            } else {
                JOptionPane.showMessageDialog(view, "Lỗi thêm nhân viên!");
            }
            return ok;
        } else {
            currentNhanVien.setHoTen(hoTen.trim());
            currentNhanVien.setSdt(sdt.trim());
            currentNhanVien.setCccd(cccd.trim());
            currentNhanVien.setNgaySinh(ngaySinh);
            currentNhanVien.setNgayVaoLam(ngayVaoLam);

            boolean ok = service.updateNhanVien(currentNhanVien);
            if (ok) {
                JOptionPane.showMessageDialog(view, "Cập nhật nhân viên thành công!");
            } else {
                JOptionPane.showMessageDialog(view, "Lỗi cập nhật nhân viên!");
            }
            return ok;
        }
    }

    // ==================== DELETE ====================

    /**
     * Xử lý xóa nhân viên – tương đương logic btnDelete trong buildBottomBar().
     * Controller tự hiển thị confirm dialog, Panel không cần xử lý thêm.
     *
     * @return true nếu xóa thành công
     */
    public boolean handleDelete(Employee nv) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Xác nhận xóa nhân viên " + nv.getHoTen() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return false;

        boolean ok = service.deleteNhanVien(nv.getMaNV());
        if (ok) {
            JOptionPane.showMessageDialog(view, "Đã xóa thành công!");
        } else {
            JOptionPane.showMessageDialog(view, "Lỗi khi xóa nhân viên!");
        }
        return ok;
    }
}
package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.service.CustomerService;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller trung gian giữa CustomerPanel và CustomerService.
 * Panel chỉ gọi Controller, không gọi Service trực tiếp.
 */
public class CustomerController {

    private final CustomerService service;
    private final JComponent view; // CustomerPanel truyền vào (dùng làm parent cho dialog)

    public CustomerController(JComponent view) {
        this.service = new CustomerService();
        this.view = view;
    }

    // ==================== LOAD ====================

    /** Lấy toàn bộ danh sách khách hàng từ DB */
    public List<Customer> loadAll() {
        return service.getAllKhachHang();
    }

    /** Lấy 1 khách hàng theo mã */
    public Customer getById(int maKH) {
        return service.getKhachHangById(maKH);
    }

    // ==================== SEARCH / FILTER ====================

    /**
     * Lọc danh sách theo từ khóa – tương đương getFilteredData() trong Panel.
     * Hỗ trợ tìm theo: họ tên, SĐT, CCCD, mã số ("1") và mã đầy đủ ("kh001").
     */
    public List<Customer> filter(List<Customer> source, String keyword) {
        if (source == null) return Collections.emptyList();
        if (keyword == null || keyword.trim().isEmpty()) return source;

        String kw = keyword.trim().toLowerCase();
        return source.stream()
                .filter(kh -> {
                    String maStr  = String.valueOf(kh.getMaKH());
                    String maFull = "kh" + String.format("%03d", kh.getMaKH());
                    return kh.getHoTen().toLowerCase().contains(kw)
                            || (kh.getSdt()  != null && kh.getSdt().contains(kw))
                            || (kh.getCccd() != null && kh.getCccd().contains(kw))
                            || maStr.contains(kw)
                            || maFull.contains(kw);
                })
                .collect(Collectors.toList());
    }

    /**
     * Lọc nhanh theo điểm tích lũy – tương đương showSimpleFilterDialog() trong Panel.
     * @param mode "all" | "loyal" (>= 100 điểm) | "new" (< 100 điểm)
     */
    public List<Customer> filterByPointMode(List<Customer> source, String mode) {
        if (source == null) return Collections.emptyList();
        if ("loyal".equals(mode)) {
            return source.stream()
                    .filter(kh -> kh.getDiemTichLuy() >= 100)
                    .collect(Collectors.toList());
            } else if ("new".equals(mode)) {
            return source.stream()
                    .filter(kh -> kh.getDiemTichLuy() < 100)
                    .collect(Collectors.toList());
            } else {
            return source;
        }
    }

    // ==================== SORT ====================

    /**
     * Sắp xếp danh sách – tương đương executeSort() trong Panel.
     * Thao tác trực tiếp trên list (in-place), Panel gọi renderPage() sau.
     * @param source    danh sách gốc (allData)
     * @param type      "MaKH" | "Diem"
     * @param ascending true = tăng dần
     */
    public void sort(List<Customer> source, String type, boolean ascending) {
        if (source == null || source.isEmpty()) return;
        source.sort((a, b) -> {
            int cmp = type.equals("MaKH")
                    ? Integer.compare(a.getMaKH(), b.getMaKH())
                    : Integer.compare(a.getDiemTichLuy(), b.getDiemTichLuy());
            return ascending ? cmp : -cmp;
        });
    }

    // ==================== ADD ====================

    /**
     * Xử lý thêm khách hàng mới – tương đương logic trong showAddCustomerDialog().
     * Panel truyền các giá trị text đã lấy từ JTextField.
     * @return true nếu thêm thành công
     */
    public boolean handleAdd(String hoTen, String sdt, String cccd,
                              String email, String diaChi) {
        try {
            Customer kh = new Customer();
            kh.setHoTen(hoTen.trim());
            kh.setSdt(sdt.trim());
            kh.setCccd(cccd.trim());
            kh.setEmail(email.trim());
            kh.setDiaChi(diaChi.trim());
            kh.setDiemTichLuy(0);

            boolean ok = service.addKhachHang(kh);
            if (ok) {
                JOptionPane.showMessageDialog(view, "Thêm khách hàng thành công!");
            } else {
                JOptionPane.showMessageDialog(view,
                        "Thêm thất bại! Vui lòng kiểm tra lại kết nối Database.");
            }
            return ok;
        } catch (IllegalArgumentException ex) {
            // Bắt lỗi từ validateKhachHang() trong CustomerService
            JOptionPane.showMessageDialog(view, ex.getMessage(),
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Lỗi hệ thống: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    // ==================== UPDATE ====================

    /**
     * Xử lý cập nhật khách hàng – tương đương logic trong showEditCustomerDialog().
     * Panel truyền đối tượng kh hiện tại và các giá trị text mới từ JTextField.
     * @return true nếu cập nhật thành công
     */
    public boolean handleUpdate(Customer kh, String hoTen, String sdt,
                                 String cccd, String email, String diaChi) {
        try {
            kh.setHoTen(hoTen.trim());
            kh.setSdt(sdt.trim());
            kh.setCccd(cccd.trim());
            kh.setEmail(email.trim());
            kh.setDiaChi(diaChi.trim());

            boolean ok = service.updateKhachHang(kh);
            if (ok) {
                JOptionPane.showMessageDialog(view, "Cập nhật thành công!");
            } else {
                JOptionPane.showMessageDialog(view, "Cập nhật thất bại!");
            }
            return ok;
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ==================== DELETE ====================

    /**
     * Xử lý xóa khách hàng – tương đương logic btnDelete trong buildBottomBar().
     * Controller tự hiển thị confirm dialog, Panel không cần xử lý thêm.
     * @return true nếu xóa thành công
     */
    public boolean handleDelete(Customer kh) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Xác nhận xóa khách hàng: " + kh.getHoTen() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return false;

        boolean ok = service.deleteKhachHang(kh.getMaKH());
        if (ok) {
            JOptionPane.showMessageDialog(view, "Xóa thành công!");
        } else {
            JOptionPane.showMessageDialog(view, "Lỗi khi xóa!");
        }
        return ok;
    }
}
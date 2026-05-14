package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.service.CustomerService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller trung gian giữa CustomerPanel và CustomerService.
 * ✅ Không giữ tham chiếu View, không hiển thị JOptionPane.
 * ✅ Trả về ActionResult để View tự xử lý thông báo.
 */
public class CustomerController {

    private final CustomerService service;

    // ✅ Không còn nhận JComponent view
    public CustomerController() {
        this.service = new CustomerService();
    }

    // ==================== LOAD ====================

    public List<Customer> loadAll() {
        return service.getAllKhachHang();
    }

    public Customer getById(int maKH) {
        return service.getKhachHangById(maKH);
    }

    // ==================== SEARCH / FILTER ====================

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
     * Lọc nhanh theo điểm tích lũy.
     * @param mode "all" | "loyal" (>= 100 điểm) | "new" (< 100 điểm)
     */
    public List<Customer> filterByPointMode(List<Customer> source, String mode) {
        if (source == null) return Collections.emptyList();
        if ("loyal".equals(mode))
            return source.stream().filter(kh -> kh.getDiemTichLuy() >= 100).collect(Collectors.toList());
        if ("new".equals(mode))
            return source.stream().filter(kh -> kh.getDiemTichLuy() < 100).collect(Collectors.toList());
        return source;
    }

    // ==================== SORT ====================

    /**
     * Sắp xếp in-place.
     * @param type "MaKH" | "Diem"
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

    // ==================== PAGINATION ====================

    /** ✅ Logic phân trang chuyển vào Controller */
    public List<Customer> getPage(List<Customer> source, int page, int pageSize) {
        if (source == null || source.isEmpty()) return Collections.emptyList();
        int from = (page - 1) * pageSize;
        int to   = Math.min(from + pageSize, source.size());
        if (from >= source.size()) return Collections.emptyList();
        return source.subList(from, to);
    }

    /** ✅ Tính tổng số trang */
    public int getTotalPages(List<Customer> source, int pageSize) {
        if (source == null || source.isEmpty()) return 1;
        return (int) Math.ceil((double) source.size() / pageSize);
    }

    // ==================== ADD ====================

    /**
     * ✅ Trả về ActionResult thay vì hiện JOptionPane.
     */
    public ActionResult handleAdd(String hoTen, String sdt, String cccd,
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
            return ok
                ? ActionResult.ok("Thêm khách hàng thành công!")
                : ActionResult.fail("Thêm thất bại! Vui lòng kiểm tra lại kết nối Database.");
        } catch (IllegalArgumentException ex) {
            return ActionResult.fail(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ActionResult.fail("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    // ==================== UPDATE ====================

    /**
     * ✅ Trả về ActionResult thay vì hiện JOptionPane.
     */
    public ActionResult handleUpdate(Customer kh, String hoTen, String sdt,
                                      String cccd, String email, String diaChi) {
        try {
            kh.setHoTen(hoTen.trim());
            kh.setSdt(sdt.trim());
            kh.setCccd(cccd.trim());
            kh.setEmail(email.trim());
            kh.setDiaChi(diaChi.trim());

            boolean ok = service.updateKhachHang(kh);
            return ok
                ? ActionResult.ok("Cập nhật thành công!")
                : ActionResult.fail("Cập nhật thất bại!");
        } catch (IllegalArgumentException ex) {
            return ActionResult.fail(ex.getMessage());
        }
    }

    // ==================== DELETE ====================

    /**
     * ✅ Chỉ thực hiện xóa, không hỏi confirm, không hiện dialog.
     * View tự hỏi confirm trước khi gọi hàm này.
     */
    public ActionResult handleDelete(int maKH) {
        boolean ok = service.deleteKhachHang(maKH);
        return ok
            ? ActionResult.ok("Xóa thành công!")
            : ActionResult.fail("Lỗi khi xóa!");
    }

    // ==================== INNER: ActionResult ====================

    /**
     * ✅ Value object trả về kết quả để View hiển thị thông báo.
     */
    public static class ActionResult {
        public final boolean success;
        public final String  message;

        private ActionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ActionResult ok(String message)   { return new ActionResult(true,  message); }
        public static ActionResult fail(String message) { return new ActionResult(false, message); }
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
    public PageResult<Customer> getPage(
            List<Customer> all,
            String keyword,
            int page,
            int pageSize) {

        List<Customer> filtered = filter(all, keyword);

        int total = Math.max(1,
            (int)Math.ceil((double) filtered.size() / pageSize));

        if (page > total) page = total;
        if (page < 1) page = 1;

        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, filtered.size());

        List<Customer> pageData = filtered.isEmpty()
            ? List.of()
            : filtered.subList(from, to);

        return new PageResult<>(pageData, page, total);
    }
}
package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Disc;
import otkhongluong.gamestoremanagement.service.DiscService;

import java.util.List;

public class DiscController {

    private final DiscService service;

    public DiscController() {
        this.service = new DiscService();
    }

    // ==================== LOAD ====================

    /** Trả danh sách đĩa CD của 1 sản phẩm. */
    public List<Disc> loadByMaSP(int maSP) {
        try {
            return service.getByMaSP(maSP);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Tổng số đĩa (mọi trạng thái). */
    public int getTongTon(int maSP) {
        return service.getTongTon(maSP);
    }

    /** Số đĩa đang Sẵn Sàng. */
    public int getSanSang(int maSP) {
        return service.getSanSang(maSP);
    }

    // ==================== THÊM ĐĨA ====================

    /**
     * Thêm nhiều đĩa cùng lúc, tình trạng mặc định "Mới".
     * View truyền vào soLuongStr (text từ JTextField).
     */
    public ProductController.ActionResult handleThemNhieuDia(int maSP, String soLuongStr) {
        try {
            if (soLuongStr == null || soLuongStr.trim().isEmpty())
                return ProductController.ActionResult.fail("Vui lòng nhập số lượng!");

            int soLuong = Integer.parseInt(soLuongStr.trim());
            if (soLuong <= 0)
                return ProductController.ActionResult.fail("Số lượng phải lớn hơn 0!");
            if (soLuong > 100)
                return ProductController.ActionResult.fail("Số lượng nhập một lần tối đa 100 đĩa!");

            int thanhCong = 0;
            for (int i = 0; i < soLuong; i++) {
                if (service.themDia(maSP, "Mới")) thanhCong++;
            }

            if (thanhCong == soLuong)
                return ProductController.ActionResult.ok(
                        "Đã nhập kho " + thanhCong + " đĩa CD thành công!");
            else if (thanhCong > 0)
                return ProductController.ActionResult.ok(
                        "Nhập kho được " + thanhCong + "/" + soLuong + " đĩa. Một số đĩa bị lỗi.");
            else
                return ProductController.ActionResult.fail("Lỗi: Không thể thêm đĩa. Kiểm tra lại Mã SP.");

        } catch (NumberFormatException ex) {
            return ProductController.ActionResult.fail("Số lượng phải là số nguyên!");
        } catch (Exception ex) {
            return ProductController.ActionResult.fail("Lỗi: " + ex.getMessage());
        }
    }

    /**
     * Thêm 1 đĩa với tình trạng tùy chỉnh (dùng cho các trường hợp khác nếu cần).
     */
    public ProductController.ActionResult handleThemDia(int maSP, String tinhTrangStr) {
        try {
            if (tinhTrangStr == null || tinhTrangStr.trim().isEmpty())
                return ProductController.ActionResult.fail("Vui lòng nhập tình trạng đĩa!");

            boolean ok = service.themDia(maSP, tinhTrangStr);
            return ok
                    ? ProductController.ActionResult.ok("Thêm đĩa CD thành công!")
                    : ProductController.ActionResult.fail("Lỗi: Không thể thêm đĩa. Kiểm tra lại Mã SP.");
        } catch (Exception ex) {
            return ProductController.ActionResult.fail("Lỗi: " + ex.getMessage());
        }
    }

    // ==================== CẬP NHẬT TÌNH TRẠNG ====================

    public ProductController.ActionResult handleCapNhatTinhTrang(int maCD, String tinhTrangStr) {
        try {
            if (tinhTrangStr == null || tinhTrangStr.trim().isEmpty())
                return ProductController.ActionResult.fail("Vui lòng nhập tình trạng mới!");

            boolean ok = service.capNhatTinhTrang(maCD, tinhTrangStr);
            return ok
                    ? ProductController.ActionResult.ok("Cập nhật tình trạng thành công!")
                    : ProductController.ActionResult.fail("Không tìm thấy đĩa CD này.");
        } catch (Exception ex) {
            return ProductController.ActionResult.fail("Lỗi: " + ex.getMessage());
        }
    }

    // ==================== XÓA ĐĨA ====================

    /**
     * Chỉ xóa được đĩa đang ở trạng thái SanSang.
     * View tự hỏi xác nhận trước khi gọi method này.
     */
    public ProductController.ActionResult handleXoaDia(int maCD) {
        try {
            boolean ok = service.xoaDia(maCD);
            return ok
                    ? ProductController.ActionResult.ok("Đã xóa đĩa CD!")
                    : ProductController.ActionResult.fail(
                        "Không thể xóa! Đĩa này đang được thuê hoặc không tồn tại.");
        } catch (Exception ex) {
            return ProductController.ActionResult.fail("Lỗi: " + ex.getMessage());
        }
    }
}
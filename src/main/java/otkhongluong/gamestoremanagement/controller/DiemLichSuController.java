package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.DiemLichSu;
import otkhongluong.gamestoremanagement.service.DiemLichSuService;

import java.util.List;

/**
 * Controller quản lý điểm tích lũy.
 * ✅ Không giữ tham chiếu View, không hiện JOptionPane.
 * ✅ Trả về ActionResult để View tự xử lý thông báo.
 */
public class DiemLichSuController {

    private final DiemLichSuService service = new DiemLichSuService();

    // ==================== ĐỌC LỊCH SỬ ====================

    public List<DiemLichSu> getLichSu(int maKH) {
        return service.getLichSu(maKH);
    }

    // ==================== CỘNG ĐIỂM ====================

    public ActionResult congDiem(int maKH, String diemStr, String ghiChu) {
        int diem;
        try {
            diem = Integer.parseInt(diemStr.trim());
        } catch (NumberFormatException e) {
            return ActionResult.fail("Số điểm không hợp lệ! Vui lòng nhập số nguyên.");
        }

        String err = service.congDiem(maKH, diem, ghiChu);
        return err == null
            ? ActionResult.ok("Cộng " + diem + " điểm thành công!")
            : ActionResult.fail(err);
    }

    // ==================== TRỪ ĐIỂM ====================

    public ActionResult truDiem(int maKH, String diemStr, String ghiChu) {
        int diem;
        try {
            diem = Integer.parseInt(diemStr.trim());
        } catch (NumberFormatException e) {
            return ActionResult.fail("Số điểm không hợp lệ! Vui lòng nhập số nguyên.");
        }

        String err = service.truDiem(maKH, diem, ghiChu);
        return err == null
            ? ActionResult.ok("Trừ " + diem + " điểm thành công!")
            : ActionResult.fail(err);
    }

    // ==================== SỬA ĐIỂM ====================

    public ActionResult suaDiem(int maKH, String diemStr, String ghiChu) {
        int diemMoi;
        try {
            diemMoi = Integer.parseInt(diemStr.trim());
        } catch (NumberFormatException e) {
            return ActionResult.fail("Số điểm không hợp lệ! Vui lòng nhập số nguyên.");
        }

        String err = service.suaDiem(maKH, diemMoi, ghiChu);
        return err == null
            ? ActionResult.ok("Cập nhật điểm thành " + diemMoi + " thành công!")
            : ActionResult.fail(err);
    }

    // ==================== XÓA LỊCH SỬ ====================

    public ActionResult xoaLichSu(int maLS) {
        String err = service.xoaLichSu(maLS);
        return err == null
            ? ActionResult.ok("Đã xóa bản ghi và hoàn tác điểm!")
            : ActionResult.fail(err);
    }

    // ==================== INNER: ActionResult ====================

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
}
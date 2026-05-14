package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.DiemLichSuDAO;
import otkhongluong.gamestoremanagement.model.DiemLichSu;

import java.util.List;

/**
 * Service quản lý điểm tích lũy khách hàng.
 * Validate đầu vào rồi uỷ quyền xuống DAO.
 */
public class DiemLichSuService {

    private final DiemLichSuDAO dao = new DiemLichSuDAO();

    // ==================== ĐỌC LỊCH SỬ ====================

    public List<DiemLichSu> getLichSu(int maKH) {
        if (maKH <= 0) throw new IllegalArgumentException("Mã khách hàng không hợp lệ!");
        return dao.findByMaKH(maKH);
    }

    // ==================== CỘNG ĐIỂM ====================

    /** @return null nếu OK, thông báo lỗi nếu thất bại */
    public String congDiem(int maKH, int diem, String ghiChu) {
        if (diem <= 0) return "Số điểm cộng phải lớn hơn 0!";
        boolean ok = dao.congDiem(maKH, diem, ghiChu);
        return ok ? null : "Cộng điểm thất bại, kiểm tra kết nối DB!";
    }

    // ==================== TRỪ ĐIỂM ====================

    /** @return null nếu OK, thông báo lỗi nếu thất bại */
    public String truDiem(int maKH, int diem, String ghiChu) {
        if (diem <= 0) return "Số điểm trừ phải lớn hơn 0!";
        return dao.truDiem(maKH, diem, ghiChu);
    }

    // ==================== SỬA ĐIỂM ====================

    /** @return null nếu OK, thông báo lỗi nếu thất bại */
    public String suaDiem(int maKH, int diemMoi, String ghiChu) {
        if (diemMoi < 0) return "Điểm không được âm!";
        return dao.suaDiem(maKH, diemMoi, ghiChu);
    }

    // ==================== XÓA LỊCH SỬ ====================

    /** @return null nếu OK, thông báo lỗi nếu thất bại */
    public String xoaLichSu(int maLS) {
        if (maLS <= 0) return "Mã lịch sử không hợp lệ!";
        return dao.xoaLichSu(maLS);
    }
}
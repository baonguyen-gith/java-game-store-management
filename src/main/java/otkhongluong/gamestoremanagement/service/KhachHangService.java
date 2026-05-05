package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.KhachHangDAO;
import otkhongluong.gamestoremanagement.model.KhachHang;

import java.util.List;

public class KhachHangService {

    private final KhachHangDAO khachHangDAO;

    public KhachHangService() {
        this.khachHangDAO = new KhachHangDAO();
    }

    // ================= GET ALL =================
    public List<KhachHang> getAllKhachHang() {
        return khachHangDAO.findAll();
    }

    // ================= GET BY ID =================
    public KhachHang getKhachHangById(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        }

        return khachHangDAO.findById(id);
    }

    // ================= ADD =================
    public boolean addKhachHang(KhachHang kh) {

        validateKhachHang(kh);

        // mặc định điểm = 0 khi tạo mới
        kh.setDiemTichLuy(0);

        return khachHangDAO.insert(kh);
    }

    // ================= UPDATE =================
    public boolean updateKhachHang(KhachHang kh) {

        if (kh == null || kh.getMaKH() <= 0) {
            throw new IllegalArgumentException("Khách hàng không hợp lệ");
        }

        validateKhachHang(kh);

        return khachHangDAO.update(kh);
    }

    // ================= DELETE =================
    public boolean deleteKhachHang(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Mã KH không hợp lệ");
        }

        return khachHangDAO.delete(id);
    }

    // ================= VALIDATION =================
    private void validateKhachHang(KhachHang kh) {

        if (kh == null) {
            throw new IllegalArgumentException("Khách hàng không được null");
        }

        ValidationService.validateNotEmpty(kh.getHoTen(), "Họ tên");

        ValidationService.validateNotEmpty(kh.getSdt(), "Số điện thoại");
        ValidationService.validatePhone(kh.getSdt());

        ValidationService.validateNotEmpty(kh.getCccd(), "CCCD");

        if (kh.getEmail() != null && !kh.getEmail().isEmpty()) {
            ValidationService.validateEmail(kh.getEmail());
        }

        ValidationService.validateNotEmpty(kh.getDiaChi(), "Địa chỉ");

        if (kh.getDiemTichLuy() < 0) {
            throw new IllegalArgumentException("Điểm tích lũy không hợp lệ");
        }
    }
}
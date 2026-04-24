package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.KhachHangDAO;
import otkhongluong.gamestoremanagement.model.KhachHang;
import java.util.List;

public class KhachHangService {
    private final KhachHangDAO khachHangDAO;

    public KhachHangService() {
        this.khachHangDAO = new KhachHangDAO();
    }

    public List<KhachHang> getAllKhachHang() {
        return khachHangDAO.findAll();
    }

    public KhachHang getKhachHangById(int id) {
        return khachHangDAO.findById(id);
    }

    public boolean addKhachHang(KhachHang kh) {
        ValidationService.validateNotEmpty(kh.getHoTen(), "Họ tên");
        ValidationService.validatePhone(kh.getSDT());
        if (kh.getEmail() != null && !kh.getEmail().isEmpty()) {
            ValidationService.validateEmail(kh.getEmail());
        }
        return khachHangDAO.insert(kh);
    }

    public boolean updateKhachHang(KhachHang kh) {
        return khachHangDAO.update(kh);
    }

    public boolean deleteKhachHang(int id) {
        return khachHangDAO.delete(id);
    }
}

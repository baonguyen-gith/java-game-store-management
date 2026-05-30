package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.CustomerDAO;
import otkhongluong.gamestoremanagement.model.Customer;

import java.util.List;

public class CustomerService {

    private final CustomerDAO khachHangDAO;

    public CustomerService() {
        this.khachHangDAO = new CustomerDAO();
    }

    // ================= GET ALL =================
    public List<Customer> getAllKhachHang() {
        return khachHangDAO.findAll();
    }

    // ================= GET BY ID =================
    public Customer getKhachHangById(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        }

        return khachHangDAO.findById(id);
    }

    // ================= ADD =================
    public boolean addKhachHang(Customer kh) {
        validateKhachHang(kh, false);
        return khachHangDAO.insert(kh);
    }

    // ================= UPDATE =================
    public boolean updateKhachHang(Customer kh) {
        if (kh == null || kh.getMaKH() <= 0)
            throw new IllegalArgumentException("Khách hàng không hợp lệ");

        // Lấy dữ liệu cũ để kiểm tra CCCD
        Customer existing = khachHangDAO.findById(kh.getMaKH());
        if (existing == null)
            throw new IllegalArgumentException("Không tìm thấy khách hàng");

        validateKhachHang(kh, existing.getCccd() != null && !existing.getCccd().isBlank());
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
    private void validateKhachHang(Customer kh, boolean cccdRequired) {
        if (kh == null)
            throw new IllegalArgumentException("Khách hàng không được null");

        ValidationService.validateNotEmpty(kh.getHoTen(), "Họ tên");
        ValidationService.validateNotEmpty(kh.getSdt(), "Số điện thoại");
        ValidationService.validatePhone(kh.getSdt());

        // CCCD: bắt buộc nếu trước đó đã có
        if (cccdRequired) {
            if (kh.getCccd() == null || kh.getCccd().isBlank())
                throw new IllegalArgumentException("CCCD đã được đăng ký trước đó, không được để trống");
        } else {
            if (kh.getCccd() == null) kh.setCccd("");
        }

        if (kh.getEmail() != null && !kh.getEmail().isBlank())
            ValidationService.validateEmail(kh.getEmail());

        if (kh.getDiemTichLuy() < 0)
            throw new IllegalArgumentException("Điểm tích lũy không hợp lệ");

        if (kh.getDiaChi() == null) kh.setDiaChi("");
        if (kh.getEmail()  == null) kh.setEmail("");
    }
}
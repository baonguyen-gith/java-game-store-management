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
    // 1. Kiểm tra dữ liệu trước khi thêm
    validateKhachHang(kh);

    // 2. Gọi DAO để chèn vào Database
    // Sử dụng hàm insert(kh) đã có sẵn trong CustomerDAO của bạn
    return khachHangDAO.insert(kh);
}

    // ================= UPDATE =================
    public boolean updateKhachHang(Customer kh) {

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
    private void validateKhachHang(Customer kh) {

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
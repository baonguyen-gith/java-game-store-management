package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.EmployeeDAO;
import otkhongluong.gamestoremanagement.model.Employee;

import java.util.List;

public class EmployeeService {
    private EmployeeDAO nhanVienDAO = new EmployeeDAO();

    public List<Employee> getAllNhanVien() {
        return nhanVienDAO.findAll();
    }

    public Employee getNhanVienById(int maNV) {
        return nhanVienDAO.findById(maNV);
    }

    public boolean addNhanVien(Employee nv) {
        if (nv == null || nv.getHoTen() == null || nv.getHoTen().trim().isEmpty()) {
            return false;
        }
        return nhanVienDAO.insert(nv);
    }

    public boolean updateNhanVien(Employee nv) {
        if (nv == null || nv.getMaNV() <= 0) {
            return false;
        }
        return nhanVienDAO.update(nv);
    }

    public boolean deleteNhanVien(int maNV) {
        if (maNV <= 0) return false;
        return nhanVienDAO.delete(maNV);
    }

    public List<String> getAllTenNhanVien() {
        return nhanVienDAO.getAllTenNhanVien();
    }

    public int getMaNVByName(String tenNV) {
        return nhanVienDAO.getMaNVByName(tenNV);
    }

    public List<String> searchByName(String keyword) {
        return nhanVienDAO.searchByName(keyword);
    }
}

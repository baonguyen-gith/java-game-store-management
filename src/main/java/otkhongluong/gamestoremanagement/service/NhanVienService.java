package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.NhanVienDAO;
import otkhongluong.gamestoremanagement.model.NhanVien;

import java.util.List;

public class NhanVienService {
    private NhanVienDAO nhanVienDAO = new NhanVienDAO();

    public List<NhanVien> getAllNhanVien() {
        return nhanVienDAO.findAll();
    }

    public NhanVien getNhanVienById(int maNV) {
        return nhanVienDAO.findById(maNV);
    }

    public boolean addNhanVien(NhanVien nv) {
        if (nv == null || nv.getHoTen() == null || nv.getHoTen().trim().isEmpty()) {
            return false;
        }
        return nhanVienDAO.insert(nv);
    }

    public boolean updateNhanVien(NhanVien nv) {
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

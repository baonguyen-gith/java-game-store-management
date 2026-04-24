package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.SanPhamDAO;
import otkhongluong.gamestoremanagement.model.SanPham;
import java.util.List;

public class SanPhamService {
    private final SanPhamDAO sanPhamDAO;

    public SanPhamService() {
        this.sanPhamDAO = new SanPhamDAO();
    }

    public List<SanPham> getAllSanPham() {
        return sanPhamDAO.findAll();
    }

    public SanPham getSanPhamById(int id) {
        return sanPhamDAO.findById(id);
    }

    public boolean addSanPham(SanPham sp) {
        if (sp.getGiaBan() < 0) throw new IllegalArgumentException("Giá bán không được âm.");
        return sanPhamDAO.insert(sp);
    }

    public boolean updateSanPham(SanPham sp) {
        return sanPhamDAO.update(sp);
    }

    public boolean deleteSanPham(int id) {
        return sanPhamDAO.delete(id);
    }

    public boolean updateStatus(int id, String status) {
        return sanPhamDAO.updateStatus(id, status);
    }
}

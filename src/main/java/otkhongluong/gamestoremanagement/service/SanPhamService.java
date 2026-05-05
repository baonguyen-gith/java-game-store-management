package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.SanPhamDAO;
import otkhongluong.gamestoremanagement.model.SanPham;

import java.util.List;

public class SanPhamService {

    private final SanPhamDAO sanPhamDAO;

    public SanPhamService() {
        this.sanPhamDAO = new SanPhamDAO();
    }

    // ================= GET ALL =================
    public List<SanPham> getAllSanPham() {
        return sanPhamDAO.findAll();
    }

    // ================= GET BY ID =================
    public SanPham getSanPhamById(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        }

        return sanPhamDAO.findById(id);
    }

    // ================= ADD =================
    public boolean addSanPham(SanPham sp) {

        validateSanPham(sp);

        return sanPhamDAO.insert(sp);
    }

    // ================= UPDATE =================
    public boolean updateSanPham(SanPham sp) {

        if (sp == null || sp.getMaSP() <= 0) {
            throw new IllegalArgumentException("Sản phẩm không hợp lệ");
        }

        validateSanPham(sp);

        return sanPhamDAO.update(sp);
    }

    // ================= DELETE =================
    public boolean deleteSanPham(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        }

        return sanPhamDAO.delete(id);
    }

    // ================= FIND BY GAME =================
    public List<SanPham> getByGame(int maGame) {
        return sanPhamDAO.findByMaGame(maGame);
    }

    // ================= VALIDATION =================
    private void validateSanPham(SanPham sp) {

        if (sp == null) {
            throw new IllegalArgumentException("Sản phẩm không được null");
        }

        if (sp.getMaGame() <= 0) {
            throw new IllegalArgumentException("MaGame không hợp lệ");
        }

        if (sp.getGiaBan() < 0) {
            throw new IllegalArgumentException("Giá bán không được âm");
        }
    }
}
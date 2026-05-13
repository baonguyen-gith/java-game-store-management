package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.ProductDAO;
import otkhongluong.gamestoremanagement.model.Product;

import java.util.List;

public class ProductService {

    private final ProductDAO sanPhamDAO;

    public ProductService() {
        this.sanPhamDAO = new ProductDAO();
    }

    // ================= GET ALL =================
    public List<Product> getAllSanPham() {
        return sanPhamDAO.findAll();
    }

    // ================= GET BY ID =================
    public Product getSanPhamById(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        }

        return sanPhamDAO.findById(id);
    }

    // ================= ADD =================
    public boolean addSanPham(Product sp) {

        validateSanPham(sp);

        return sanPhamDAO.insert(sp);
    }

    // ================= UPDATE =================
    public boolean updateSanPham(Product sp) {

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
    public List<Product> getByGame(int maGame) {
        return sanPhamDAO.findByMaGame(maGame);
    }

    // ================= VALIDATION =================
    private void validateSanPham(Product sp) {

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
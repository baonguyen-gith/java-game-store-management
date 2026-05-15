package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.DiscDAO;
import otkhongluong.gamestoremanagement.model.Disc;

import java.util.List;

public class DiscService {

    private final DiscDAO discDAO;

    public DiscService() {
        this.discDAO = new DiscDAO();
    }

    // ================= GET BY MASP =================
    public List<Disc> getByMaSP(int maSP) {
        if (maSP <= 0) throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        return discDAO.findByMaSP(maSP);
    }

    // ================= TỔNG TỒN KHO =================
    public int getTongTon(int maSP) {
        return discDAO.countByMaSP(maSP);
    }

    // ================= SỐ ĐĨA SẴN SÀNG =================
    public int getSanSang(int maSP) {
        return discDAO.countSanSangByMaSP(maSP);
    }

    // ================= THÊM ĐĨA =================
    public boolean themDia(int maSP, String tinhTrang) {
        if (maSP <= 0) throw new IllegalArgumentException("Mã sản phẩm không hợp lệ");
        if (tinhTrang == null || tinhTrang.trim().isEmpty())
            throw new IllegalArgumentException("Tình trạng không được để trống");
        Disc disc = new Disc(0, maSP, tinhTrang.trim(), "SanSang");
        return discDAO.insert(disc);
    }

    // ================= CẬP NHẬT TÌNH TRẠNG =================
    public boolean capNhatTinhTrang(int maCD, String tinhTrang) {
        if (maCD <= 0) throw new IllegalArgumentException("Mã CD không hợp lệ");
        if (tinhTrang == null || tinhTrang.trim().isEmpty())
            throw new IllegalArgumentException("Tình trạng không được để trống");
        return discDAO.updateTinhTrang(maCD, tinhTrang.trim());
    }

    // ================= XÓA ĐĨA (chỉ khi SanSang) =================
    public boolean xoaDia(int maCD) {
        if (maCD <= 0) throw new IllegalArgumentException("Mã CD không hợp lệ");
        return discDAO.deleteByMaCD(maCD);
    }
}
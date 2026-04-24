package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.HoaDonDAO;
import otkhongluong.gamestoremanagement.model.HoaDon;
import java.util.List;

public class HoaDonService {
    private final HoaDonDAO hoaDonDAO;

    public HoaDonService() {
        this.hoaDonDAO = new HoaDonDAO();
    }

    public boolean createHoaDon(HoaDon hd) {
        // Business logic: Tính tổng tiền tự động trước khi lưu
        double total = calculateTotal(hd.getDanhSachChiTiet());
        hd.setTongTien(total);
        
        return hoaDonDAO.insert(hd);
    }

    public double calculateTotal(List<HoaDon.ChiTietHoaDon> list) {
        double total = 0;
        if (list != null) {
            for (HoaDon.ChiTietHoaDon ct : list) {
                total += ct.getSoLuong() * ct.getDonGia();
            }
        }
        return total;
    }

    public List<HoaDon> getAllHoaDon() {
        return hoaDonDAO.findAll();
    }

    public HoaDon getHoaDonById(int id) {
        return hoaDonDAO.findById(id);
    }
}

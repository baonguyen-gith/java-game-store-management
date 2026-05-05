package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.HoaDonDAO;
import otkhongluong.gamestoremanagement.model.HoaDon;

import java.util.List;

public class HoaDonService {

    private final HoaDonDAO hoaDonDAO;

    public HoaDonService() {
        hoaDonDAO = new HoaDonDAO();
    }

    // ================= CREATE =================
    public boolean createHoaDon(HoaDon hd) {

        if(hd == null) return false;

        // ⭐ BUSINESS RULE:
        // tự tính tổng tiền
        double total = calculateTotal(hd.getDanhSachChiTiet());
        hd.setTongTien(total);

        // trạng thái mặc định
        if(hd.getTrangThai() == null)
            hd.setTrangThai("ChuaThanhToan");

        return hoaDonDAO.insert(hd);
    }

    // ================= CALCULATE =================
    public double calculateTotal(List<HoaDon.ChiTietHoaDon> list) {

        double total = 0;

        if(list == null) return 0;

        for(HoaDon.ChiTietHoaDon ct : list){

            total += ct.getSoLuong() * ct.getDonGia();
        }

        return total;
    }

    // ================= PAYMENT =================
    public boolean thanhToanHoaDon(int maHD){

        HoaDon hd = hoaDonDAO.findById(maHD);
        if(hd == null) return false;

        hd.setTrangThai("DaThanhToan");

        return hoaDonDAO.updateTrangThai(hd);
    }

    // ================= UPDATE =================
    public boolean updateHoaDon(HoaDon hd){

        double total =
                calculateTotal(hd.getDanhSachChiTiet());

        hd.setTongTien(total);

        return hoaDonDAO.update(hd);
    }

    // ================= DELETE =================
    public boolean deleteHoaDon(int maHD){
        return hoaDonDAO.delete(maHD);
    }

    // ================= READ =================
    public List<HoaDon> getAllHoaDon(){
        return hoaDonDAO.findAll();
    }

    public HoaDon getHoaDonById(int id){
        return hoaDonDAO.findById(id);
    }
}
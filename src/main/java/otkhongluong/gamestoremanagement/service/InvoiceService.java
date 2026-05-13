package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.InvoiceDAO;
import otkhongluong.gamestoremanagement.model.Invoice;

import java.util.List;

public class InvoiceService {

    private final InvoiceDAO hoaDonDAO;

    public InvoiceService() {
        hoaDonDAO = new InvoiceDAO();
    }

    // ================= CREATE =================
    public boolean createHoaDon(Invoice hd) {

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
    public double calculateTotal(List<Invoice.ChiTietHoaDon> list) {

        double total = 0;

        if(list == null) return 0;

        for(Invoice.ChiTietHoaDon ct : list){

            total += ct.getSoLuong() * ct.getDonGia();
        }

        return total;
    }

    // ================= PAYMENT =================
    public boolean thanhToanHoaDon(int maHD){

        Invoice hd = hoaDonDAO.findById(maHD);
        if(hd == null) return false;

        hd.setTrangThai("DaThanhToan");

        return hoaDonDAO.updateTrangThai(hd);
    }

    // ================= UPDATE =================
    public boolean updateHoaDon(Invoice hd){

        double total =
                calculateTotal(hd.getDanhSachChiTiet());

        hd.setTongTien(total);

        return hoaDonDAO.update(hd);
    }

    // ================= DELETE =================
    public boolean deleteHoaDon(int maHD) {
        return hoaDonDAO.deleteWithRollback(maHD);
    }

    // ================= READ =================
    public List<Invoice> getAllHoaDon(){
        return hoaDonDAO.findAll();
    }

    public Invoice getHoaDonById(int id){
        return hoaDonDAO.findById(id);
    }
}
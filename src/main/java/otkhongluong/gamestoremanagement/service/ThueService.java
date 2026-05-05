package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.CDDAO;
import otkhongluong.gamestoremanagement.dao.PhieuThueDAO;
import otkhongluong.gamestoremanagement.dao.KhachHangDAO;
import otkhongluong.gamestoremanagement.dao.NhanVienDAO;
import otkhongluong.gamestoremanagement.model.PhieuThue;
import otkhongluong.gamestoremanagement.model.CD;
import java.time.temporal.ChronoUnit;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class ThueService {

    private final PhieuThueDAO phieuThueDAO;
    private final CDDAO cdDAO;
    
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    public ThueService() {

        phieuThueDAO = new PhieuThueDAO();
        cdDAO = new CDDAO();
    }

    // ================= CREATE RENT =================
    public boolean createPhieuThue(PhieuThue pt) {

        if (pt == null || pt.getDanhSachChiTiet() == null) return false;

        pt.setTrangThai("DangThue");

        boolean ok = phieuThueDAO.insert(pt);

        if (!ok) return false;

        // update CD status
        for (PhieuThue.CTPhieuThue ct : pt.getDanhSachChiTiet()) {
            cdDAO.updateTrangThai(ct.getMaCD(), "DangThue");
        }

        return true;
    }

    // ================= RETURN =================
    public boolean returnCD(int maPT, LocalDateTime ngayTraThucTe){

        // 1. lấy phiếu thuê
        PhieuThue pt = phieuThueDAO.findById(maPT);
        if(pt == null) return false;

        // 2. lấy danh sách CD trước
        List<PhieuThue.CTPhieuThue> listCD = pt.getDanhSachChiTiet();

        // 3. tính tiền phạt
        double tienPhat =
        tinhTienPhat(pt, ngayTraThucTe, pt.getDanhSachChiTiet());

        // 4. update phiếu thuê
        boolean ok = phieuThueDAO.updateReturn(
                maPT,
                Timestamp.valueOf(ngayTraThucTe),
                tienPhat
        );

        if(!ok) return false;

        // 5. update CD status
        for(PhieuThue.CTPhieuThue ct : listCD){
            cdDAO.updateTrangThai(ct.getMaCD(), "SanSang");
        }

        // 6. update trạng thái phiếu thuê (nếu cần riêng)
        pt.setTrangThai("DaTra");
        phieuThueDAO.update(pt);

        return true;
    }
    
    // ================= PENALTY =================
    public  double tinhTienPhat(PhieuThue pt,
                             LocalDateTime ngayTra,
                             List<PhieuThue.CTPhieuThue> cds){

        double phat = 0;

        if(pt == null || ngayTra == null) return 0;

        LocalDateTime ngayDK = pt.getNgayTraDuKien();

        if(ngayDK == null) return 0;

        // 🔴 1. PHẠT TRỄ HẠN
        if(ngayTra.isAfter(ngayDK)){

            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    ngayDK.toLocalDate(),
                    ngayTra.toLocalDate()
            );

            if(days <= 0) days = 1; // quá ngày nhưng lệch giờ

            phat += days * 10000;
        }

        // 🔴 2. PHẠT THEO CD
        if(cds != null){
            for(PhieuThue.CTPhieuThue ct : cds){

                if(ct != null &&
                   ct.getTinhTrang() != null &&
                   ct.getTinhTrang().equalsIgnoreCase("HONG")){

                    phat += 50000;
                }
            }
        }

        return phat;
    }
    
    public List<String> getAllKhachHangNames() {
        return khachHangDAO.getAllTenKhachHang();
    }
    
    public List<String> getAllNhanVienNames() {
        return nhanVienDAO.getAllTenNhanVien();
    }

    // ================= UPDATE =================
    public boolean updatePhieuThue(PhieuThue pt){
        return phieuThueDAO.update(pt);
    }

    // ================= DELETE =================
    public boolean deletePhieuThue(int maPT){
        return phieuThueDAO.delete(maPT);
    }

    // ================= READ =================
    public List<PhieuThue> getAll(){
        return phieuThueDAO.findAll();
    }

    public PhieuThue getById(int id){
        return phieuThueDAO.findById(id);
    }
}
package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.CDDAO;
import otkhongluong.gamestoremanagement.dao.PhieuThueDAO;
import otkhongluong.gamestoremanagement.dao.KhachHangDAO;
import otkhongluong.gamestoremanagement.dao.NhanVienDAO;
import otkhongluong.gamestoremanagement.model.PhieuThue;
import otkhongluong.gamestoremanagement.model.CD;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ThueService {

    private final PhieuThueDAO phieuThueDAO;
    private final CDDAO cdDAO;
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhanVienDAO nhanVienDAO   = new NhanVienDAO();

    public ThueService() {
        phieuThueDAO = new PhieuThueDAO();
        cdDAO        = new CDDAO();
    }

    /* ================= CREATE ================= */

    public boolean createPhieuThue(PhieuThue pt) {
        if (pt == null || pt.getDanhSachChiTiet() == null) return false;
        pt.setTrangThai("DangThue");
        boolean ok = phieuThueDAO.insert(pt);
        if (!ok) return false;
        for (PhieuThue.CTPhieuThue ct : pt.getDanhSachChiTiet()) {
            cdDAO.updateTrangThai(ct.getMaCD(), "DangThue");
        }
        return true;
    }

    /* ================= RETURN ================= */

    /** @deprecated dùng returnCD(maPT, ngayTra, chiPhiHuHong) */
    @Deprecated
    public boolean returnCD(int maPT, LocalDateTime ngayTraThucTe) {
        return returnCD(maPT, ngayTraThucTe, 0);
    }

    /**
     * Xử lý trả CD:
     *   TienPhat = phatTreHan + chiPhiHuHong  (ghi DB, chốt)
     *   TienCoc giữ nguyên — dialog tự tính delta = TienCoc - TienPhat để hoàn/thu thêm
     *
     * @param chiPhiHuHong phí sửa chữa/hư hỏng do nhân viên nhập tay
     */
    public boolean returnCD(int maPT, LocalDateTime ngayTraThucTe, double chiPhiHuHong) {
        PhieuThue pt = phieuThueDAO.findById(maPT);
        if (pt == null) return false;

        List<PhieuThue.CTPhieuThue> listCD = pt.getDanhSachChiTiet();

        // Phạt trễ hạn tính từ NgayTraDuKien → ngayTraThucTe
        double phatTreHan = tinhPhatTreHanOnly(pt, ngayTraThucTe);

        // Tổng TienPhat khi trả = phí đã ghi từ gia hạn (TienPhat DB) + trễ hạn mới + hư hỏng
        double tienPhatCu   = pt.getTienPhat();  // đã tích lũy từ các lần gia hạn
        double tienPhatChot = tienPhatCu + phatTreHan + chiPhiHuHong;

        boolean ok = phieuThueDAO.updateReturn(
            maPT,
            Timestamp.valueOf(ngayTraThucTe),
            tienPhatChot
        );
        if (!ok) return false;

        for (PhieuThue.CTPhieuThue ct : listCD) {
            cdDAO.updateTrangThai(ct.getMaCD(), "SanSang");
        }
        return true;
    }

    /** Tính riêng phạt trễ hạn (không cộng phạt hư hỏng) */
    private double tinhPhatTreHanOnly(PhieuThue pt, LocalDateTime ngayTra) {
        if (pt == null || ngayTra == null || pt.getNgayTraDuKien() == null) return 0;
        LocalDateTime ngayDK = pt.getNgayTraDuKien();
        if (!ngayTra.isAfter(ngayDK)) return 0;
        long days = ChronoUnit.DAYS.between(ngayDK.toLocalDate(), ngayTra.toLocalDate());
        if (days <= 0) days = 1;
        return days * 10_000;
    }

    /* ================= PENALTY (ước tính hiển thị) ================= */

    /**
     * Chỉ dùng để hiển thị ước tính — KHÔNG dùng ghi DB.
     * Tính: trễ hạn + phạt hỏng cố định 50k/CD.
     */
    public double tinhTienPhat(PhieuThue pt,
                               LocalDateTime ngayTra,
                               List<PhieuThue.CTPhieuThue> cds) {
        if (pt == null || ngayTra == null || pt.getNgayTraDuKien() == null) return 0;
        double phat = 0;
        LocalDateTime ngayDK = pt.getNgayTraDuKien();
        if (ngayTra.isAfter(ngayDK)) {
            long days = ChronoUnit.DAYS.between(ngayDK.toLocalDate(), ngayTra.toLocalDate());
            if (days <= 0) days = 1;
            phat += days * 10_000;
        }
        if (cds != null) {
            for (PhieuThue.CTPhieuThue ct : cds) {
                if (ct != null && "HONG".equalsIgnoreCase(ct.getTinhTrang())) {
                    phat += 50_000;
                }
            }
        }
        return phat;
    }

    /* ================= EXTEND ================= */

    /**
     * Gia hạn phiếu thuê.
     *
     * Gọi DAO với 4 tham số — TienCoc KHÔNG được chạm vào.
     *
     *   NgayTraDuKien += soNgay
     *   TienPhat      += phatTre + phiGiaHan
     *   TienCoc       = giữ nguyên (quyết toán khi trả CD)
     *
     * @param maPT       mã phiếu thuê
     * @param soNgay     số ngày gia hạn thêm
     * @param phatTre    phí phạt trễ (0 nếu chưa trễ)
     * @param phiGiaHan  phí gia hạn = tổng đơn giá × soNgay
     */
    public boolean extendRental(int maPT, int soNgay, double phatTre, double phiGiaHan) {
        return phieuThueDAO.extendRental(maPT, soNgay, phatTre, phiGiaHan);
    }

    /* ================= CRUD ================= */

    public boolean updatePhieuThue(PhieuThue pt) { return phieuThueDAO.update(pt); }
    public boolean deletePhieuThue(int maPT)     { return phieuThueDAO.delete(maPT); }
    public List<PhieuThue> getAll()              { return phieuThueDAO.findAll(); }
    public PhieuThue getById(int id)             { return phieuThueDAO.findById(id); }

    public List<String> getAllKhachHangNames()   { return khachHangDAO.getAllTenKhachHang(); }
    public List<String> getAllNhanVienNames()    { return nhanVienDAO.getAllTenNhanVien(); }
}
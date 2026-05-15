package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.DiscDAO;
import otkhongluong.gamestoremanagement.dao.RentalOrderDAO;
import otkhongluong.gamestoremanagement.dao.CustomerDAO;
import otkhongluong.gamestoremanagement.dao.EmployeeDAO;
import otkhongluong.gamestoremanagement.model.RentalOrder;
import otkhongluong.gamestoremanagement.util.DBConnection;  // ✅ THÊM

import java.sql.Connection;        // ✅ THÊM
import java.sql.PreparedStatement; // ✅ THÊM
import java.sql.ResultSet;         // ✅ THÊM
import java.sql.SQLException;      // ✅ THÊM
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RentalService {

    private final RentalOrderDAO phieuThueDAO;
    private final DiscDAO cdDAO;
    private final CustomerDAO khachHangDAO = new CustomerDAO();
    private final EmployeeDAO nhanVienDAO   = new EmployeeDAO();

    public RentalService() {
        phieuThueDAO = new RentalOrderDAO();
        cdDAO        = new DiscDAO();
    }

    /* ================= CREATE ================= */

    public boolean createPhieuThue(RentalOrder pt) {
        if (pt == null || pt.getDanhSachChiTiet() == null) return false;
        pt.setTrangThai("DangThue");

        // Dùng 1 connection duy nhất để transaction an toàn
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // ✅ CHECK RACE CONDITION trước khi insert
                for (RentalOrder.CTPhieuThue ct : pt.getDanhSachChiTiet()) {
                    String chk = "SELECT TrangThai FROM CD WHERE MaCD = ?";
                    try (PreparedStatement ps = con.prepareStatement(chk)) {
                        ps.setInt(1, ct.getMaCD());
                        ResultSet rs = ps.executeQuery();
                        if (!rs.next()) {
                            con.rollback();
                            return false; // CD không tồn tại
                        }
                        String trangThai = rs.getString("TrangThai");
                        if (!"SanSang".equals(trangThai)) {
                            con.rollback();
                            return false; // CD đã bị thuê/bán bởi giao dịch khác
                        }
                    }
                }

                // Insert phiếu thuê
                boolean ok = phieuThueDAO.insertWithConnection(pt, con); // ⚠ xem vấn đề 2
                if (!ok) { con.rollback(); return false; }

                // Cập nhật TrangThai CD
                for (RentalOrder.CTPhieuThue ct : pt.getDanhSachChiTiet()) {
                    String upd = "UPDATE CD SET TrangThai = N'DangThue' WHERE MaCD = ?";
                    try (PreparedStatement ps = con.prepareStatement(upd)) {
                        ps.setInt(1, ct.getMaCD());
                        ps.executeUpdate();
                    }
                }

                con.commit();
                return true;

            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /* ================= RETURN ================= */

    /** @deprecated dùng returnCD(maPT, ngayTra, chiPhiHuHong) */
    @Deprecated
    public boolean returnCD(int maPT, LocalDateTime ngayTraThucTe) {
        return returnCD(maPT, ngayTraThucTe, 0);
    }

    public boolean returnCD(int maPT, LocalDateTime ngayTraThucTe, double chiPhiHuHong) {
        RentalOrder pt = phieuThueDAO.findById(maPT);
        if (pt == null) return false;

        double phatTreHan   = tinhPhatTreHanOnly(pt, ngayTraThucTe);
        double tienPhatChot = pt.getTienPhat() + phatTreHan + chiPhiHuHong;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Update phiếu thuê
                String sqlPT = "UPDATE PHIEUTHUE SET NgayTraThucTe = ?, TienPhat = ?, TrangThai = N'DaTra' WHERE MaPT = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlPT)) {
                    ps.setTimestamp(1, Timestamp.valueOf(ngayTraThucTe));
                    ps.setDouble(2, tienPhatChot);
                    ps.setInt(3, maPT);
                    if (ps.executeUpdate() == 0) { con.rollback(); return false; }
                }

                // Update TrangThai tất cả CD về SanSang
                String sqlCD = "UPDATE CD SET TrangThai = N'SanSang' WHERE MaCD = ?";
                for (RentalOrder.CTPhieuThue ct : pt.getDanhSachChiTiet()) {
                    try (PreparedStatement ps = con.prepareStatement(sqlCD)) {
                        ps.setInt(1, ct.getMaCD());
                        ps.executeUpdate();
                    }
                }

                con.commit();
                return true;

            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Tính riêng phạt trễ hạn (không cộng phạt hư hỏng) */
    private double tinhPhatTreHanOnly(RentalOrder pt, LocalDateTime ngayTra) {
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
    public double tinhTienPhat(RentalOrder pt,
                               LocalDateTime ngayTra,
                               List<RentalOrder.CTPhieuThue> cds) {
        if (pt == null || ngayTra == null || pt.getNgayTraDuKien() == null) return 0;
        double phat = 0;
        LocalDateTime ngayDK = pt.getNgayTraDuKien();
        if (ngayTra.isAfter(ngayDK)) {
            long days = ChronoUnit.DAYS.between(ngayDK.toLocalDate(), ngayTra.toLocalDate());
            if (days <= 0) days = 1;
            phat += days * 10_000;
        }
        if (cds != null) {
            for (RentalOrder.CTPhieuThue ct : cds) {
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

    public boolean updatePhieuThue(RentalOrder pt) { 
        return phieuThueDAO.update(pt); 
    }
    public boolean deletePhieuThue(int maPT){ 
        return phieuThueDAO.delete(maPT); 
    }
    public List<RentalOrder> getAll() {
        return phieuThueDAO.findAll();
    }
    public RentalOrder getById(int id) { 
        return phieuThueDAO.findById(id); 
    }

    public List<String> getAllKhachHangNames() { 
        return khachHangDAO.getAllTenKhachHang(); 
    }
    public List<String> getAllNhanVienNames() { 
        return nhanVienDAO.getAllTenNhanVien(); 
    }
}
package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.DiscDAO;
import otkhongluong.gamestoremanagement.dao.PointDAO;
import otkhongluong.gamestoremanagement.dao.RentalOrderDAO;
import otkhongluong.gamestoremanagement.dao.CustomerDAO;
import otkhongluong.gamestoremanagement.dao.EmployeeDAO;
import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.model.Employee;
import otkhongluong.gamestoremanagement.model.RentalOrder;
import otkhongluong.gamestoremanagement.model.CTPhieuThue;
import otkhongluong.gamestoremanagement.model.RentDetailData;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RentalService {

    private final RentalOrderDAO phieuThueDAO;
    private final DiscDAO        cdDAO;
    private final PointDAO       pointDAO     = new PointDAO();
    private final CustomerDAO    khachHangDAO = new CustomerDAO();
    private final EmployeeDAO    nhanVienDAO  = new EmployeeDAO();

    private static final int DIEM_TO_VND  = 5_000;   // 1 điểm = 5.000 VNĐ
    private static final int VND_PER_DIEM = 100_000;  // 100.000 VNĐ thuê = 1 điểm cộng

    public RentalService() {
        phieuThueDAO = new RentalOrderDAO();
        cdDAO        = new DiscDAO();
    }

    /* ================= CREATE ================= */

    public boolean createPhieuThue(RentalOrder pt) {
        if (pt == null || pt.getDanhSachChiTiet() == null) return false;
        pt.setTrangThai("DangThue");

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                for (CTPhieuThue ct : pt.getDanhSachChiTiet()) {
                    String chk = "SELECT TrangThai FROM CD WHERE MaCD = ?";
                    try (PreparedStatement ps = con.prepareStatement(chk)) {
                        ps.setInt(1, ct.getMaCD());
                        ResultSet rs = ps.executeQuery();
                        if (!rs.next() || !"SanSang".equals(rs.getString("TrangThai"))) {
                            con.rollback();
                            return false;
                        }
                    }
                }

                boolean ok = phieuThueDAO.insertWithConnection(pt, con);
                if (!ok) { con.rollback(); return false; }

                for (CTPhieuThue ct : pt.getDanhSachChiTiet()) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE CD SET TrangThai = N'DangThue' WHERE MaCD = ?")) {
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

    /* ================= DEDUCT POINTS (RentAddDialog) ================= */

    /**
     * Trừ điểm khi tạo phiếu thuê + ghi DIEM_LICHSU.
     * Delegate hoàn toàn xuống PointDAO — không còn SQL trực tiếp.
     *
     * @return null nếu thành công, message lỗi nếu thất bại
     */
    public String deductPointForRental(int maKH, int diemThucDung, int maPT) {
        return pointDAO.truDiem(maKH, diemThucDung, "Thuê CD - PT" + maPT);
    }

    /* ================= RETURN CD (full — RentReturnDialog) ================= */

    /**
     * Trả CD đầy đủ: cập nhật phiếu thuê + đặt CD = SanSang trong 1 transaction,
     * sau đó cộng điểm tích lũy qua PointDAO (connection riêng biệt — eventual consistency).
     *
     * @return "OK:..." nếu thành công, "ERR:..." nếu lỗi
     */
    public String returnCDFull(int maPT, LocalDateTime ngayTraThucTe,
                                double chiPhiHuHong, int maKH, int diemTichLuy) {
        RentalOrder pt = phieuThueDAO.findById(maPT);
        if (pt == null) return "ERR:Không tìm thấy phiếu thuê PT" + maPT;

        double phatTreHan   = tinhPhatTreHanOnly(pt, ngayTraThucTe);
        double tienPhatChot = pt.getTienPhat() + phatTreHan + chiPhiHuHong;

        // Bước 1 — cập nhật phiếu thuê + CD trong 1 transaction
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE PHIEUTHUE SET NgayTraThucTe=?, TienPhat=?, TrangThai=N'DaTra' WHERE MaPT=?")) {
                    ps.setTimestamp(1, Timestamp.valueOf(ngayTraThucTe));
                    ps.setDouble(2, tienPhatChot);
                    ps.setInt(3, maPT);
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return "ERR:Cập nhật phiếu thuê thất bại!";
                    }
                }

                for (CTPhieuThue ct : pt.getDanhSachChiTiet()) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE CD SET TrangThai = N'SanSang' WHERE MaCD = ?")) {
                        ps.setInt(1, ct.getMaCD());
                        ps.executeUpdate();
                    }
                }

                con.commit();

            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                return "ERR:" + ex.getMessage();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "ERR:Lỗi kết nối: " + ex.getMessage();
        }

        // Bước 2 — cộng điểm qua PointDAO (eventual consistency)
        if (maKH > 0 && diemTichLuy > 0) {
            boolean diemOk = pointDAO.congDiem(maKH, diemTichLuy, "Trả CD - PT" + maPT);
            if (!diemOk) {
                // Phiếu thuê đã trả thành công; log lỗi điểm để xử lý thủ công nếu cần
                System.err.println("[RentalService] Cảnh báo: trả CD PT" + maPT
                        + " thành công nhưng cộng điểm cho KH" + maKH + " thất bại.");
            }
        }

        return "OK:Trả CD thành công!";
    }

    /** @deprecated dùng returnCDFull() */
    @Deprecated
    public boolean returnCD(int maPT, LocalDateTime ngayTraThucTe) {
        return returnCD(maPT, ngayTraThucTe, 0);
    }

    public boolean returnCD(int maPT, LocalDateTime ngayTraThucTe, double chiPhiHuHong) {
        return returnCDFull(maPT, ngayTraThucTe, chiPhiHuHong, -1, 0).startsWith("OK");
    }

    /* ================= LOAD DIEM DA TRU (RentReturnDialog) ================= */

    /**
     * Tổng điểm đã trừ khi lập phiếu thuê, lấy từ DIEM_LICHSU qua PointDAO.
     */
    public int loadDiemDaTru(int maPT) {
        return pointDAO.sumDiemTruByMaPT(maPT);
    }

    /* ================= SAVE EDIT RENTAL (RentEditDialog) ================= */

    /**
     * Lưu thay đổi sửa phiếu thuê trong 1 transaction (KH/NV + ngày trả).
     * Điều chỉnh điểm khi đổi KH được thực hiện qua PointDAO sau transaction chính.
     *
     * @return "OK:..." nếu thành công, "ERR:..." nếu lỗi
     */
    public String saveEditRental(int maPT,
                                 Customer newKH,
                                 Employee newNV,
                                 LocalDateTime newNgayTra,
                                 int maKHCu, int maNVCu) {
        boolean khChanged   = newKH != null && newKH.getMaKH() != maKHCu;
        boolean nvChanged   = newNV != null && newNV.getMaNV() != maNVCu;
        boolean ngayChanged = newNgayTra != null;

        if (!khChanged && !nvChanged && !ngayChanged)
            return "ERR:Không có thay đổi nào để lưu.";

        int maKHMoi = khChanged ? newKH.getMaKH() : maKHCu;
        int maNVMoi = nvChanged ? newNV.getMaNV() : maNVCu;

        // Bước 1 — cập nhật DB trong transaction
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (khChanged || nvChanged) {
                    phieuThueDAO.updateKhachHangVaNhanVien(maPT, maKHMoi, maNVMoi);
                }
                if (ngayChanged) {
                    phieuThueDAO.updateNgayTraVaDonGia(maPT, newNgayTra);
                }
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                return "ERR:" + ex.getMessage();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "ERR:Lỗi kết nối: " + ex.getMessage();
        }

        // Bước 2 — chuyển điểm từ KH cũ sang KH mới qua PointDAO (eventual consistency)
        if (khChanged) {
            int diem = phieuThueDAO.tinhDiemPhieu(maPT);
            if (diem > 0) {
                String errTru  = pointDAO.truDiem(maKHCu,  diem, "Chuyển điểm sang KH" + maKHMoi + " - PT" + maPT);
                boolean congOk = pointDAO.congDiem(maKHMoi, diem, "Nhận điểm từ KH"    + maKHCu  + " - PT" + maPT);
                if (errTru != null || !congOk) {
                    System.err.println("[RentalService] Cảnh báo: cập nhật PT" + maPT
                            + " thành công nhưng chuyển điểm KH" + maKHCu
                            + "→KH" + maKHMoi + " có lỗi. Kiểm tra thủ công.");
                }
            }
        }

        return "OK:Cập nhật phiếu PT" + String.format("%04d", maPT) + " thành công!";
    }

    
    public RentDetailData loadRentDetail(int maPT) {
        RentDetailData data = new RentDetailData();

        String sqlHeader =
            "SELECT pt.NgayThue, pt.NgayTraDuKien, pt.NgayTraThucTe, " +
            "       pt.TienCoc, pt.TienPhat, pt.TrangThai, " +
            "       kh.HoTen, kh.SDT, " +
            "       SUM(ct.DonGiaThue) AS TongDonGiaThue " +
            "FROM PHIEUTHUE pt " +
            "LEFT JOIN KHACHHANG kh ON pt.MaKH = kh.MaKH " +
            "LEFT JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
            "WHERE pt.MaPT = ? " +
            "GROUP BY pt.MaPT, pt.NgayThue, pt.NgayTraDuKien, pt.NgayTraThucTe, " +
            "         pt.TienCoc, pt.TienPhat, pt.TrangThai, kh.HoTen, kh.SDT";

        String sqlDetail =
            "SELECT TOP 1 cd.MaCD, g.MaGame, g.TenGame, sp.GiaThueNgay " +
            "FROM CTPHIEUTHUE ct " +
            "JOIN CD      cd ON ct.MaCD   = cd.MaCD " +
            "JOIN SANPHAM sp ON cd.MaSP   = sp.MaSP " +
            "JOIN GAME    g  ON sp.MaGame = g.MaGame " +
            "WHERE ct.MaPT = ?";

        // Điểm đã trừ — qua PointDAO
        data.giamDiem = loadDiemDaTru(maPT) * (double) DIEM_TO_VND;

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sqlHeader)) {
                ps.setInt(1, maPT);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;

                    data.daTra           = "DaTra".equalsIgnoreCase(rs.getString("TrangThai"));
                    data.trangThai       = data.daTra ? "Đã trả" : "Đang thuê";
                    data.tenKH           = nvl2(rs.getString("HoTen"), "—");
                    data.sdt             = nvl2(rs.getString("SDT"),   "—");
                    data.ngayThue        = rs.getTimestamp("NgayThue");
                    data.ngayTraDK       = rs.getTimestamp("NgayTraDuKien");
                    data.ngayTraTT       = rs.getTimestamp("NgayTraThucTe");
                    data.tienCoc         = rs.getDouble("TienCoc");
                    data.tienPhatDB      = rs.getDouble("TienPhat");
                    data.tienThueBanDau  = rs.getDouble("TongDonGiaThue");
                    data.tienThueNetDiem = Math.max(0, data.tienThueBanDau - data.giamDiem);

                    if (!data.daTra && data.ngayTraDK != null) {
                        LocalDateTime ngayDK = data.ngayTraDK.toLocalDateTime();
                        LocalDateTime homNay = LocalDate.now().atStartOfDay();
                        if (homNay.isAfter(ngayDK)) {
                            long ngayTre = ChronoUnit.DAYS.between(
                                ngayDK.toLocalDate(), homNay.toLocalDate());
                            if (ngayTre <= 0) ngayTre = 1;
                            data.phatTreTamTinh = ngayTre * 10_000;
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlDetail)) {
                ps.setInt(1, maPT);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.maCD          = rs.getInt("MaCD");
                        data.maGame        = rs.getInt("MaGame");
                        data.tenGame       = nvl(rs.getString("TenGame"));
                        data.giaThueNgayHT = rs.getDouble("GiaThueNgay");

                        data.soNgayGoc = data.giaThueNgayHT > 0
                            ? Math.max(1, Math.round(data.tienThueBanDau / data.giaThueNgayHT))
                            : 1;

                        if (data.tienPhatDB > 0 && data.ngayThue != null && data.ngayTraDK != null) {
                            long soNgayTong = ChronoUnit.DAYS.between(
                                data.ngayThue.toLocalDateTime().toLocalDate(),
                                data.ngayTraDK.toLocalDateTime().toLocalDate());
                            long soNgayGiaHan   = Math.max(0, soNgayTong - data.soNgayGoc);
                            data.tienGiaHan     = soNgayGiaHan * data.giaThueNgayHT;
                            data.treHanDaDong   = Math.max(0, data.tienPhatDB - data.tienGiaHan);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return data;
    }

    /* ================= EXTEND ================= */

    public boolean extendRental(int maPT, int soNgay, double phatTre, double phiGiaHan) {
        return phieuThueDAO.extendRental(maPT, soNgay, phatTre, phiGiaHan);
    }

    /* ================= PENALTY ================= */

    public double tinhTienPhat(RentalOrder pt,
                               LocalDateTime ngayTra,
                               List<CTPhieuThue> cds) {
        if (pt == null || ngayTra == null || pt.getNgayTraDuKien() == null) return 0;
        double phat = 0;
        LocalDateTime ngayDK = pt.getNgayTraDuKien();
        if (ngayTra.isAfter(ngayDK)) {
            long days = ChronoUnit.DAYS.between(ngayDK.toLocalDate(), ngayTra.toLocalDate());
            if (days <= 0) days = 1;
            phat += days * 10_000;
        }
        if (cds != null)
            for (CTPhieuThue ct : cds)
                if (ct != null && "HONG".equalsIgnoreCase(ct.getTrangThai()))
                    phat += 50_000;
        return phat;
    }

    /* ================= CRUD ================= */

    public boolean updatePhieuThue(RentalOrder pt) { return phieuThueDAO.update(pt); }
    public boolean deletePhieuThue(int maPT)       { return phieuThueDAO.delete(maPT); }
    public List<RentalOrder> getAll()              { return phieuThueDAO.findAll(); }
    public RentalOrder getById(int id)             { return phieuThueDAO.findById(id); }

    public List<String> getAllKhachHangNames()     { return khachHangDAO.getAllTenKhachHang(); }
    public List<String> getAllNhanVienNames()      { return nhanVienDAO.getAllTenNhanVien(); }

    public int tinhDiemTuPhieuThue(int maPT) {
        double tongTien = phieuThueDAO.getTongTienByMaPT(maPT);
        return (int) (tongTien / VND_PER_DIEM);
    }

    /* ================= PRIVATE HELPERS ================= */

    private double tinhPhatTreHanOnly(RentalOrder pt, LocalDateTime ngayTra) {
        if (pt == null || ngayTra == null || pt.getNgayTraDuKien() == null) return 0;
        LocalDateTime ngayDK = pt.getNgayTraDuKien();
        if (!ngayTra.isAfter(ngayDK)) return 0;
        long days = ChronoUnit.DAYS.between(ngayDK.toLocalDate(), ngayTra.toLocalDate());
        if (days <= 0) days = 1;
        return days * 10_000;
    }

    private String nvl(String s)              { return s == null ? "" : s; }
    private String nvl2(String s, String def) { return (s == null || s.isBlank()) ? def : s; }
}
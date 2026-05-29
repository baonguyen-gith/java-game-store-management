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
import otkhongluong.gamestoremanagement.util.FormatUtil;
import java.util.ArrayList;
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

    private static final int DIEM_TO_VND  = 5_000;
    private static final int VND_PER_DIEM = 100_000;

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
                    // UPDLOCK: giữ khóa update, ngăn transaction khác cùng đọc-update
                    // ROWLOCK: chỉ lock dòng này, không lock cả bảng
                    String chk = "SELECT TrangThai FROM CD WITH (UPDLOCK, ROWLOCK) WHERE MaCD = ?";
                    try (PreparedStatement ps = con.prepareStatement(chk)) {
                        ps.setInt(1, ct.getMaCD());
                        ResultSet rs = ps.executeQuery();
                        if (!rs.next()) { con.rollback(); return false; }
                        String trangThai = rs.getString("TrangThai");
                        if (!"SanSang".equals(trangThai)) {
                            con.rollback();
                            return false;   // CD đang bận — báo lỗi cho UI
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

    public String deductPointForRental(int maKH, int diemThucDung, int maPT) {
        return pointDAO.truDiemVoiMaPT(maKH, diemThucDung, maPT, "Thuê CD - PT" + maPT);
    }

    /* ================= RETURN CD (full — RentReturnDialog) ================= */

    public String returnCDFull(int maPT, LocalDateTime ngayTraThucTe,
                                double chiPhiHuHong, int maKH, int diemTichLuy) {
        RentalOrder pt = phieuThueDAO.findById(maPT);
        if (pt == null) return "ERR:Không tìm thấy phiếu thuê PT" + maPT;

        // Trigger tự cộng phạt trễ mới vào TienPhat hiện tại.
        // Java chỉ cần cộng huHong — không cộng phatTreHan nữa để tránh tính 2 lần.
        double tienPhatChot = pt.getTienPhat() + chiPhiHuHong;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE PHIEUTHUE SET NgayTraThucTe=?, TienPhat=? WHERE MaPT=?")) {
                    ps.setTimestamp(1, Timestamp.valueOf(ngayTraThucTe));
                    ps.setDouble(2, tienPhatChot);
                    ps.setInt(3, maPT);
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return "ERR:Cập nhật phiếu thuê thất bại!";
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

        if (maKH > 0 && diemTichLuy > 0) {
            boolean diemOk = pointDAO.congDiem(maKH, diemTichLuy, "Trả CD - PT" + maPT);
            if (!diemOk) {
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

    public int loadDiemDaTru(int maPT) {
        return pointDAO.sumDiemTruByMaPT(maPT);
    }

    /* ================= SAVE EDIT RENTAL (RentEditDialog) ================= */

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

       try (Connection con = DBConnection.getConnection()) {
           con.setAutoCommit(false);
           try {
               if (khChanged || nvChanged) {
                   phieuThueDAO.updateKhachHangVaNhanVien(con, maPT, maKHMoi, maNVMoi);
               }
               if (ngayChanged) {
                   phieuThueDAO.updateNgayTraVaDonGia(con, maPT, newNgayTra);
               }
               if (khChanged) {
                   int diem = phieuThueDAO.tinhDiemPhieu(maPT);
                   if (diem > 0) {
                       String errTru = pointDAO.truDiemInTx(con, maKHCu, diem,
                           "Chuyển điểm sang KH" + maKHMoi + " - PT" + maPT);
                       if (errTru != null) {
                           con.rollback();
                           return "ERR:Chuyển điểm thất bại: " + errTru;
                       }
                       pointDAO.congDiemInTx(con, maKHMoi, diem,
                           "Nhận điểm từ KH" + maKHCu + " - PT" + maPT);
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

       return "OK:Cập nhật phiếu PT" + String.format("%04d", maPT) + " thành công!";
   }

    /* ================= LOAD RENT DETAIL ================= */

    /**
     * Tải toàn bộ dữ liệu chi tiết phiếu thuê để hiển thị trong RentDetailDialog.
     *
     * FIX BUG 1: Thêm nhánh xử lý trường hợp đang thuê, chưa quá hạn mới,
     * nhưng tienPhatDB > 0 (phạt cũ đã thu khi gia hạn).
     * Trước đây nhánh này bị bỏ qua hoàn toàn làm mất tiền phạt khỏi màn hình.
     *
     * Ba trạng thái sau gia hạn:
     *   [A] Đang thuê, còn hạn, chưa từng quá hạn  -> phatTreTamTinh = 0
     *   [B] Đang thuê, còn hạn, có phạt cũ từ gia hạn -> phatTreTamTinh = tienPhatDB  ← FIX
     *   [C] Đang thuê, đang quá hạn mới             -> phatTreTamTinh = ngayTre * giá * 1.5 + tienPhatDB
     */
    public RentDetailData loadRentDetail(int maPT) {
        RentDetailData data = new RentDetailData();

        final String SQL_DETAIL =
            "SELECT TOP 1 cd.MaCD, g.MaGame, g.TenGame, sp.GiaThueNgay " +
            "FROM CTPHIEUTHUE ct " +
            "JOIN CD      cd ON ct.MaCD   = cd.MaCD " +
            "JOIN SANPHAM sp ON cd.MaSP   = sp.MaSP " +
            "JOIN GAME    g  ON sp.MaGame = g.MaGame " +
            "WHERE ct.MaPT = ?";

        final String SQL_HEADER =
            "SELECT pt.NgayThue, pt.NgayTraDuKien, pt.NgayTraThucTe, " +
            "       pt.TienCoc, pt.TienPhat, pt.TrangThai, " +
            "       kh.HoTen, kh.SDT, " +
            "       SUM(ct.DonGiaThue) AS TongDonGiaThue " +
            "FROM PHIEUTHUE pt " +
            "LEFT JOIN KHACHHANG    kh ON pt.MaKH = kh.MaKH " +
            "LEFT JOIN CTPHIEUTHUE  ct ON pt.MaPT = ct.MaPT " +
            "WHERE pt.MaPT = ? " +
            "GROUP BY pt.MaPT, pt.NgayThue, pt.NgayTraDuKien, pt.NgayTraThucTe, " +
            "         pt.TienCoc, pt.TienPhat, pt.TrangThai, kh.HoTen, kh.SDT";

        data.giamDiem = loadDiemDaTru(maPT) * (double) DIEM_TO_VND;

        try (Connection con = DBConnection.getConnection()) {

            // BƯỚC 1: Tải CD detail TRƯỚC để có giaThueNgayHT
            try (PreparedStatement ps = con.prepareStatement(SQL_DETAIL)) {
                ps.setInt(1, maPT);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.maCD          = rs.getInt("MaCD");
                        data.maGame        = rs.getInt("MaGame");
                        data.tenGame       = nvl(rs.getString("TenGame"));
                        data.giaThueNgayHT = rs.getDouble("GiaThueNgay");
                    }
                }
            }

            // BƯỚC 2: Tải header phiếu thuê
            try (PreparedStatement ps = con.prepareStatement(SQL_HEADER)) {
                ps.setInt(1, maPT);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;

                    data.daTra          = "DaTra".equalsIgnoreCase(rs.getString("TrangThai"));
                    data.trangThai      = data.daTra ? "Đã trả" : "Đang thuê";
                    data.tenKH          = nvl2(rs.getString("HoTen"), "—");
                    data.sdt            = nvl2(rs.getString("SDT"),   "—");
                    data.ngayThue       = rs.getTimestamp("NgayThue");
                    data.ngayTraDK      = rs.getTimestamp("NgayTraDuKien");
                    data.ngayTraTT      = rs.getTimestamp("NgayTraThucTe");
                    data.tienCoc        = rs.getDouble("TienCoc");
                    data.tienPhatDB     = rs.getDouble("TienPhat");
                    data.tienThueBanDau = rs.getDouble("TongDonGiaThue");
                    data.tienThueNetDiem = Math.max(0, data.tienThueBanDau - data.giamDiem);

                    // Tính soNgayGoc
                    if (data.ngayThue != null && data.ngayTraDK != null) {
                        long soNgayTongDK = ChronoUnit.DAYS.between(
                            data.ngayThue.toLocalDateTime().toLocalDate(),
                            data.ngayTraDK.toLocalDateTime().toLocalDate());
                        if (data.giaThueNgayHT > 0) {
                            data.soNgayGoc = Math.max(1,
                                (long) Math.round(data.tienThueBanDau / data.giaThueNgayHT));
                            data.soNgayGoc = Math.min(data.soNgayGoc, Math.max(1, soNgayTongDK));
                        } else {
                            data.soNgayGoc = Math.max(1, soNgayTongDK);
                        }
                    }

                    // Tính tiền gia hạn & phạt đã đóng (chỉ khi đã trả)
                    if (data.daTra && data.tienPhatDB > 0
                            && data.ngayThue != null && data.ngayTraDK != null) {
                        long soNgayTong = ChronoUnit.DAYS.between(
                            data.ngayThue.toLocalDateTime().toLocalDate(),
                            data.ngayTraDK.toLocalDateTime().toLocalDate());
                        long soNgayGiaHan = Math.max(0, soNgayTong - data.soNgayGoc);
                        data.tienGiaHan   = soNgayGiaHan * data.giaThueNgayHT;
                        data.treHanDaDong = Math.max(0, data.tienPhatDB - data.tienGiaHan);
                    }

                    // ── FIX BUG 1: Tính phạt trễ tạm tính (đang thuê) ──────────────
                    if (!data.daTra && data.ngayTraDK != null && data.giaThueNgayHT > 0) {
                        LocalDateTime ngayDK = data.ngayTraDK.toLocalDateTime();
                        LocalDateTime homNay = LocalDate.now().atStartOfDay();

                        if (homNay.isAfter(ngayDK)) {
                            // [C] Đang quá hạn mới: phạt mới + cộng phạt cũ từ DB
                            long ngayTre = ChronoUnit.DAYS.between(
                                ngayDK.toLocalDate(), homNay.toLocalDate());
                            if (ngayTre <= 0) ngayTre = 1;
                            data.phatTreTamTinh = ngayTre * data.giaThueNgayHT * 1.5
                                                  + data.tienPhatDB;
                        } else if (data.tienPhatDB > 0) {
                            // [B] Còn hạn nhưng đã có phạt cũ từ lần gia hạn trước
                            // Hiển thị lại tienPhatDB để không bị "mất" khỏi màn hình
                            data.phatTreTamTinh = data.tienPhatDB;
                        }
                        // [A] Còn hạn, chưa từng quá hạn: phatTreTamTinh = 0 (mặc định)
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

    public boolean extendRental(int maPT, LocalDateTime ngayTraMoi, double phatTre, double phiGiaHan) {
        return phieuThueDAO.extendRental(maPT, ngayTraMoi, phatTre, phiGiaHan);
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
            double giaThueNgay = cds != null
                ? cds.stream().mapToDouble(ct -> ct != null ? ct.getGiaThueNgay() : 0).sum()
                : 0;
            phat += days * giaThueNgay * 1.5;
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
        double giaThueNgay = pt.getDanhSachChiTiet() != null
            ? pt.getDanhSachChiTiet().stream().mapToDouble(CTPhieuThue::getGiaThueNgay).sum()
            : 0;
        return days * giaThueNgay * 1.5;
    }

    public Object[] getRentalExportData(int maPT) {
        RentalOrder rental = phieuThueDAO.findById(maPT);
        if (rental == null) throw new RuntimeException("Không tìm thấy phiếu thuê #" + maPT);

        List<String[]> items = new ArrayList<>();
        for (CTPhieuThue ct : rental.getDanhSachChiTiet()) {
            items.add(new String[]{
                "CD" + String.format("%04d", ct.getMaCD()),
                ct.getTenGame(),
                ct.getTrangThai(),
                FormatUtil.formatTien(ct.getDonGiaThue())
            });
        }

        double tongTienThue = rental.getDanhSachChiTiet().stream()
            .mapToDouble(CTPhieuThue::getDonGiaThue).sum();

        return new Object[]{rental, items, tongTienThue};
    }

    private String nvl(String s)              { return s == null ? "" : s; }
    private String nvl2(String s, String def) { return (s == null || s.isBlank()) ? def : s; }
}
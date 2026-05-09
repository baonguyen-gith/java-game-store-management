package otkhongluong.gamestoremanagement.dao;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import otkhongluong.gamestoremanagement.model.PhieuThue;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuThueDAO {

    /* ================= INSERT ================= */

    public boolean insert(PhieuThue pt) {
        String sql =
            "INSERT INTO PHIEUTHUE(MaKH, NgayTraDuKien, TienCoc, TrangThai) "
          + "VALUES(?, ?, ?, N'DangThue')";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, pt.getMaKH());
            ps.setTimestamp(2, Timestamp.valueOf(pt.getNgayTraDuKien()));
            ps.setDouble(3, pt.getTienCoc());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) pt.setMaPT(rs.getInt(1));

            insertChiTiet(con, pt);
            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /* ================= INSERT DETAIL ================= */

    private void insertChiTiet(Connection con, PhieuThue pt) throws Exception {
        String sql = "INSERT INTO CTPHIEUTHUE(MaPT, MaCD, MaNV, DonGiaThue) VALUES(?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        for (PhieuThue.CTPhieuThue ct : pt.getDanhSachChiTiet()) {
            ps.setInt(1, pt.getMaPT());
            ps.setInt(2, ct.getMaCD());
            ps.setInt(3, ct.getMaNV());
            ps.setDouble(4, ct.getDonGiaThue());
            ps.addBatch();
        }
        ps.executeBatch();
    }

    /* ================= FIND ALL ================= */

    public List<PhieuThue> findAll() {
        List<PhieuThue> list = new ArrayList<>();
        String sql =
            "SELECT pt.MaPT, MIN(ct.MaNV) AS MaNV, pt.NgayThue, " +
            "       kh.HoTen, kh.SDT, pt.NgayTraDuKien, pt.TrangThai " +
            "FROM PHIEUTHUE pt " +
            "LEFT JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
            "LEFT JOIN KHACHHANG kh ON pt.MaKH = kh.MaKH " +
            "GROUP BY pt.MaPT, pt.NgayThue, kh.HoTen, kh.SDT, pt.NgayTraDuKien, pt.TrangThai " +
            "ORDER BY pt.MaPT DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PhieuThue pt = new PhieuThue();
                pt.setMaPT(rs.getInt("MaPT"));
                pt.setMaNV(rs.getInt("MaNV"));
                pt.setTenKhachHang(rs.getString("HoTen"));
                pt.setSoDienThoai(rs.getString("SDT"));
                Timestamp ts = rs.getTimestamp("NgayThue");
                if (ts != null) pt.setNgayThue(ts.toLocalDateTime());
                Timestamp t2 = rs.getTimestamp("NgayTraDuKien");
                if (t2 != null) pt.setNgayTraDuKien(t2.toLocalDateTime());
                pt.setTrangThai(rs.getString("TrangThai"));
                list.add(pt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /* ================= FIND BY ID ================= */

    public PhieuThue findById(int id) {
        String sql =
            "SELECT pt.*, kh.HoTen AS TenKH, MIN(nv.HoTen) AS TenNV " +
            "FROM PHIEUTHUE pt " +
            "LEFT JOIN KHACHHANG kh ON pt.MaKH = kh.MaKH " +
            "LEFT JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
            "LEFT JOIN NHANVIEN nv ON ct.MaNV = nv.MaNV " +
            "WHERE pt.MaPT = ? " +
            "GROUP BY pt.MaPT, pt.MaKH, pt.NgayThue, pt.NgayTraDuKien, " +
            "         pt.NgayTraThucTe, pt.TienCoc, pt.TienPhat, pt.TrangThai, kh.HoTen";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PhieuThue pt = map(rs);
                pt.setDanhSachChiTiet(getChiTiet(con, id));
                return pt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* ================= LOAD DETAIL ================= */

    // ✅ SỬA THÀNH — thêm sp.GiaThueNgay vào SELECT rồi set vào object
    private List<PhieuThue.CTPhieuThue> getChiTiet(Connection con, int maPT) {
        List<PhieuThue.CTPhieuThue> list = new ArrayList<>();
        String sql =
            "SELECT g.TenGame, ct.MaCD, ct.DonGiaThue, cd.TrangThai, sp.GiaThueNgay " + // ← THÊM sp.GiaThueNgay
            "FROM CTPHIEUTHUE ct " +
            "JOIN CD cd ON ct.MaCD = cd.MaCD " +
            "JOIN SANPHAM sp ON cd.MaSP = sp.MaSP " +
            "JOIN GAME g ON sp.MaGame = g.MaGame " +
            "WHERE ct.MaPT = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPT);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PhieuThue.CTPhieuThue ct = new PhieuThue.CTPhieuThue(
                    rs.getInt("MaCD"),
                    rs.getString("TenGame"),
                    rs.getDouble("DonGiaThue"),
                    rs.getString("TrangThai")
                );
                ct.setGiaThueNgay(rs.getDouble("GiaThueNgay")); // ← THÊM dòng này
                ct.setTinhTrang(rs.getString("TrangThai"));
                list.add(ct);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /* ================= UPDATE RETURN ================= */

    public boolean updateReturn(int maPT, Timestamp ngayTra, double tienPhat) {
        String sql =
            "UPDATE PHIEUTHUE " +
            "SET NgayTraThucTe = ?, TienPhat = ?, TrangThai = N'DaTra' " +
            "WHERE MaPT = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, ngayTra);
            ps.setDouble(2, tienPhat);
            ps.setInt(3, maPT);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /* ================= UPDATE CD STATUS AFTER RETURN ================= */

    public void updateCDStatusAfterReturn(Connection con, int maPT) throws Exception {
        String sql =
            "UPDATE CD SET TrangThai = N'SanSang' " +
            "WHERE MaCD IN (SELECT MaCD FROM CTPHIEUTHUE WHERE MaPT = ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPT);
            ps.executeUpdate();
        }
    }

    /* ================= UPDATE ================= */

    public boolean update(PhieuThue pt) {
        String sql =
            "UPDATE PHIEUTHUE " +
            "SET MaKH = ?, NgayTraDuKien = ?, TienCoc = ?, TrangThai = ? " +
            "WHERE MaPT = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pt.getMaKH());
            ps.setTimestamp(2, Timestamp.valueOf(pt.getNgayTraDuKien()));
            ps.setDouble(3, pt.getTienCoc());
            ps.setString(4, pt.getTrangThai());
            ps.setInt(5, pt.getMaPT());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /* ================= DELETE ================= */

    public boolean delete(int id) {
        String sqlDiem    = "DELETE FROM DIEM_LICHSU  WHERE MaPT = ?";
        String sqlChiTiet = "DELETE FROM CTPHIEUTHUE  WHERE MaPT = ?";
        String sqlPhieu   = "DELETE FROM PHIEUTHUE    WHERE MaPT = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps0 = con.prepareStatement(sqlDiem)) {
                ps0.setInt(1, id);
                int rows = ps0.executeUpdate();
                System.out.println("[DELETE] DIEM_LICHSU rows deleted: " + rows);
            }

            try (PreparedStatement ps1 = con.prepareStatement(sqlChiTiet)) {
                ps1.setInt(1, id);
                int rows = ps1.executeUpdate();
                System.out.println("[DELETE] CTPHIEUTHUE rows deleted: " + rows);
            }

            try (PreparedStatement ps2 = con.prepareStatement(sqlPhieu)) {
                ps2.setInt(1, id);
                int rows = ps2.executeUpdate();
                System.out.println("[DELETE] PHIEUTHUE rows deleted: " + rows);

                if (rows == 0) {
                    con.rollback();
                    System.out.println("[DELETE] Rollback — MaPT " + id + " không tồn tại!");
                    return false;
                }
            }

            con.commit();
            System.out.println("[DELETE] Commit thành công MaPT = " + id);
            return true;

        } catch (Exception e) {
            System.out.println("[DELETE] Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Thêm vào PhieuThueDAO.java
    public boolean insertWithConnection(PhieuThue pt, Connection con) throws SQLException {
        // Copy logic của insert() hiện tại nhưng dùng con thay vì tự mở connection
        // KHÔNG gọi con.commit() / con.close() ở đây
        String sql = "INSERT INTO PHIEUTHUE (MaKH, MaNV, NgayThue, NgayTraDuKien, TienCoc, TienPhat, TrangThai) " +
                     "VALUES (?, ?, GETDATE(), ?, ?, 0, N'DangThue')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pt.getMaKH());
            if (pt.getMaNV() > 0) ps.setInt(2, pt.getMaNV());
            else ps.setNull(2, Types.INTEGER);
            ps.setTimestamp(3, Timestamp.valueOf(pt.getNgayTraDuKien()));
            ps.setDouble(4, pt.getTienCoc());
            ps.executeUpdate();

            ResultSet gk = ps.getGeneratedKeys();
            if (!gk.next()) return false;
            pt.setMaPT(gk.getInt(1)); // lưu lại MaPT để dùng sau

            // Insert CTPHIEUTHUE
            for (PhieuThue.CTPhieuThue ct : pt.getDanhSachChiTiet()) {
                String sqlCT = "INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psCT = con.prepareStatement(sqlCT)) {
                    psCT.setInt(1, pt.getMaPT());
                    psCT.setInt(2, ct.getMaCD());
                    if (ct.getMaNV() > 0) psCT.setInt(3, ct.getMaNV());
                    else psCT.setNull(3, Types.INTEGER);
                    psCT.setDouble(4, ct.getDonGiaThue());
                    psCT.executeUpdate();
                }
            }
            return true;
        }
    }

    // ================= UPDATE KH + NV (dùng cho RentEditDialog) =================
    public boolean updateKhachHangVaNhanVien(int maPT, int maKH, int maNV) {
        String sqlPT = "UPDATE PHIEUTHUE SET MaKH = ? WHERE MaPT = ?";
        String sqlCT = "UPDATE CTPHIEUTHUE SET MaNV = ? WHERE MaPT = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(sqlPT)) {
                ps1.setInt(1, maKH);
                ps1.setInt(2, maPT);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = con.prepareStatement(sqlCT)) {
                ps2.setInt(1, maNV);
                ps2.setInt(2, maPT);
                ps2.executeUpdate();
            }
            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
// Thêm vào class PhieuThueDAO
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Cập nhật NgayTraDuKien của phiếu thuê và tính lại DonGiaThue
 * cho từng dòng ChiTietPhieuThue thuộc phiếu đó.
 *
 * Công thức (mỗi CTPT):
 *   soNgayMoi  = ngayTraMoi − NgayThue  (số ngày)
 *   donGiaMoi  = soNgayMoi × GiaThueNgay
 *   tienGiaHan = MAX(0, donGiaMoi − DonGiaThue_cũ)
 *   DonGiaThue_mới = donGiaMoi − tienGiaHan − TienGiamDiem
 *
 * @param maPT      Mã phiếu thuê cần cập nhật
 * @param ngayTraMoi Ngày trả dự kiến mới (đã validate > NgayThue)
 * @return true nếu cập nhật thành công toàn bộ, false nếu có lỗi (đã rollback)
 */
    // ── Trong PhieuThueDAO ──────────────────────────────────────────────────────

    public boolean updateNgayTraVaDonGia(int maPT, LocalDateTime ngayTraMoi) {

        final String SQL_SELECT_CTPT =
            "SELECT ct.MaCD, ct.DonGiaThue, sp.GiaThueNgay, pt.NgayThue, pt.NgayTraDuKien " +
            "FROM   CTPHIEUTHUE ct " +
            "JOIN   PHIEUTHUE   pt ON pt.MaPT = ct.MaPT " +
            "JOIN   CD          cd ON cd.MaCD = ct.MaCD " +
            "JOIN   SANPHAM     sp ON sp.MaSP = cd.MaSP " +
            "WHERE  ct.MaPT = ?";

        final String SQL_UPDATE_PT =
            "UPDATE PHIEUTHUE SET NgayTraDuKien = ? WHERE MaPT = ?";

        final String SQL_UPDATE_CTPT =
            "UPDATE CTPHIEUTHUE SET DonGiaThue = ? WHERE MaPT = ? AND MaCD = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Bước 1: đọc CTPT trước khi cập nhật NgayTraDuKien
            // (để lấy NgayTraDuKien HIỆN TẠI còn đúng trong DB)
            List<int[]>    maCDList       = new ArrayList<>();
            List<double[]> donGiaFinalList = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(SQL_SELECT_CTPT)) {
                ps.setInt(1, maPT);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int    maCD        = rs.getInt("MaCD");
                        double donGiaCu    = rs.getDouble("DonGiaThue");
                        double giaThueNgay = rs.getDouble("GiaThueNgay");

                        java.sql.Timestamp tsNgayThue  = rs.getTimestamp("NgayThue");
                        java.sql.Timestamp tsNgayTraCu = rs.getTimestamp("NgayTraDuKien");
                        LocalDateTime ngayThue  = tsNgayThue  != null ? tsNgayThue.toLocalDateTime()  : LocalDateTime.now();
                        LocalDateTime ngayTraCu = tsNgayTraCu != null ? tsNgayTraCu.toLocalDateTime() : LocalDateTime.now();


                        // Bước 1: tính tiền gia hạn đã thu trước đó (nếu có)
                        long soNgayCu  = ChronoUnit.DAYS.between(
                            ngayThue.toLocalDate().atStartOfDay(),
                            ngayTraCu.toLocalDate().atStartOfDay()
                        );
                        double tienGiaHan = Math.max(0, soNgayCu * giaThueNgay - donGiaCu);

                        // Bước 2: tính đơn giá mới

                        long soNgayMoi = ChronoUnit.DAYS.between(
                            ngayThue.toLocalDate().atStartOfDay(),
                            ngayTraMoi.toLocalDate().atStartOfDay()
                        );
                        double donGiaFinal = soNgayMoi * giaThueNgay - tienGiaHan;

                        System.out.printf(
                            "[CD%d] ngayTraCu=%s soNgayCu=%d tienGiaHan=%.0f " +
                            "soNgayMoi=%d donGiaFinal=%.0f%n",
                            maCD, ngayTraCu, soNgayCu, tienGiaHan, soNgayMoi, donGiaFinal
                        );

                        maCDList.add(new int[]{maCD});
                        donGiaFinalList.add(new double[]{donGiaFinal});
                    }
                }
            }

            // Bước 2: cập nhật NgayTraDuKien trên PHIEUTHUE
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_PT)) {
                ps.setTimestamp(1, Timestamp.valueOf(ngayTraMoi));
                ps.setInt(2, maPT);
                ps.executeUpdate();
            }

            // Bước 3: batch update DonGiaThue trên CTPHIEUTHUE
            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_CTPT)) {
                for (int i = 0; i < maCDList.size(); i++) {
                    ps.setDouble(1, donGiaFinalList.get(i)[0]);
                    ps.setInt(2, maPT);
                    ps.setInt(3, maCDList.get(i)[0]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public int tinhDiemPhieu(int maPT) {
        String sql = "SELECT SUM(DonGiaThue) AS TongTien FROM CTPHIEUTHUE WHERE MaPT = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPT);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double tongTien = rs.getDouble("TongTien");
                return (int) (tongTien / 100000); // 100K = 1 điểm
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /* ================= EXTEND RENTAL ================= */

    /**
     * Gia hạn phiếu thuê — SQL Server syntax (DATEADD, ISNULL).
     *
     * Logic đúng:
     *   NgayTraDuKien += soNgay   (đẩy ngày trả ra)
     *   TienPhat      += phatTre + phiGiaHan   (ghi nhận phí đã thu)
     *   TienCoc       KHÔNG THAY ĐỔI           (giữ đến khi trả CD)
     *
     * @param maPT       mã phiếu thuê
     * @param soNgay     số ngày gia hạn thêm
     * @param phatTre    phí phạt trễ hiện tại (0 nếu không trễ)
     * @param phiGiaHan  phí gia hạn = tổng đơn giá thuê × soNgay
     */
    public boolean extendRental(int maPT, int soNgay, double phatTre, double phiGiaHan) {
        String sql =
            "UPDATE PHIEUTHUE " +
            "SET NgayTraDuKien = DATEADD(DAY, ?, NgayTraDuKien), " +
            "    TienPhat      = ISNULL(TienPhat, 0) + ? " +
            // TienCoc KHÔNG có trong SET — giữ nguyên
            "WHERE MaPT = ? AND TrangThai = N'DangThue'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, soNgay);
            ps.setDouble(2, phatTre + phiGiaHan);
            ps.setInt(3, maPT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /* ================= MAPPER ================= */

    private PhieuThue map(ResultSet rs) throws SQLException {
        PhieuThue pt = new PhieuThue();
        pt.setMaPT(rs.getInt("MaPT"));
        pt.setMaKH(rs.getInt("MaKH")); 
        pt.setTenKhachHang(rs.getString("TenKH"));
        pt.setTenNhanVien(rs.getString("TenNV"));

        Timestamp t1 = rs.getTimestamp("NgayThue");
        if (t1 != null) pt.setNgayThue(t1.toLocalDateTime());

        Timestamp t2 = rs.getTimestamp("NgayTraDuKien");
        if (t2 != null) pt.setNgayTraDuKien(t2.toLocalDateTime());

        Timestamp t3 = rs.getTimestamp("NgayTraThucTe");
        if (t3 != null) pt.setNgayTraThucTe(t3.toLocalDateTime());

        pt.setTienCoc(rs.getDouble("TienCoc"));
        pt.setTienPhat(rs.getDouble("TienPhat"));
        pt.setTrangThai(rs.getString("TrangThai"));
        return pt;
    }
}
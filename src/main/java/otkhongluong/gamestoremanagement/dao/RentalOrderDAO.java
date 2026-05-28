package otkhongluong.gamestoremanagement.dao;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import otkhongluong.gamestoremanagement.model.RentalOrder;
import otkhongluong.gamestoremanagement.model.CTPhieuThue;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalOrderDAO {
    private static final int VND_PER_DIEM = 100_000;

    /* ================= INSERT ================= */

    public boolean insert(RentalOrder pt) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                boolean ok = insertWithConnection(pt, con);
                if (!ok) { con.rollback(); return false; }
                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ================= INSERT WITH CONNECTION ================= */

    public boolean insertWithConnection(RentalOrder pt, Connection con) throws SQLException {
        String sql =
            "INSERT INTO PHIEUTHUE (MaKH, NgayThue, NgayTraDuKien, TienCoc, TienPhat, TrangThai) " +
            "VALUES (?, GETDATE(), ?, ?, 0, N'DangThue')";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pt.getMaKH());
            ps.setTimestamp(2, Timestamp.valueOf(pt.getNgayTraDuKien()));
            ps.setDouble(3, pt.getTienCoc());
            ps.executeUpdate();

            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (!gk.next()) return false;
                pt.setMaPT(gk.getInt(1));
            }

            for (CTPhieuThue ct : pt.getDanhSachChiTiet()) {
                String sqlCT = "INSERT INTO CTPHIEUTHUE (MaPT, MaCD, MaNV, DonGiaThue) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psCT = con.prepareStatement(sqlCT)) {
                    psCT.setInt(1, pt.getMaPT());
                    psCT.setInt(2, ct.getMaCD());
                    if (ct.getMaNV() > 0) psCT.setInt(3, ct.getMaNV());
                    else                  psCT.setNull(3, Types.INTEGER);
                    psCT.setDouble(4, ct.getDonGiaThue());
                    psCT.executeUpdate();
                }
            }
            return true;
        }
    }

    /* ================= FIND ALL ================= */

    public List<RentalOrder> findAll() {
        List<RentalOrder> list = new ArrayList<>();
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
                RentalOrder pt = new RentalOrder();
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

    public RentalOrder findById(int id) {
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
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RentalOrder pt = map(rs);
                    pt.setDanhSachChiTiet(getChiTiet(con, id));
                    return pt;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* ================= LOAD DETAIL ================= */

    private List<CTPhieuThue> getChiTiet(Connection con, int maPT) {
        List<CTPhieuThue> list = new ArrayList<>();
        String sql =
            "SELECT g.TenGame, ct.MaCD, ct.DonGiaThue, cd.TrangThai, sp.GiaThueNgay " +
            "FROM CTPHIEUTHUE ct " +
            "JOIN CD cd ON ct.MaCD = cd.MaCD " +
            "JOIN SANPHAM sp ON cd.MaSP = sp.MaSP " +
            "JOIN GAME g ON sp.MaGame = g.MaGame " +
            "WHERE ct.MaPT = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPT);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CTPhieuThue ct = new CTPhieuThue(
                    rs.getInt("MaCD"),
                    rs.getString("TenGame"),
                    rs.getDouble("DonGiaThue"),
                    rs.getString("TrangThai")
                );
                ct.setGiaThueNgay(rs.getDouble("GiaThueNgay"));
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

    // FIX BUG 4: Đã XÓA method updateCDStatusAfterReturn() vì là dead code.
    // returnCDFull() trong RentalService không gọi method này.
    // Việc cập nhật CD.TrangThai được xử lý bởi Trigger TRG_CAP_NHAT_TRANGTHAI_CD_KHI_TRA.

    /* ================= UPDATE ================= */

    public boolean update(RentalOrder pt) {
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
                ps0.setInt(1, id); ps0.executeUpdate();
            }
            try (PreparedStatement ps1 = con.prepareStatement(sqlChiTiet)) {
                ps1.setInt(1, id); ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = con.prepareStatement(sqlPhieu)) {
                ps2.setInt(1, id);
                if (ps2.executeUpdate() == 0) { con.rollback(); return false; }
            }

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ================= UPDATE KH + NV ================= */

    public boolean updateKhachHangVaNhanVien(int maPT, int maKH, int maNV) {
        String sqlPT = "UPDATE PHIEUTHUE SET MaKH = ? WHERE MaPT = ?";
        String sqlCT = "UPDATE CTPHIEUTHUE SET MaNV = ? WHERE MaPT = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(sqlPT)) {
                ps1.setInt(1, maKH); ps1.setInt(2, maPT); ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = con.prepareStatement(sqlCT)) {
                ps2.setInt(1, maNV); ps2.setInt(2, maPT); ps2.executeUpdate();
            }
            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ================= UPDATE NGAY TRA + DON GIA ================= */

    /**
     * FIX BUG 5: Đơn giản hóa công thức tính DonGiaFinal.
     *
     * Công thức cũ:
     *   tienGiaHan = max(0, soNgayCu * giaThueNgay - donGiaCu)
     *   donGiaFinal = soNgayMoi * giaThueNgay - tienGiaHan
     * → Sai khi DonGiaThue ban đầu đã bị giảm do điểm tích lũy
     *   (tienGiaHan > 0 làm donGiaFinal bị khấu trừ oan)
     *
     * Công thức mới: donGiaFinal = soNgayMoi * giaThueNgay
     * DonGiaThue trong CTPHIEUTHUE luôn phản ánh tổng tiền thuê theo số ngày,
     * không lưu discount điểm (discount điểm chỉ ghi vào DIEM_LICHSU).
     */
    public boolean updateNgayTraVaDonGia(int maPT, LocalDateTime ngayTraMoi) {
        final String SQL_SELECT_CTPT =
            "SELECT ct.MaCD, sp.GiaThueNgay, pt.NgayThue " +
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

            List<int[]>    maCDList        = new ArrayList<>();
            List<double[]> donGiaFinalList = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(SQL_SELECT_CTPT)) {
                ps.setInt(1, maPT);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int    maCD        = rs.getInt("MaCD");
                        double giaThueNgay = rs.getDouble("GiaThueNgay");
                        LocalDateTime ngayThue = rs.getTimestamp("NgayThue") != null
                            ? rs.getTimestamp("NgayThue").toLocalDateTime() : LocalDateTime.now();

                        // FIX BUG 5: Tính lại hoàn toàn từ GiaThueNgay * số ngày mới.
                        // Không trừ "tienGiaHan" nữa vì discount điểm không nằm trong DonGiaThue.
                        long soNgayMoi = ChronoUnit.DAYS.between(
                            ngayThue.toLocalDate().atStartOfDay(),
                            ngayTraMoi.toLocalDate().atStartOfDay());
                        double donGiaFinal = Math.max(0, soNgayMoi) * giaThueNgay;

                        maCDList.add(new int[]{maCD});
                        donGiaFinalList.add(new double[]{donGiaFinal});
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_PT)) {
                ps.setTimestamp(1, Timestamp.valueOf(ngayTraMoi));
                ps.setInt(2, maPT);
                ps.executeUpdate();
            }

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
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    /* ================= GET TONG TIEN ================= */

    public double getTongTienByMaPT(int maPT) {
        String sql = "SELECT ISNULL(SUM(DonGiaThue), 0) FROM CTPHIEUTHUE WHERE MaPT = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    /* ================= TINH DIEM PHIEU ================= */

    public int tinhDiemPhieu(int maPT) {
        double tongTien = getTongTienByMaPT(maPT);
        return (int) (tongTien / VND_PER_DIEM);
    }

    /* ================= EXTEND RENTAL ================= */

    public boolean extendRental(int maPT, LocalDateTime ngayTraMoi, double phatTre, double phiGiaHan) {
        // FIX: Lưu tổng (phatTre + phiGiaHan) vào TienPhat thay vì chỉ phatTre.
        // Lý do: returnCDFull() cộng pt.getTienPhat() vào tienPhatChot khi trả CD.
        // Nếu chỉ lưu phatTre, toàn bộ phiGiaHan đã thu tại quầy bị mất khỏi quyết toán.
        String sql =
            "UPDATE PHIEUTHUE " +
            "SET NgayTraDuKien = ?, " +
            "    TienPhat      = ISNULL(TienPhat, 0) + ? " +
            "WHERE MaPT = ? AND TrangThai = N'DangThue'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(ngayTraMoi));
            ps.setDouble(2, phatTre + phiGiaHan);   // ← đây là dòng duy nhất thay đổi
            ps.setInt(3, maPT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /* ================= MAPPER ================= */

    private RentalOrder map(ResultSet rs) throws SQLException {
        RentalOrder pt = new RentalOrder();
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
package otkhongluong.gamestoremanagement.dao;

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
        String sqlChiTiet = "DELETE FROM CTPHIEUTHUE WHERE MaPT = ?";
        String sqlPhieu   = "DELETE FROM PHIEUTHUE WHERE MaPT = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(sqlChiTiet)) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = con.prepareStatement(sqlPhieu)) {
                ps2.setInt(1, id);
                ps2.executeUpdate();
            }
            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.Disc;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiscDAO {

    // ================= UPDATE TRẠNG THÁI (có sẵn) =================
    public boolean updateTrangThai(int maCD, String trangThai) {
        String sql = "UPDATE CD SET TrangThai = ? WHERE MaCD = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, maCD);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= GIÁ THUÊ THEO MACD (có sẵn) =================
    public double getGiaThueByMaCD(int maCD) {
        String sql =
            "SELECT sp.GiaThueNgay " +
            "FROM CD cd JOIN SANPHAM sp ON cd.MaSP = sp.MaSP " +
            "WHERE cd.MaCD = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maCD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ================= CD SẴN SÀNG (có sẵn) =================
    public List<Object[]> getAllAvailableCD() {
        String sql =
            "SELECT cd.MaCD, g.TenGame, sp.GiaThueNgay " +
            "FROM CD cd " +
            "JOIN SANPHAM sp ON cd.MaSP = sp.MaSP " +
            "JOIN GAME g ON sp.MaGame = g.MaGame " +
            "WHERE cd.TrangThai = N'SanSang' " +
            "  AND cd.TinhTrang != N'Hỏng' " +  // ← THÊM
            "  AND cd.IsDeleted = 0 " +
            "  AND sp.GiaThueNgay IS NOT NULL " +
            "  AND sp.GiaThueNgay > 0 " +
            "ORDER BY g.TenGame";
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("MaCD"),
                    rs.getString("TenGame"),
                    rs.getDouble("GiaThueNgay")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= [MỚI] FIND ALL BY MASP =================
    public List<Disc> findByMaSP(int maSP) {
        List<Disc> list = new ArrayList<>();
        String sql = "SELECT * FROM CD WHERE MaSP = ? AND IsDeleted = 0 ORDER BY MaCD";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSP);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= [MỚI] INSERT (thêm 1 đĩa) =================
    public boolean insert(Disc disc) {
        String sql = "INSERT INTO CD (MaSP, TinhTrang, TrangThai) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, disc.getMaSP());
            ps.setString(2, disc.getTinhTrang());
            ps.setString(3, disc.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= [MỚI] UPDATE TÌNH TRẠNG =================
    public boolean updateTinhTrang(int maCD, String tinhTrang) {
        String sql = "UPDATE CD SET TinhTrang = ? WHERE MaCD = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tinhTrang);
            ps.setInt(2, maCD);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= [MỚI] DELETE (chỉ xóa khi SanSang) =================
    public boolean deleteByMaCD(int maCD) {
        // Chỉ soft delete đĩa đang SanSang
        String sql = "UPDATE CD SET IsDeleted = 1 WHERE MaCD = ? AND TrangThai = N'SanSang'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maCD);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= [MỚI] COUNT TỔNG =================
    public int countByMaSP(int maSP) {
        String sql = "SELECT COUNT(*) FROM CD WHERE MaSP = ? AND IsDeleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSP);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ================= [MỚI] COUNT SẴN SÀNG =================
    public int countSanSangByMaSP(int maSP) {
        String sql = "SELECT COUNT(*) FROM CD WHERE MaSP = ? AND TrangThai = N'SanSang' AND IsDeleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSP);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ================= MAPPER =================
    private Disc map(ResultSet rs) throws SQLException {
        return new Disc(
                rs.getInt("MaCD"),
                rs.getInt("MaSP"),
                rs.getString("TinhTrang"),
                rs.getString("TrangThai")
        );
    }
}
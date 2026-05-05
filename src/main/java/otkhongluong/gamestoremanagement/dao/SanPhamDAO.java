package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.SanPham;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAO {

    // ================= INSERT =================
    public boolean insert(SanPham sp) {

        String sql =
                "INSERT INTO SANPHAM (MaGame, GiaBan, GiaThueNgay) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sp.getMaGame());
            ps.setDouble(2, sp.getGiaBan());
            ps.setDouble(3, sp.getGiaThueNgay());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= UPDATE =================
    public boolean update(SanPham sp) {

        String sql =
                "UPDATE SANPHAM SET MaGame = ?, GiaBan = ?, GiaThueNgay = ? WHERE MaSP = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sp.getMaGame());
            ps.setDouble(2, sp.getGiaBan());
            ps.setDouble(3, sp.getGiaThueNgay());
            ps.setInt(4, sp.getMaSP());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= DELETE =================
    public boolean delete(int maSP) {

        String sql = "DELETE FROM SANPHAM WHERE MaSP = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maSP);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= FIND BY ID =================
    public SanPham findById(int maSP) {

        String sql = "SELECT * FROM SANPHAM WHERE MaSP = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maSP);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return map(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= FIND ALL =================
    public List<SanPham> findAll() {

        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SANPHAM ORDER BY MaSP DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= FIND BY GAME =================
    public List<SanPham> findByMaGame(int maGame) {

        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM SANPHAM WHERE MaGame = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maGame);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= MAPPER =================
    private SanPham map(ResultSet rs) throws SQLException {

        SanPham sp = new SanPham();

        sp.setMaSP(rs.getInt("MaSP"));
        sp.setMaGame(rs.getInt("MaGame"));
        sp.setGiaBan(rs.getDouble("GiaBan"));
        sp.setGiaThueNgay(rs.getDouble("GiaThueNgay"));

        return sp;
    }
}
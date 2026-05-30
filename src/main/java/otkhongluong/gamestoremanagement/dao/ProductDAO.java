package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.Product;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // ================= INSERT =================
    public boolean insert(Product sp) {

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
    public boolean update(Product sp) {

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
        String sql = "UPDATE SANPHAM SET IsDeleted = 1 WHERE MaSP = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSP);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= FIND BY ID =================
    public Product findById(int maSP) {

        String sql = "SELECT * FROM SANPHAM WHERE MaSP = ? AND IsDeleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maSP);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= FIND ALL =================
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql =
            "SELECT sp.MaSP, sp.MaGame, sp.GiaBan, sp.GiaThueNgay, " +
            "  CASE WHEN r.MaSP IS NOT NULL THEN 1 ELSE 0 END AS HasRom, " +
            "  CASE WHEN COUNT(cd.MaCD) > 0  THEN 1 ELSE 0 END AS HasCd " +
            "FROM SANPHAM sp " +
            "LEFT JOIN ROM r  ON r.MaSP  = sp.MaSP AND r.IsDeleted = 0 " +
            "LEFT JOIN CD  cd ON cd.MaSP = sp.MaSP AND cd.IsDeleted = 0 " +
            "WHERE sp.IsDeleted = 0 " +
            "GROUP BY sp.MaSP, sp.MaGame, sp.GiaBan, sp.GiaThueNgay, r.MaSP " +
            "ORDER BY sp.MaSP DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product sp = new Product();
                sp.setMaSP(rs.getInt("MaSP"));
                sp.setMaGame(rs.getInt("MaGame"));
                sp.setGiaBan(rs.getDouble("GiaBan"));
                sp.setGiaThueNgay(rs.getDouble("GiaThueNgay"));
                sp.setHasRom(rs.getInt("HasRom") == 1);
                sp.setHasCd(rs.getInt("HasCd")  == 1);
                list.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= FIND BY GAME =================
    public List<Product> findByMaGame(int maGame) {

        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM SANPHAM WHERE MaGame = ? AND IsDeleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maGame);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= MAPPER =================
    private Product map(ResultSet rs) throws SQLException {

        Product sp = new Product();

        sp.setMaSP(rs.getInt("MaSP"));
        sp.setMaGame(rs.getInt("MaGame"));
        sp.setGiaBan(rs.getDouble("GiaBan"));
        sp.setGiaThueNgay(rs.getDouble("GiaThueNgay"));

        return sp;
    }
}
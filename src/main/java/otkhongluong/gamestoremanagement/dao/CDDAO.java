package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.util.DBConnection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CDDAO {

    // ================= UPDATE TRẠNG THÁI =================
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
    
        public double getGiaThueByMaCD(int maCD) {

        String sql =
            "SELECT sp.GiaThueNgay " +
            "FROM CD cd " +
            "JOIN SANPHAM sp ON cd.MaSP = sp.MaSP " +
            "WHERE cd.MaCD = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, maCD);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
        
    public List<Object[]> getAllAvailableCD() {

        String sql =
            "SELECT cd.MaCD, g.TenGame, sp.GiaThueNgay " +
            "FROM CD cd " +
            "JOIN SANPHAM sp ON cd.MaSP = sp.MaSP " +
            "JOIN GAME g ON sp.MaGame = g.MaGame " +
            "WHERE cd.TrangThai = N'SanSang'";

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
}
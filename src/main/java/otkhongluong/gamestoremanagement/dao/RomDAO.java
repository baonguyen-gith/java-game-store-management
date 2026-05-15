package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.ROM;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;

public class RomDAO {

    // ================= FIND BY MASP =================
    public ROM findByMaSP(int maSP) {
        String sql = "SELECT * FROM ROM WHERE MaSP = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSP);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ================= INSERT =================
    public boolean insert(ROM rom) {
        String sql = "INSERT INTO ROM (MaSP, DungLuong, LinkLuuTru, SoLuotBan) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rom.getMaSP());
            ps.setString(2, rom.getDungLuong());
            ps.setString(3, rom.getLinkLuuTru());
            ps.setInt(4, rom.getSoLuotBan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= UPDATE =================
    public boolean update(ROM rom) {
        String sql = "UPDATE ROM SET DungLuong = ?, LinkLuuTru = ?, SoLuotBan = ? WHERE MaSP = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rom.getDungLuong());
            ps.setString(2, rom.getLinkLuuTru());
            ps.setInt(3, rom.getSoLuotBan());
            ps.setInt(4, rom.getMaSP());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= DELETE =================
    public boolean delete(int maSP) {
        String sql = "DELETE FROM ROM WHERE MaSP = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSP);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= EXISTS =================
    public boolean existsByMaSP(int maSP) {
        String sql = "SELECT 1 FROM ROM WHERE MaSP = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSP);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= MAPPER =================
    private ROM map(ResultSet rs) throws SQLException {
        return new ROM(
                rs.getInt("MaSP"),
                rs.getString("DungLuong"),
                rs.getString("LinkLuuTru"),
                rs.getInt("SoLuotBan")
        );
    }
}
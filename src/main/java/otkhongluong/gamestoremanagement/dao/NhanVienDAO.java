package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    // ================= GET ALL NAMES (for autocomplete) =================
    public List<String> getAllTenNhanVien() {

        List<String> list = new ArrayList<>();

        String sql = "SELECT HoTen FROM NHANVIEN ORDER BY HoTen";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("HoTen"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= GET ID BY NAME =================
    public int getMaNVByName(String tenNV) {

        String sql = "SELECT MaNV FROM NHANVIEN WHERE HoTen = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tenNV);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("MaNV");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // không tìm thấy
    }

    // ================= OPTIONAL: SEARCH LIKE (nâng cấp autocomplete) =================
    public List<String> searchByName(String keyword) {

        List<String> list = new ArrayList<>();

        String sql = "SELECT HoTen FROM NHANVIEN WHERE LOWER(HoTen) LIKE ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword.toLowerCase() + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getString("HoTen"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
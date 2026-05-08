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
    
    // ================= FIND BY ID =================
    public otkhongluong.gamestoremanagement.model.NhanVien findById(int maNV) {
        String sql = "SELECT MaNV, HoTen FROM NHANVIEN WHERE MaNV = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                otkhongluong.gamestoremanagement.model.NhanVien nv =
                    new otkhongluong.gamestoremanagement.model.NhanVien();
                nv.setMaNV(rs.getInt("MaNV"));
                nv.setHoTen(rs.getString("HoTen"));
                return nv;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
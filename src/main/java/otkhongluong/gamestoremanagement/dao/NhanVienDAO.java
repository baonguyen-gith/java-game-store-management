package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.NhanVien;
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

    // ================= CRUD OPERATIONS =================
    public List<NhanVien> findAll() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NHANVIEN ORDER BY MaNV DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToNhanVien(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public NhanVien findById(int maNV) {
        String sql = "SELECT * FROM NHANVIEN WHERE MaNV = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToNhanVien(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(NhanVien nv) {
        String sql = "INSERT INTO NHANVIEN (HoTen, SDT, NgaySinh, CCCD, NgayVaoLam) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getHoTen());
            ps.setString(2, nv.getSdt());
            ps.setDate(3, nv.getNgaySinh() != null ? java.sql.Date.valueOf(nv.getNgaySinh()) : null);
            ps.setString(4, nv.getCccd());
            ps.setDate(5, nv.getNgayVaoLam() != null ? java.sql.Date.valueOf(nv.getNgayVaoLam()) : null);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(NhanVien nv) {
        String sql = "UPDATE NHANVIEN SET HoTen = ?, SDT = ?, NgaySinh = ?, CCCD = ?, NgayVaoLam = ? WHERE MaNV = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getHoTen());
            ps.setString(2, nv.getSdt());
            ps.setDate(3, nv.getNgaySinh() != null ? java.sql.Date.valueOf(nv.getNgaySinh()) : null);
            ps.setString(4, nv.getCccd());
            ps.setDate(5, nv.getNgayVaoLam() != null ? java.sql.Date.valueOf(nv.getNgayVaoLam()) : null);
            ps.setInt(6, nv.getMaNV());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int maNV) {
        String sql = "DELETE FROM NHANVIEN WHERE MaNV = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private NhanVien mapResultSetToNhanVien(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getInt("MaNV"));
        nv.setHoTen(rs.getString("HoTen"));
        nv.setSdt(rs.getString("SDT"));
        
        Date ngaySinh = rs.getDate("NgaySinh");
        if (ngaySinh != null) nv.setNgaySinh(ngaySinh.toLocalDate());
        
        nv.setCccd(rs.getString("CCCD"));
        
        Date ngayVaoLam = rs.getDate("NgayVaoLam");
        if (ngayVaoLam != null) nv.setNgayVaoLam(ngayVaoLam.toLocalDate());
        
        return nv;
    }
}
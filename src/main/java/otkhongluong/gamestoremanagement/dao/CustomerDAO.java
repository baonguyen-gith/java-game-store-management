package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    // ================= INSERT =================
    public boolean insert(Customer kh) {

        String sql = "INSERT INTO KHACHHANG (HoTen, SDT, CCCD, Email, DiaChi, DiemTichLuy) " +
                     "VALUES (?, ?, ?, ?, ?, 0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kh.getHoTen());
            ps.setString(2, kh.getSdt());
            ps.setString(3, kh.getCccd());
            ps.setString(4, kh.getEmail());
            ps.setString(5, kh.getDiaChi());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Lỗi SQL cụ thể: " + e.getMessage()); // Thêm dòng này để xem lỗi ở Console
            e.printStackTrace();
        }

        return false;
    }

    // ================= UPDATE =================
    public boolean update(Customer kh) {

        String sql = "UPDATE KHACHHANG SET HoTen=?, SDT=?, CCCD=?, Email=?, DiaChi=?, DiemTichLuy=? " +
                     "WHERE MaKH=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kh.getHoTen());
            ps.setString(2, kh.getSdt());
            ps.setString(3, kh.getCccd());
            ps.setString(4, kh.getEmail());
            ps.setString(5, kh.getDiaChi());
            ps.setInt(6, kh.getDiemTichLuy());
            ps.setInt(7, kh.getMaKH());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= DELETE =================
    public boolean delete(int maKH) {

        String sql = "DELETE FROM KHACHHANG WHERE MaKH = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maKH);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Không thể xóa khách hàng (liên quan HOADON / PHIEUTHUE)");
            e.printStackTrace();
        }

        return false;
    }

    // ================= FIND BY ID =================
    public Customer findById(int maKH) {

        String sql = "SELECT * FROM KHACHHANG WHERE MaKH = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maKH);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= FIND ALL =================
    public List<Customer> findAll() {

        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM KHACHHANG ORDER BY MaKH DESC";

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
    
       public List<String> getAllTenKhachHang() {

        List<String> list = new ArrayList<>();

        String sql = "SELECT HoTen FROM KHACHHANG";

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
       
       // ================= FIND BY SDT =================
    public Customer findBySDT(String sdt) {

        String sql = "SELECT * FROM KHACHHANG WHERE SDT = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sdt);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);   // map() đã đọc đủ các field
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= UPDATE POINT =================
    public void updatePoint(int maKH, int delta) {

        String sql =
            "UPDATE KHACHHANG " +
            "SET DiemTichLuy = DiemTichLuy + ? " +
            "WHERE MaKH = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, delta);
            ps.setInt(2, maKH);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SEARCH =================
    public List<Customer> search(String keyword) {

        List<Customer> list = new ArrayList<>();

        String sql = "SELECT * FROM KHACHHANG " +
                     "WHERE LOWER(HoTen) LIKE ? " +
                     "OR SDT LIKE ? " +
                     "OR LOWER(Email) LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String key = "%" + keyword.toLowerCase() + "%";

            ps.setString(1, key);
            ps.setString(2, key);
            ps.setString(3, key);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public void truDiem(int maKH, int diem) {
        String sql =
            "UPDATE KHACHHANG " +
            "SET DiemTichLuy = CASE WHEN DiemTichLuy - ? < 0 THEN 0 ELSE DiemTichLuy - ? END " +
            "WHERE MaKH = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, diem);
            ps.setInt(2, diem);
            ps.setInt(3, maKH);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= MAPPER =================
    private Customer map(ResultSet rs) throws SQLException {

        Customer kh = new Customer();

        kh.setMaKH(rs.getInt("MaKH"));
        kh.setHoTen(rs.getString("HoTen"));
        kh.setSdt(rs.getString("SDT"));
        kh.setCccd(rs.getString("CCCD"));
        kh.setEmail(rs.getString("Email"));
        kh.setDiaChi(rs.getString("DiaChi"));
        kh.setDiemTichLuy(rs.getInt("DiemTichLuy"));

        return kh;
    }
}
package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.util.DBConnection;
import otkhongluong.gamestoremanagement.util.FormatUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FIX: dùng FormatUtil.formatMaNV() thay vì tự format "NV" + maNV inline.
 */
public class UserDAO {

    public boolean insert(User user) {
        String sql = "INSERT INTO USERS (Username, Password, MaRole, MaNV) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getMaRole());
            if (user.getMaNV() > 0) ps.setInt(4, user.getMaNV());
            else                    ps.setNull(4, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi thêm user: " + e.getMessage(), e);
        }
    }

    public boolean update(User user) {
        String sql = "UPDATE USERS SET Username = ?, Password = ?, MaRole = ?, MaNV = ? WHERE MaUser = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getMaRole());
            if (user.getMaNV() > 0) ps.setInt(4, user.getMaNV());
            else                    ps.setNull(4, Types.INTEGER);
            ps.setInt(5, user.getMaUser());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi cập nhật user: " + e.getMessage(), e);
        }
    }

    public boolean delete(int maUser) {
        String sql = "DELETE FROM USERS WHERE MaUser = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maUser);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi xóa user: " + e.getMessage(), e);
        }
    }

    public User findById(int maUser) {
        String sql = "SELECT * FROM USERS WHERE MaUser = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi tìm user theo ID: " + e.getMessage(), e);
        }
        return null;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM USERS WHERE Username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi tìm user theo username: " + e.getMessage(), e);
        }
        return null;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM USERS ORDER BY MaUser DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi lấy danh sách user: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Object[]> findAllWithEmployee() {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT u.MaUser, u.Username, u.MaRole, u.MaNV, nv.HoTen " +
            "FROM USERS u " +
            "LEFT JOIN NHANVIEN nv ON u.MaNV = nv.MaNV " +
            "ORDER BY u.MaUser DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int maNV = rs.getInt("MaNV");
                String hoTen = rs.getString("HoTen");
                if (hoTen == null) hoTen = "—";

                list.add(new Object[]{
                    rs.getInt("MaUser"),
                    rs.getString("Username"),
                    rs.getInt("MaRole") == 1 ? "Admin" : "Staff",
                    FormatUtil.formatMaNV(maNV),    // FIX: dùng FormatUtil
                    hoTen
                });
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi lấy danh sách user + nhân viên: " + e.getMessage(), e);
        }
        return list;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("MaUser"),
            rs.getString("Username"),
            rs.getString("Password"),
            rs.getInt("MaRole"),
            rs.getInt("MaNV")
        );
    }
    
    public List<Object[]> findNhanVienDropdown(int maNVEditDang) {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT nv.MaNV, nv.HoTen, " +
            "       CASE WHEN u.MaNV IS NOT NULL THEN 1 ELSE 0 END AS DaCoTK " +
            "FROM NHANVIEN nv " +
            "LEFT JOIN USERS u ON nv.MaNV = u.MaNV " +
            "WHERE u.MaNV IS NULL OR nv.MaNV = ? " +
            "ORDER BY nv.MaNV";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNVEditDang < 0 ? -1 : maNVEditDang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int maNV = rs.getInt("MaNV");
                    list.add(new Object[]{
                        maNV,
                        FormatUtil.formatMaNV(maNV),
                        rs.getString("HoTen"),
                        rs.getInt("DaCoTK") == 1
                    });
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi lấy dropdown nhân viên: " + e.getMessage(), e);
        }
        return list;
    }
}
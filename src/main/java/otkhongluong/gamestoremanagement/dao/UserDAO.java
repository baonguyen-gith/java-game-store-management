package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Triển khai IUserDAO — chỉ biết SQL, không biết business logic.
 *
 * Thay đổi so với bản cũ:
 *  - implements IUserDAO (có thể mock khi unit test)
 *  - throw DatabaseException thay vì e.printStackTrace() (caller biết lỗi)
 */
public class UserDAO implements IUserDAO {

    @Override
    public boolean insert(User user) {
        String sql = "INSERT INTO USERS (Username, Password, MaRole) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getMaRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi thêm user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE USERS SET Username = ?, Password = ?, MaRole = ? WHERE MaUser = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getMaRole());
            ps.setInt(4, user.getMaUser());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Lỗi khi cập nhật user: " + e.getMessage(), e);
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("MaUser"),
            rs.getString("Username"),
            rs.getString("Password"),
            rs.getInt("MaRole")
        );
    }
}
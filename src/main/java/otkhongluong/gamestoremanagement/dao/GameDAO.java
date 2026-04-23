package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {
    public boolean insert(Game game) {
        String sql = "INSERT INTO GAME (TenGame, TheLoai, NenTang, GiaBan, GiaThueNgay) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, game.getTenGame());
            ps.setString(2, game.getTheLoai());
            ps.setString(3, game.getNenTang());
            ps.setDouble(4, game.getGiaBan());
            ps.setDouble(5, game.getGiaThueNgay());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Game game) {
        String sql = "UPDATE GAME SET TenGame = ?, TheLoai = ?, NenTang = ?, GiaBan = ?, GiaThueNgay = ? WHERE MaGame = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, game.getTenGame());
            ps.setString(2, game.getTheLoai());
            ps.setString(3, game.getNenTang());
            ps.setDouble(4, game.getGiaBan());
            ps.setDouble(5, game.getGiaThueNgay());
            ps.setInt(6, game.getMaGame());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int maGame) {
        String sql = "DELETE FROM GAME WHERE MaGame = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, maGame);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi: Không thể xóa Game này vì đang có Sản Phẩm (CD/ROM) tham chiếu tới nó!");
            e.printStackTrace();
        }
        return false;
    }

    public Game findById(int maGame) {
        String sql = "SELECT * FROM GAME WHERE MaGame = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, maGame);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGame(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Game> findAll() {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM GAME ORDER BY MaGame DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToGame(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Game> search(String keyword) {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM GAME WHERE LOWER(TenGame) LIKE ? OR LOWER(TheLoai) LIKE ? OR LOWER(NenTang) LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGame(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Game> filterByTheLoai(String theLoai) {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM GAME WHERE LOWER(TheLoai) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, theLoai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGame(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Game mapResultSetToGame(ResultSet rs) throws SQLException {
        Game game = new Game();
        game.setMaGame(rs.getInt("MaGame"));
        game.setTenGame(rs.getString("TenGame"));
        game.setTheLoai(rs.getString("TheLoai"));
        game.setNenTang(rs.getString("NenTang"));
        game.setGiaBan(rs.getDouble("GiaBan"));
        game.setGiaThueNgay(rs.getDouble("GiaThueNgay"));
        return game;
    }
}
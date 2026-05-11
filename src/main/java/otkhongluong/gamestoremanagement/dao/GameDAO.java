package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {

    // ================= INSERT =================
    public boolean insert(Game game) {
    String sqlGame = "INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) VALUES (?, ?, ?, ?, ?)";
    String sqlDetail = "INSERT INTO GAME_CHITIET (MaGame, MoTa, Rating, Genre, DeliveryMethod, ReleaseDate, Region, Features, Language, Currency) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DBConnection.getConnection()) {
        conn.setAutoCommit(false); // Bắt đầu Transaction
        try (PreparedStatement ps1 = conn.prepareStatement(sqlGame, Statement.RETURN_GENERATED_KEYS)) {
            ps1.setString(1, game.getTenGame());
            ps1.setString(2, game.getTheLoai());
            ps1.setString(3, game.getNenTang());
            ps1.setString(4, game.getGhiChu());
            ps1.setString(5, game.getHinhAnh());
            ps1.executeUpdate();

            ResultSet rs = ps1.getGeneratedKeys();
            int newId = rs.next() ? rs.getInt(1) : 0;

            try (PreparedStatement ps2 = conn.prepareStatement(sqlDetail)) {
                ps2.setInt(1, newId);
                ps2.setString(2, game.getMoTa());
                ps2.setString(3, game.getRating());
                ps2.setString(4, game.getGenre());
                ps2.setString(5, game.getDeliveryMethod());
                ps2.setDate(6, game.getReleaseDate() != null ? java.sql.Date.valueOf(game.getReleaseDate()) : null);
                ps2.setString(7, game.getRegion());
                ps2.setString(8, game.getFeatures());
                ps2.setString(9, game.getLanguage());
                ps2.setString(10, game.getCurrency());
                ps2.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        }
    } catch (Exception e) { e.printStackTrace(); }
    return false;
}
    // ================= UPDATE =================
    public boolean update(Game game) {
        String sql = "UPDATE GAME SET TenGame=?, TheLoai=?, NenTang=?, GhiChu=?, HinhAnh=? WHERE MaGame=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, game.getTenGame());
            ps.setString(2, game.getTheLoai());
            ps.setString(3, game.getNenTang());
            ps.setString(4, game.getGhiChu());
            ps.setString(5, game.getHinhAnh());
            ps.setInt(6, game.getMaGame());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= DELETE =================
    public boolean delete(int id) {
        String sql = "DELETE FROM GAME WHERE MaGame = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // In lỗi ra Console để kiểm tra nếu bị vướng Khóa ngoại (Foreign Key)
            e.printStackTrace();
        }
        return false;
    }
    
    // ================= getAllGames =================
    public List<Game> getAllGames() {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM GAME ORDER BY TenGame"; // Lấy tất cả cột (*)

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Dùng hàm map(rs) đã viết ở dưới để lấy đầy đủ các cột giá
                list.add(map(rs)); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= FIND BY ID =================
    public Game findById(int maGame) {

        String sql = "SELECT * FROM GAME WHERE MaGame = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maGame);

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
    public List<Game> findAll() {
        List<Game> list = new ArrayList<>();
        // Tốt nhất là liệt kê tên cột rõ ràng
        String sql = "SELECT MaGame, TenGame, TheLoai, NenTang, GhiChu, HinhAnh FROM GAME ORDER BY MaGame DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= SEARCH =================
    public List<Game> search(String keyword) {

        List<Game> list = new ArrayList<>();

        String sql = "SELECT * FROM GAME " +
                     "WHERE LOWER(TenGame) LIKE ? " +
                     "OR LOWER(TheLoai) LIKE ? " +
                     "OR LOWER(NenTang) LIKE ?";

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
    public List<Game> findAllWithPrice() {

        List<Game> list = new ArrayList<>();

        String sql =
            "SELECT g.*, s.GiaBan, s.GiaThueNgay " +
            "FROM GAME g " +
            "LEFT JOIN SANPHAM s ON g.MaGame = s.MaGame";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Game g = map(rs);

                // nếu bạn muốn mở rộng model:
                // g.setGiaBan(rs.getDouble("GiaBan"));
                // g.setGiaThueNgay(rs.getDouble("GiaThueNgay"));

                list.add(g);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= MAP RESULT =================
    private Game map(ResultSet rs) throws SQLException {
    Game g = new Game();
    g.setMaGame(rs.getInt("MaGame"));
    g.setTenGame(rs.getString("TenGame"));
    g.setTheLoai(rs.getString("TheLoai"));
    g.setNenTang(rs.getString("NenTang"));
    g.setGhiChu(rs.getString("GhiChu"));
    g.setHinhAnh(rs.getString("HinhAnh"));
    return g;
}
}
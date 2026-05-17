package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {

    // ================= INSERT =================
    public boolean insert(Game game) {

        String sql = "INSERT INTO GAME (TenGame, TheLoai, NenTang, GhiChu, HinhAnh) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, game.getTenGame());
            ps.setString(2, game.getTheLoai());
            ps.setString(3, game.getNenTang());
            ps.setString(4, game.getGhiChu());
            ps.setString(5, game.getHinhAnh());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= UPDATE =================
    public boolean update(Game game) {

        String sql = "UPDATE GAME SET TenGame = ?, TheLoai = ?, NenTang = ?, " +
                     "GhiChu = ?, HinhAnh = ? WHERE MaGame = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, game.getTenGame());
            ps.setString(2, game.getTheLoai());
            ps.setString(3, game.getNenTang());
            ps.setString(4, game.getGhiChu());
            ps.setString(5, game.getHinhAnh());
            ps.setInt(6, game.getMaGame());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= DELETE =================
    public boolean delete(int maGame) {

        String sql = "DELETE FROM GAME WHERE MaGame = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maGame);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Không thể xóa Game (đang có SANPHAM/CD/ROM liên kết)");
            e.printStackTrace();
        }

        return false;
    }
    
    // ================= getAllGames =================
    public List<Game> getAllGames() {
        List<Game> list = new ArrayList<>();
        String sql =
            "SELECT " +
            "  g.MaGame, g.TenGame, g.TheLoai, g.NenTang, g.GhiChu, g.HinhAnh, " +

            // ← THÊM CÁC CỘT TỪ GAME_CHITIET
            "  gc.MoTa, gc.Rating, gc.Genre, gc.DeliveryMethod, gc.ReleaseDate, " +
            "  gc.Region, gc.Features, gc.Language, gc.Currency, " +

            "  CASE WHEN COUNT(CASE WHEN cd.TrangThai = N'SanSang' THEN 1 END) > 0 " +
            "       THEN MAX(CASE WHEN cd.MaSP IS NOT NULL THEN spCD.GiaBan ELSE NULL END) " +
            "       ELSE NULL END AS GiaCD, " +
            "  MAX(CASE WHEN r.MaSP IS NOT NULL THEN spROM.GiaBan ELSE NULL END) AS GiaROM, " +
            "  CASE WHEN COUNT(CASE WHEN cd.TrangThai = N'SanSang' THEN 1 END) > 0 " +
            "       THEN MAX(CASE WHEN cd.MaSP IS NOT NULL THEN spCD.GiaThueNgay ELSE NULL END) " +
            "       ELSE NULL END AS GiaThueNgay " +

            "FROM GAME g " +
            "LEFT JOIN GAME_CHITIET gc ON gc.MaGame = g.MaGame " + // ← THÊM DÒNG NÀY
            "LEFT JOIN SANPHAM spCD ON spCD.MaGame = g.MaGame " +
            "LEFT JOIN CD cd        ON cd.MaSP     = spCD.MaSP " +
            "LEFT JOIN SANPHAM spROM ON spROM.MaGame = g.MaGame " +
            "LEFT JOIN ROM r         ON r.MaSP        = spROM.MaSP " +

            "GROUP BY g.MaGame, g.TenGame, g.TheLoai, g.NenTang, g.GhiChu, g.HinhAnh, " +
            "  gc.MoTa, gc.Rating, gc.Genre, gc.DeliveryMethod, gc.ReleaseDate, " + // ← THÊM VÀO GROUP BY
            "  gc.Region, gc.Features, gc.Language, gc.Currency " +
            "ORDER BY g.TenGame";
        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {

                Game g = new Game();
                g.setMaGame(rs.getInt("MaGame"));
                g.setTenGame(rs.getString("TenGame"));
                g.setTheLoai(rs.getString("TheLoai"));
                g.setNenTang(rs.getString("NenTang"));
                g.setGhiChu(rs.getString("GhiChu"));
                g.setHinhAnh(rs.getString("HinhAnh"));
                g.setMoTa(rs.getString("MoTa"));
                g.setRating(rs.getString("Rating"));
                g.setGenre(rs.getString("Genre"));
                g.setDeliveryMethod(rs.getString("DeliveryMethod"));
                g.setRegion(rs.getString("Region"));
                g.setFeatures(rs.getString("Features"));
                g.setLanguage(rs.getString("Language"));
                g.setCurrency(rs.getString("Currency"));
                double giaCD = rs.getDouble("GiaCD");
                if (!rs.wasNull()) g.setGiaCD(giaCD);

                double giaROM = rs.getDouble("GiaROM");
                if (!rs.wasNull()) g.setGiaROM(giaROM);

                double giaThueNgay = rs.getDouble("GiaThueNgay");
                if (!rs.wasNull()) g.setGiaThueNgay(giaThueNgay);
                
                Date rd = rs.getDate("ReleaseDate");
                if (rd != null) g.setReleaseDate(rd.toLocalDate());

                list.add(g);
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
package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.GameDAO;
import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GameService {

    private final GameDAO gameDAO;

    public GameService() {
        this.gameDAO = new GameDAO();
    }

    // =================================================
    // GET ALL GAME + PRICE CD + ROM
    // =================================================
    public List<Game> getAllGames() {

        List<Game> list = new ArrayList<>();

        String sql =
            "SELECT " +
            " g.MaGame, g.TenGame, g.TheLoai, g.NenTang, g.GhiChu, g.HinhAnh, " +

            " MAX(CASE WHEN cd.MaCD IS NOT NULL THEN sp.GiaThueNgay END) AS GiaCD, " +
            " MAX(CASE WHEN r.MaSP IS NOT NULL THEN sp.GiaBan END) AS GiaROM " +

            "FROM GAME g " +
            "LEFT JOIN SANPHAM sp ON g.MaGame = sp.MaGame " +
            "LEFT JOIN CD cd ON sp.MaSP = cd.MaSP " +
            "LEFT JOIN ROM r ON sp.MaSP = r.MaSP " +

            "GROUP BY g.MaGame, g.TenGame, g.TheLoai, g.NenTang, g.GhiChu, g.HinhAnh " +
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

                // ===== PRICE CD =====
                double giaCD = rs.getDouble("GiaCD");
                if (!rs.wasNull()) {
                    g.setGiaCD(giaCD);
                }

                // ===== PRICE ROM =====
                double giaROM = rs.getDouble("GiaROM");
                if (!rs.wasNull()) {
                    g.setGiaROM(giaROM);
                }

                list.add(g);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // =================================================
    // GET BY ID
    // =================================================
    public Game getGameById(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("ID game không hợp lệ");
        }

        return gameDAO.findById(id);
    }

    // =================================================
    // ADD GAME
    // =================================================
    public boolean addGame(Game game) {

        validateGame(game);
        return gameDAO.insert(game);
    }

    // =================================================
    // UPDATE GAME
    // =================================================
    public boolean updateGame(Game game) {

        if (game == null || game.getMaGame() <= 0) {
            throw new IllegalArgumentException("Game không hợp lệ để update");
        }

        validateGame(game);
        return gameDAO.update(game);
    }

    // =================================================
    // DELETE GAME
    // =================================================
    public boolean deleteGame(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("ID không hợp lệ");
        }

        return gameDAO.delete(id);
    }

    // =================================================
    // SEARCH
    // =================================================
    public List<Game> searchGames(String keyword) {

        if (keyword == null) {
            keyword = "";
        }

        return gameDAO.search(keyword.trim());
    }

    // =================================================
    // VALIDATION
    // =================================================
    private void validateGame(Game game) {

        if (game == null) {
            throw new IllegalArgumentException("Game không được null");
        }

        ValidationService.validateNotEmpty(game.getTenGame(), "Tên game");
        ValidationService.validateNotEmpty(game.getTheLoai(), "Thể loại");
        ValidationService.validateNotEmpty(game.getNenTang(), "Nền tảng");

        if (game.getHinhAnh() == null) {
            game.setHinhAnh("");
        }

        if (game.getGhiChu() == null) {
            game.setGhiChu("");
        }
    }
}
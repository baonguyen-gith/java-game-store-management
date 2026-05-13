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
    // GET ALL GAME + PRICE CD + ROM + THUÊ
    // =================================================
    public List<Game> getAllGames() {

        List<Game> list = new ArrayList<>();

        /*
         * Tách rõ 3 cột giá:
         *   GiaCD        — GiaBan của SanPham có bản ghi trong bảng CD
         *   GiaROM       — GiaBan của SanPham có bản ghi trong bảng ROM
         *   GiaThueNgay  — GiaThueNgay của SanPham thuộc bảng CD
         *
         * Dùng MAX(...) để gộp nhiều sản phẩm cùng loại về 1 dòng/game.
         * NULL được giữ nguyên → wasNull() phía Java xử lý đúng.
         */
        String sql =
            "SELECT " +
            "  g.MaGame, g.TenGame, g.TheLoai, g.NenTang, g.GhiChu, g.HinhAnh, " +

            // GiaBan CD — chỉ tính khi còn ít nhất 1 CD SanSang
            "  CASE WHEN COUNT(CASE WHEN cd.TrangThai = N'SanSang' THEN 1 END) > 0 " +
            "       THEN MAX(CASE WHEN cd.MaSP IS NOT NULL THEN spCD.GiaBan ELSE NULL END) " +
            "       ELSE NULL END AS GiaCD, " +

            // GiaBan ROM — ROM không có TrangThai, luôn lấy nếu có
            "  MAX(CASE WHEN r.MaSP IS NOT NULL THEN spROM.GiaBan ELSE NULL END) AS GiaROM, " +

            // GiaThueNgay — chỉ tính khi còn ít nhất 1 CD SanSang
            "  CASE WHEN COUNT(CASE WHEN cd.TrangThai = N'SanSang' THEN 1 END) > 0 " +
            "       THEN MAX(CASE WHEN cd.MaSP IS NOT NULL THEN spCD.GiaThueNgay ELSE NULL END) " +
            "       ELSE NULL END AS GiaThueNgay " +

            "FROM GAME g " +

            // JOIN riêng cho CD
            "LEFT JOIN SANPHAM spCD ON spCD.MaGame = g.MaGame " +
            "LEFT JOIN CD cd        ON cd.MaSP     = spCD.MaSP " +

            // JOIN riêng cho ROM
            "LEFT JOIN SANPHAM spROM ON spROM.MaGame = g.MaGame " +
            "LEFT JOIN ROM r         ON r.MaSP        = spROM.MaSP " +

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

                double giaCD = rs.getDouble("GiaCD");
                if (!rs.wasNull()) g.setGiaCD(giaCD);

                double giaROM = rs.getDouble("GiaROM");
                if (!rs.wasNull()) g.setGiaROM(giaROM);

                double giaThueNgay = rs.getDouble("GiaThueNgay");
                if (!rs.wasNull()) g.setGiaThueNgay(giaThueNgay);

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
        if (id <= 0) throw new IllegalArgumentException("ID game không hợp lệ");
        return gameDAO.findById(id);
    }

    // =================================================
    // ADD / UPDATE / DELETE / SEARCH
    // =================================================
    public boolean addGame(Game game) {
        validateGame(game);
        return gameDAO.insert(game);
    }

    public boolean updateGame(Game game) {
        if (game == null || game.getMaGame() <= 0)
            throw new IllegalArgumentException("Game không hợp lệ để update");
        validateGame(game);
        return gameDAO.update(game);
    }

    public boolean deleteGame(int id) {
        if (id <= 0) throw new IllegalArgumentException("ID không hợp lệ");
        return gameDAO.delete(id);
    }

    public List<Game> searchGames(String keyword) {
        if (keyword == null) keyword = "";
        return gameDAO.search(keyword.trim());
    }

    // =================================================
    // VALIDATION
    // =================================================
    private void validateGame(Game game) {
        if (game == null) throw new IllegalArgumentException("Game không được null");
        ValidationService.validateNotEmpty(game.getTenGame(), "Tên game");
        ValidationService.validateNotEmpty(game.getTheLoai(), "Thể loại");
        ValidationService.validateNotEmpty(game.getNenTang(), "Nền tảng");
        if (game.getHinhAnh() == null) game.setHinhAnh("");
        if (game.getGhiChu()  == null) game.setGhiChu("");
    }
}
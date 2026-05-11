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
        return gameDAO.findAll();
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
package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.GameDAO;
import otkhongluong.gamestoremanagement.model.Game;
import java.util.List;

public class GameService {
    private final GameDAO gameDAO;

    public GameService() {
        this.gameDAO = new GameDAO();
    }

    public List<Game> getAllGames() {
        return gameDAO.findAll();
    }

    public Game getGameById(int id) {
        return gameDAO.findById(id);
    }

    public boolean addGame(Game game) {
        ValidationService.validateNotEmpty(game.getTenGame(), "Tên game");
        if (game.getGiaBan() < 0) throw new IllegalArgumentException("Giá bán không được âm.");
        return gameDAO.insert(game);
    }

    public boolean updateGame(Game game) {
        return gameDAO.update(game);
    }

    public boolean deleteGame(int id) {
        return gameDAO.delete(id);
    }

    public List<Game> searchGames(String keyword) {
        return gameDAO.search(keyword);
    }
}

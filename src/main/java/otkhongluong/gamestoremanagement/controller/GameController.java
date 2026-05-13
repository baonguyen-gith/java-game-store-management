package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.service.GameService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GameController — lớp trung gian giữa View và Service (MVC).
 *
 * View chỉ được phép gọi GameController, KHÔNG gọi GameService trực tiếp.
 * Controller xử lý: điều phối logic, lọc, sắp xếp, phân trang.
 */
public class GameController {

    private final GameService gameService;

    // ── Cache danh sách đầy đủ để filter/sort không cần query lại DB ──
    private List<Game> cachedGames;

    public GameController() {
        this.gameService = new GameService();
    }

    // =================================================================
    // CRUD — Uỷ thác xuống Service
    // =================================================================

    /** Tải toàn bộ game từ DB, cập nhật cache nội bộ. */
    public List<Game> loadAllGames() {
        cachedGames = gameService.getAllGames();
        return cachedGames;
    }

    public Game getGameById(int id) {
        return gameService.getGameById(id);
    }

    public boolean addGame(Game game) {
        boolean ok = gameService.addGame(game);
        if (ok) loadAllGames(); // refresh cache
        return ok;
    }

    public boolean updateGame(Game game) {
        boolean ok = gameService.updateGame(game);
        if (ok) loadAllGames();
        return ok;
    }

    public boolean deleteGame(int id) {
        boolean ok = gameService.deleteGame(id);
        if (ok) loadAllGames();
        return ok;
    }

    // =================================================================
    // FILTER — View truyền keyword, Controller trả về danh sách đã lọc
    // (dùng cache, không query DB lại)
    // =================================================================

    /**
     * Lọc game theo từ khóa (tên hoặc mã).
     * Dùng cho GamePanel (store-front).
     */
    public List<Game> filterByKeyword(String keyword) {
        ensureCache();
        if (keyword == null || keyword.isBlank()) return cachedGames;
        String kw = keyword.trim().toLowerCase();
        return cachedGames.stream()
            .filter(g ->
                nvl(g.getTenGame()).toLowerCase().contains(kw) ||
                String.valueOf(g.getMaGame()).contains(kw))
            .collect(Collectors.toList());
    }

    /**
     * Lọc game theo từ khóa (tên hoặc thể loại).
     * Dùng cho GameManagePanel (admin table).
     */
    public List<Game> filterForManage(String keyword) {
        ensureCache();
        if (keyword == null || keyword.isBlank()) return cachedGames;
        String kw = keyword.trim().toLowerCase();
        return cachedGames.stream()
            .filter(g ->
                nvl(g.getTenGame()).toLowerCase().contains(kw) ||
                nvl(g.getTheLoai()).toLowerCase().contains(kw))
            .collect(Collectors.toList());
    }

    // =================================================================
    // SORT
    // =================================================================

    /** Sắp xếp cache theo tên hoặc mã game. */
    public void sortCache(String type, boolean ascending) {
        ensureCache();
        cachedGames.sort((g1, g2) -> {
            int res = type.equals("MaGame")
                ? Integer.compare(g1.getMaGame(), g2.getMaGame())
                : nvl(g1.getTenGame()).compareToIgnoreCase(nvl(g2.getTenGame()));
            return ascending ? res : -res;
        });
    }

    // =================================================================
    // PAGINATION
    // =================================================================

    /**
     * Cắt trang từ một danh sách đã lọc.
     *
     * @param source   danh sách nguồn (đã lọc)
     * @param page     trang hiện tại (1-based)
     * @param pageSize số dòng mỗi trang
     */
    public List<Game> getPage(List<Game> source, int page, int pageSize) {
        if (source == null || source.isEmpty()) return List.of();
        int from = Math.max(0, (page - 1) * pageSize);
        if (from >= source.size()) from = 0;
        int to = Math.min(from + pageSize, source.size());
        return new ArrayList<>(source.subList(from, to));
    }

    /** Tổng số trang. */
    public int getTotalPages(List<Game> source, int pageSize) {
        if (source == null || source.isEmpty()) return 1;
        return (int) Math.ceil((double) source.size() / pageSize);
    }

    // =================================================================
    // HELPERS
    // =================================================================

    private void ensureCache() {
        if (cachedGames == null) loadAllGames();
    }

    private String nvl(String s) { return s == null ? "" : s; }
}
package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.service.GameService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GameController — lớp trung gian giữa View và Service (MVC).
 * ✅ FIX: xóa duplicate import java.util.List (xuất hiện 2 lần → compile error).
 * View chỉ được phép gọi GameController, KHÔNG gọi GameService trực tiếp.
 */
public class GameController {

    private final GameService gameService;

    // Cache danh sách đầy đủ để filter/sort không cần query lại DB
    private List<Game> cachedGames;

    public GameController() {
        this.gameService = new GameService();
    }

    // =================================================================
    // CRUD
    // =================================================================

    public List<Game> loadAllGames() {
        cachedGames = gameService.getAllGames();
        return cachedGames;
    }

    public Game getGameById(int id) {
        return gameService.getGameById(id);
    }

    public boolean addGame(Game game) {
        boolean ok = gameService.addGame(game);
        if (ok) loadAllGames();
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

    public SaveResult handleSave(Game existing, Map<String, String> form) {
        String ten = form.getOrDefault("tenGame", "").trim();
        if (ten.isEmpty()) return SaveResult.fail("Tên game không được trống!");

        Game target = existing != null ? existing : new Game();
        target.setTenGame(ten);
        target.setTheLoai(form.getOrDefault("theLoai", "").trim());
        target.setNenTang(form.getOrDefault("nenTang", "").trim());
        target.setGhiChu(form.getOrDefault("ghiChu", "").trim());
        target.setHinhAnh(form.getOrDefault("hinhAnh", "").trim());
        target.setRating(form.getOrDefault("rating", "").trim());
        target.setGenre(form.getOrDefault("genre", "").trim());
        target.setRegion(form.getOrDefault("region", "").trim());
        target.setMoTa(form.getOrDefault("moTa", "").trim());

        try {
            String rel = form.getOrDefault("releaseDate", "");
            if (!rel.isEmpty()) target.setReleaseDate(LocalDate.parse(rel));
        } catch (Exception e) { /* bỏ qua nếu sai định dạng */ }

        boolean ok = existing != null ? updateGame(target) : addGame(target);
        return ok ? SaveResult.ok("Thành công!") : SaveResult.fail("Lỗi lưu dữ liệu!");
    }

    // =================================================================
    // FILTER
    // =================================================================

    /** Lọc theo tên hoặc mã — dùng cho GamePanel (store-front). */
    public List<Game> filterByKeyword(String keyword) {
        ensureCache();
        if (keyword == null || keyword.isBlank()) return cachedGames;
        String kw = keyword.trim().toLowerCase();
        return cachedGames.stream()
            .filter(g -> nvl(g.getTenGame()).toLowerCase().contains(kw)
                      || String.valueOf(g.getMaGame()).contains(kw))
            .collect(Collectors.toList());
    }

    /** Lọc theo tên hoặc thể loại — dùng cho GameManagePanel (admin table). */
    public List<Game> filterForManage(String keyword) {
        ensureCache();
        if (keyword == null || keyword.isBlank()) return cachedGames;
        String kw = keyword.trim().toLowerCase();
        return cachedGames.stream()
            .filter(g -> nvl(g.getTenGame()).toLowerCase().contains(kw)
                      || nvl(g.getTheLoai()).toLowerCase().contains(kw))
            .collect(Collectors.toList());
    }

    // =================================================================
    // SORT
    // =================================================================

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

    public PageResult<Game> getPage(String keyword, int page, int pageSize) {
        List<Game> filtered = filterForManage(keyword);
        int total = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
        if (page > total) page = total;
        if (page < 1)     page = 1;
        int from = (page - 1) * pageSize;
        int to   = Math.min(from + pageSize, filtered.size());
        return new PageResult<>(filtered.subList(from, to), page, total);
    }

    public int getTotalPages(List<Game> source, int pageSize) {
        if (source == null || source.isEmpty()) return 1;
        return (int) Math.ceil((double) source.size() / pageSize);
    }

    // =================================================================
    // INNER CLASSES
    // =================================================================

    public static class SaveResult {
        public final boolean success;
        public final String  message;

        private SaveResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static SaveResult ok(String msg)   { return new SaveResult(true,  msg); }
        public static SaveResult fail(String msg) { return new SaveResult(false, msg); }
    }

    public static class PageResult<T> {
        public final List<T> data;
        public final int     currentPage;
        public final int     totalPages;

        public PageResult(List<T> data, int currentPage, int totalPages) {
            this.data        = data;
            this.currentPage = currentPage;
            this.totalPages  = totalPages;
        }
    }

    // =================================================================
    // HELPERS
    // =================================================================

    private void ensureCache() {
        if (cachedGames == null) loadAllGames();
    }

    private String nvl(String s) { return s == null ? "" : s; }
}
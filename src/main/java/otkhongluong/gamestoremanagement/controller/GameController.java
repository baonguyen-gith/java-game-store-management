package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.service.GameService;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    public SaveResult handleSave(Game existing, Map<String,String> form) {
        String ten = form.getOrDefault("tenGame","").trim();
        if (ten.isEmpty()) return SaveResult.fail("Tên game không được trống!");
        Game target = existing != null ? existing : new Game();
        target.setTenGame(ten);
        target.setTheLoai(form.getOrDefault("theLoai","").trim());
        target.setNenTang(form.getOrDefault("nenTang","").trim());
        target.setGhiChu(form.getOrDefault("ghiChu","").trim());
        target.setHinhAnh(form.getOrDefault("hinhAnh","").trim());
        target.setRating(form.getOrDefault("rating","").trim());
        target.setGenre(form.getOrDefault("genre","").trim());
        target.setRegion(form.getOrDefault("region","").trim());
        target.setMoTa(form.getOrDefault("moTa","").trim());
        try {
            String rel = form.getOrDefault("releaseDate","");
            if (!rel.isEmpty()) target.setReleaseDate(LocalDate.parse(rel));
        } catch (Exception e) { /* bỏ qua nếu sai định dạng */ }
        boolean ok = existing != null ? updateGame(target) : addGame(target);
        return ok ? SaveResult.ok("Thành công!") : SaveResult.fail("Lỗi lưu dữ liệu!");
    }

    // =================================================================
    // FILTER — View truyền keyword, Controller trả về danh sách đã lọc
    // (dùng cache, không query DB lại)
    // =================================================================

    /**
     * Lọc game theo từ khóa (tên hoặc mã).
     * Dùng cho GamePanel (store-front).
     */
    public static class SaveResult {
        public boolean success;
        public String message;

        public SaveResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static SaveResult ok(String msg) {
            return new SaveResult(true, msg);
        }

        public static SaveResult fail(String msg) {
            return new SaveResult(false, msg);
        }
    }
    
    public static class PageResult<T> {
        public List<T> data;
        public int currentPage;
        public int totalPages;

        public PageResult(List<T> data, int currentPage, int totalPages) {
            this.data = data;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }
    }
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
    public PageResult<Game> getPage(String keyword, int page, int pageSize) {
        List<Game> filtered = filterForManage(keyword);

        int total = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));

        if (page > total) page = total;
        if (page < 1) page = 1;

        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, filtered.size());

        List<Game> pageData = filtered.subList(from, to);

        return new PageResult<>(pageData, page, total);
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
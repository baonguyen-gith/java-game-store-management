package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.service.TransactionService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

/**
 * Controller cho màn hình Lịch sử giao dịch.
 *
 * Trách nhiệm:
 *  - Giữ toàn bộ state của màn hình (dữ liệu gốc, trang hiện tại, cột sort, ...)
 *  - Nhận input từ View, gọi Service xử lý, trả kết quả về View
 *  - View không được gọi DAO hay chứa logic filter/sort nữa
 *
 * Luồng dữ liệu:
 *   View → Controller.onXxx() → Service → Controller trả PageResult → View render
 */
public class TransactionController {

    // ══════════════════════════════════════════════════════════
    // Constants
    // ══════════════════════════════════════════════════════════
    public static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ══════════════════════════════════════════════════════════
    // Dependencies
    // ══════════════════════════════════════════════════════════
    private final TransactionService service;

    // ══════════════════════════════════════════════════════════
    // State
    // ══════════════════════════════════════════════════════════

    /** Toàn bộ dữ liệu thô tải từ DB, không bao giờ bị filter/sort trực tiếp. */
    private List<Object[]> allData = Collections.emptyList();

    /** Trang hiện tại (1-based). */
    private int currentPage = 1;

    /**
     * Cột đang sort (-1 = chưa sort).
     * Ánh xạ: 0=Mã GD, 1=Loại, 2=Mã NV, 3=Khách hàng, 4=Ngày, 5=Tiền.
     */
    private int sortCol = -1;

    /**
     * Trạng thái sort của từng cột:
     *   0 = không sort, 1 = tăng dần (▲), 2 = giảm dần (▼)
     */
    private final int[] sortState;

    // ══════════════════════════════════════════════════════════
    // Constructor
    // ══════════════════════════════════════════════════════════

    public TransactionController(TransactionService service, int totalColumns) {
        this.service   = service;
        this.sortState = new int[totalColumns];
    }

    /** Constructor tiện lợi — tự tạo Service mặc định. */
    public TransactionController() {
        this(new TransactionService(), 7 /* số cột trong bảng */);
    }

    // ══════════════════════════════════════════════════════════
    // Public API — gọi từ View
    // ══════════════════════════════════════════════════════════

    /**
     * Tải (hoặc tải lại) toàn bộ dữ liệu từ DB và reset mọi state.
     *
     * @return {@link PageResult} trang đầu tiên
     */
    public PageResult loadAll() {
        allData     = service.getAll();
        currentPage = 1;
        sortCol     = -1;
        java.util.Arrays.fill(sortState, 0);
        return buildPage("", "Tất cả", null, null);
    }

    /**
     * Gọi khi người dùng thay đổi bất kỳ bộ lọc nào
     * (ô tìm kiếm, ngày, loại giao dịch).
     * Reset về trang 1.
     *
     * @param keyword từ khóa tìm kiếm (rỗng = không lọc)
     * @param loai    "Tất cả" | "Hóa đơn" | "Phiếu thuê"
     * @param fromStr ngày bắt đầu dạng "dd/MM/yyyy", rỗng/null = không giới hạn
     * @param toStr   ngày kết thúc dạng "dd/MM/yyyy", rỗng/null = không giới hạn
     * @return {@link PageResult} trang 1 sau khi lọc, hoặc {@link PageResult#dateError()} nếu ngày sai định dạng
     */
    public PageResult onFilterChanged(String keyword, String loai,
                                      String fromStr, String toStr) {
        DateParseResult dates = parseDates(fromStr, toStr);
        if (dates.hasError()) return PageResult.dateError(dates.fromError, dates.toError);

        currentPage = 1;
        return buildPage(keyword, loai, dates.from, dates.to);
    }

    /**
     * Gọi khi người dùng click vào header để sort.
     *
     * @param col chỉ số cột được click (phải < tổng số cột - 1 để bỏ cột "Chi tiết")
     * @return {@link PageResult} cùng trang nhưng đã được sort lại
     */
    public PageResult onSortChanged(int col, String keyword, String loai,
                                    String fromStr, String toStr) {
        // Xoay vòng: 0 → 1 (▲) → 2 (▼) → 0 (bỏ sort)
        sortState[col] = (sortState[col] + 1) % 3;
        sortCol = (sortState[col] == 0) ? -1 : col;

        // Reset các cột khác
        for (int i = 0; i < sortState.length; i++)
            if (i != col) sortState[i] = 0;

        currentPage = 1;
        DateParseResult dates = parseDates(fromStr, toStr);
        if (dates.hasError()) return PageResult.dateError(dates.fromError, dates.toError);
        return buildPage(keyword, loai, dates.from, dates.to);
    }

    /**
     * Gọi khi người dùng bấm nút trang trước / trang sau.
     *
     * @param delta -1 (trang trước) hoặc +1 (trang sau)
     * @return {@link PageResult} trang mới, hoặc trang hiện tại nếu đã ở đầu/cuối
     */
    public PageResult onPageChanged(int delta, String keyword, String loai,
                                    String fromStr, String toStr) {
        DateParseResult dates = parseDates(fromStr, toStr);
        if (dates.hasError()) return PageResult.dateError(dates.fromError, dates.toError);

        List<Object[]> filtered = applyFilterAndSort(keyword, loai, dates.from, dates.to);
        int totalPages = totalPages(filtered.size());
        int newPage    = currentPage + delta;

        if (newPage < 1)          newPage = 1;
        if (newPage > totalPages) newPage = totalPages;
        currentPage = newPage;

        return slice(filtered);
    }

    // ══════════════════════════════════════════════════════════
    // Getters — View dùng để đọc state hiện tại
    // ══════════════════════════════════════════════════════════

    public int getCurrentPage()    { return currentPage; }
    public int getSortCol()        { return sortCol; }
    public int[] getSortState()    { return sortState.clone(); }

    // ══════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════

    /** Lọc + sort + phân trang và đóng gói thành PageResult. */
    private PageResult buildPage(String keyword, String loai,
                                  LocalDate from, LocalDate to) {
        List<Object[]> filtered = applyFilterAndSort(keyword, loai, from, to);
        return slice(filtered);
    }

    /** Lọc + sort (không phân trang). */
    private List<Object[]> applyFilterAndSort(String keyword, String loai,
                                               LocalDate from, LocalDate to) {
        List<Object[]> filtered = service.filter(allData, from, to, keyword, loai);

        if (sortCol >= 0) {
            boolean ascending = (sortState[sortCol] == 1);
            service.sort(filtered, sortCol, ascending);
        }
        return filtered;
    }

    /** Cắt trang từ danh sách đã lọc/sort. */
    private PageResult slice(List<Object[]> filtered) {
        int totalPages = totalPages(filtered.size());
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIdx = (currentPage - 1) * PAGE_SIZE;
        int toIdx   = Math.min(fromIdx + PAGE_SIZE, filtered.size());

        List<Object[]> page = (fromIdx <= toIdx)
            ? filtered.subList(fromIdx, toIdx)
            : Collections.emptyList();

        return new PageResult(
            page,
            currentPage,
            totalPages,
            filtered.size(),
            sortCol,
            sortState.clone(),
            false, false
        );
    }

    private int totalPages(int total) {
        return Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
    }

    // ── Date parsing ──────────────────────────────────────────

    private DateParseResult parseDates(String fromStr, String toStr) {
        LocalDate from = null;
        LocalDate to   = null;
        boolean fromError = false;
        boolean toError   = false;

        if (fromStr != null && !fromStr.trim().isEmpty()) {
            try { from = LocalDate.parse(fromStr.trim(), FMT); }
            catch (DateTimeParseException e) { fromError = true; }
        }
        if (toStr != null && !toStr.trim().isEmpty()) {
            try { to = LocalDate.parse(toStr.trim(), FMT); }
            catch (DateTimeParseException e) { toError = true; }
        }

        return new DateParseResult(from, to, fromError, toError);
    }

    // ══════════════════════════════════════════════════════════
    // Value Objects (trả về cho View)
    // ══════════════════════════════════════════════════════════

    /**
     * Kết quả phân trang trả về cho View.
     * View chỉ cần đọc object này để render — không cần biết gì thêm.
     */
    public static class PageResult {

        /** Các hàng của trang hiện tại (cấu trúc Object[] như TransactionService mô tả). */
        public final List<Object[]> rows;

        /** Trang hiện tại (1-based). */
        public final int currentPage;

        /** Tổng số trang. */
        public final int totalPages;

        /** Tổng số bản ghi sau khi lọc. */
        public final int totalRows;

        /** Cột đang sort (-1 = không sort). */
        public final int sortCol;

        /** Trạng thái sort từng cột (0=none, 1=▲, 2=▼). */
        public final int[] sortState;

        /** true nếu ô "Từ ngày" nhập sai định dạng. */
        public final boolean fromDateError;

        /** true nếu ô "Đến ngày" nhập sai định dạng. */
        public final boolean toDateError;

        PageResult(List<Object[]> rows, int currentPage, int totalPages,
                   int totalRows, int sortCol, int[] sortState,
                   boolean fromDateError, boolean toDateError) {
            this.rows          = rows;
            this.currentPage   = currentPage;
            this.totalPages    = totalPages;
            this.totalRows     = totalRows;
            this.sortCol       = sortCol;
            this.sortState     = sortState;
            this.fromDateError = fromDateError;
            this.toDateError   = toDateError;
        }

        /** Factory: kết quả báo lỗi định dạng ngày (không có rows). */
        static PageResult dateError(boolean fromError, boolean toError) {
            return new PageResult(
                Collections.emptyList(), 1, 1, 0, -1, new int[0],
                fromError, toError
            );
        }

        public boolean hasDateError() { return fromDateError || toDateError; }
    }

    /** Kết quả nội bộ sau khi parse ngày. */
    private static class DateParseResult {
        final LocalDate from, to;
        final boolean fromError, toError;

        DateParseResult(LocalDate from, LocalDate to,
                        boolean fromError, boolean toError) {
            this.from      = from;
            this.to        = to;
            this.fromError = fromError;
            this.toError   = toError;
        }

        boolean hasError() { return fromError || toError; }
    }
}
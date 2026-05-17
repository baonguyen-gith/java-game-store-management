package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.TransactionDTO;
import otkhongluong.gamestoremanagement.service.TransactionService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Controller cho màn hình Lịch sử giao dịch.
 * ✅ FIX: dùng List<TransactionDTO> thay vì List<Object[]> thô xuyên suốt.
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

    // ✅ FIX: List<TransactionDTO> thay vì List<Object[]>
    private List<TransactionDTO> allData = Collections.emptyList();

    private int currentPage = 1;
    private int sortCol     = -1;
    private final int[] sortState;

    // ══════════════════════════════════════════════════════════
    // Constructor
    // ══════════════════════════════════════════════════════════

    public TransactionController(TransactionService service, int totalColumns) {
        this.service   = service;
        this.sortState = new int[totalColumns];
    }

    public TransactionController() {
        this(new TransactionService(), 7);
    }

    // ══════════════════════════════════════════════════════════
    // Public API
    // ══════════════════════════════════════════════════════════

    /**
     * Tải (hoặc tải lại) toàn bộ dữ liệu từ DB và reset mọi state.
     */
    public PageResult loadAll() {
        // ✅ FIX: service.getAll() trả List<TransactionDTO>
        allData     = service.getAll();
        currentPage = 1;
        sortCol     = -1;
        Arrays.fill(sortState, 0);
        return buildPage("", "Tất cả", null, null);
    }

    /**
     * Gọi khi người dùng thay đổi bất kỳ bộ lọc nào.
     */
    public PageResult onFilterChanged(String keyword, String loai,
                                      String fromStr, String toStr) {
        DateParseResult dates = parseDates(fromStr, toStr);
        if (dates.hasError()) return PageResult.dateError(dates.fromError, dates.toError);
        currentPage = 1;
        return buildPage(keyword, loai, dates.from, dates.to);
    }

    /**
     * Gọi khi người dùng click header để sort.
     */
    public PageResult onSortChanged(int col, String keyword, String loai,
                                    String fromStr, String toStr) {
        sortState[col] = (sortState[col] + 1) % 3;
        sortCol        = (sortState[col] == 0) ? -1 : col;
        for (int i = 0; i < sortState.length; i++)
            if (i != col) sortState[i] = 0;
        currentPage = 1;
        DateParseResult dates = parseDates(fromStr, toStr);
        if (dates.hasError()) return PageResult.dateError(dates.fromError, dates.toError);
        return buildPage(keyword, loai, dates.from, dates.to);
    }

    /**
     * Gọi khi người dùng bấm trang trước / trang sau.
     */
    public PageResult onPageChanged(int delta, String keyword, String loai,
                                    String fromStr, String toStr) {
        DateParseResult dates = parseDates(fromStr, toStr);
        if (dates.hasError()) return PageResult.dateError(dates.fromError, dates.toError);

        List<TransactionDTO> filtered = applyFilterAndSort(keyword, loai, dates.from, dates.to);
        int totalPages = totalPages(filtered.size());
        int newPage    = Math.min(Math.max(1, currentPage + delta), totalPages);
        currentPage    = newPage;

        return slice(filtered);
    }

    // ══════════════════════════════════════════════════════════
    // Getters
    // ══════════════════════════════════════════════════════════

    public int   getCurrentPage() { return currentPage; }
    public int   getSortCol()     { return sortCol; }
    public int[] getSortState()   { return sortState.clone(); }

    // ══════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════

    private PageResult buildPage(String keyword, String loai, LocalDate from, LocalDate to) {
        List<TransactionDTO> filtered = applyFilterAndSort(keyword, loai, from, to);
        return slice(filtered);
    }

    // ✅ FIX: filter và sort đều dùng List<TransactionDTO>
    private List<TransactionDTO> applyFilterAndSort(String keyword, String loai,
                                                     LocalDate from, LocalDate to) {
        List<TransactionDTO> filtered = service.filter(allData, from, to, keyword, loai);
        if (sortCol >= 0) {
            boolean ascending = (sortState[sortCol] == 1);
            service.sort(filtered, sortCol, ascending);
        }
        return filtered;
    }

    // ✅ FIX: slice trả PageResult<TransactionDTO>
    private PageResult slice(List<TransactionDTO> filtered) {
        int totalPages = totalPages(filtered.size());
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIdx = (currentPage - 1) * PAGE_SIZE;
        int toIdx   = Math.min(fromIdx + PAGE_SIZE, filtered.size());

        List<TransactionDTO> page = (fromIdx <= toIdx)
            ? filtered.subList(fromIdx, toIdx)
            : Collections.emptyList();

        return new PageResult(
            page, currentPage, totalPages, filtered.size(),
            sortCol, sortState.clone(), false, false
        );
    }

    private int totalPages(int total) {
        return Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
    }

    // ── Date parsing ──────────────────────────────────────────

    private DateParseResult parseDates(String fromStr, String toStr) {
        LocalDate from = null, to = null;
        boolean fromError = false, toError = false;

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
    // Value Objects
    // ══════════════════════════════════════════════════════════

    /**
     * Kết quả phân trang trả về cho View.
     * ✅ FIX: rows là List<TransactionDTO>, không phải List<Object[]>
     */
    public static class PageResult {

        /** Các bản ghi trang hiện tại — View đọc từng field qua getter của DTO. */
        public final List<TransactionDTO> rows;
        public final int currentPage;
        public final int totalPages;
        public final int totalRows;
        public final int sortCol;
        public final int[] sortState;
        public final boolean fromDateError;
        public final boolean toDateError;

        PageResult(List<TransactionDTO> rows, int currentPage, int totalPages,
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

        static PageResult dateError(boolean fromError, boolean toError) {
            return new PageResult(
                Collections.emptyList(), 1, 1, 0, -1, new int[0],
                fromError, toError
            );
        }

        public boolean hasDateError() { return fromDateError || toDateError; }
    }

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
package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Invoice;
import otkhongluong.gamestoremanagement.service.InvoiceService;
import otkhongluong.gamestoremanagement.view.dialog.InvoiceAddDialog;
import otkhongluong.gamestoremanagement.view.dialog.InvoiceEditDialog;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceController {

    private final InvoiceService service;
    private int[] sortState = new int[7]; // số cột
    private int sortCol = -1;
    private static final int PAGE_SIZE = 8;

    public InvoiceController() {
        this.service = new InvoiceService();
    }

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }
    
    public static class InvoicePageResult {
        public final List<Invoice> rows;
        public final int currentPage;
        public final int totalPages;
        public final int totalRows;
        public final boolean fromDateError;
        public final boolean toDateError;
        

        public InvoicePageResult(List<Invoice> rows,
                                 int cur, int total, int totalRows,
                                 boolean fromErr, boolean toErr) {
                this.rows = rows;
                this.currentPage = cur;
                this.totalPages = total;
                this.totalRows = totalRows;
                this.fromDateError = fromErr;
                this.toDateError = toErr;
            }

        public boolean hasDateError() {
            return fromDateError || toDateError;
        }
    }
    public int[] getSortState() {
        return sortState;
    }

    public int getSortCol() {
        return sortCol;
    }
    public InvoicePageResult onSortChanged(
        int col, String from, String to, String kw, int page) {

        sortState[col] = (sortState[col] + 1) % 3;
        sortCol = sortState[col] == 0 ? -1 : col;

        boolean asc = sortState[col] != 2;

        return query(from, to, kw, sortCol, asc, 1);
    }
    
    public InvoicePageResult query(
            String from,
            String to,
            String keyword,
            int page) {

        boolean asc = true;

        if (sortCol >= 0) {
            asc = sortState[sortCol] != 2;
        }

        return query(from, to, keyword, sortCol, asc, page);
    }
    public InvoicePageResult query(String fromStr, String toStr,
                               String keyword, int sortCol,
                               boolean asc, int page) {

        boolean fromErr = false;
        boolean toErr = false;

        LocalDate from = null;
        LocalDate to = null;

        try {
            if (!fromStr.trim().isEmpty()) from = parseDate(fromStr);
        } catch (Exception e) {
            fromErr = true;
        }

        try {
            if (!toStr.trim().isEmpty()) to = parseDate(toStr);
        } catch (Exception e) {
            toErr = true;
        }

        if (fromErr || toErr) {
            return new InvoicePageResult(
                    new ArrayList<>(),
                    page, 1, 0,
                    fromErr, toErr
            );
        }

        List<Invoice> filtered =
                getFilteredInvoices(from, to, keyword, sortCol, asc);

        int total = Math.max(1,
                (int) Math.ceil((double) filtered.size() / PAGE_SIZE));

        if (page > total) page = total;
        if (page < 1) page = 1;

        int f = (page - 1) * PAGE_SIZE;
        int t = Math.min(f + PAGE_SIZE, filtered.size());

        return new InvoicePageResult(
                filtered.subList(f, t),
                page,
                total,
                filtered.size(),
                false,
                false
        );
    }
    // ── Lấy toàn bộ dữ liệu ──────────────────────────────────
    public List<Invoice> getAllInvoices() {
        return service.getAllHoaDon();
    }

    // ── Filter + Sort (logic nghiệp vụ nằm ở đây, không ở Panel) ──
    public List<Invoice> getFilteredInvoices(
            LocalDate from, LocalDate to,
            String keyword,
            int sortCol, boolean ascending) {

        List<Invoice> all = service.getAllHoaDon();

        List<Invoice> result = all.stream()
            .filter(hd -> matchesDateRange(hd, from, to))
            .filter(hd -> matchesKeyword(hd, keyword))
            .collect(Collectors.toList());

        if (sortCol >= 0) {
            Comparator<Invoice> cmp = buildComparator(sortCol);
            if (!ascending) cmp = cmp.reversed();
            result.sort(cmp);
        }

        return result;
    }

    // ── Xóa ──────────────────────────────────────────────────
    public boolean deleteInvoice(int id) {
        return service.deleteHoaDon(id);
    }

    // ── Mở dialog Thêm (Controller quyết định dialog nào) ────
    public void openAddDialog(Component parent) {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(parent);
        new InvoiceAddDialog(frame).setVisible(true);
    }

    // ── Mở dialog Sửa ────────────────────────────────────────
    public void openEditDialog(Component parent, int id) {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(parent);
        new InvoiceEditDialog(frame, id).setVisible(true);
    }

    // ── Private helpers ───────────────────────────────────────
    private boolean matchesDateRange(Invoice hd, LocalDate from, LocalDate to) {
        if (from == null && to == null) return true;
        if (hd.getNgayLap() == null) return false;
        LocalDate ngayLap = hd.getNgayLap().toLocalDate();
        if (from != null && ngayLap.isBefore(from)) return false;
        if (to   != null && ngayLap.isAfter(to))   return false;
        return true;
    }

    private boolean matchesKeyword(Invoice hd, String keyword) {
        if (keyword == null || keyword.isEmpty()) return true;
        String row = String.join(" ",
            nvl(hd.getMaHDFormatted()),
            nvl(hd.getMaNVFormatted()),
            nvl(hd.getTenKhachHang()),
            nvl(hd.getSoDienThoai())
        ).toLowerCase();
        return row.contains(keyword.toLowerCase());
    }

    private Comparator<Invoice> buildComparator(int col) {
        switch (col) {
            case 0: return Comparator.comparingInt(Invoice::getMaHD);
            case 1: return Comparator.comparingInt(Invoice::getMaNV);
            case 2: return Comparator.comparing(hd -> nvl(hd.getTenKhachHang()));
            case 3: return Comparator.comparing(hd -> nvl(hd.getSoDienThoai()));
            case 4: return Comparator.comparing(
                        hd -> hd.getNgayLap() != null ? hd.getNgayLap() : LocalDateTime.MIN);
            case 5: return Comparator.comparingDouble(Invoice::getTongTien);
            default: return Comparator.comparingInt(Invoice::getMaHD);
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }
    
    private LocalDate parseDate(String text) {
        return LocalDate.parse(text.trim());
    }
}
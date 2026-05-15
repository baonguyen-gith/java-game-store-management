package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.RentalOrder;
import otkhongluong.gamestoremanagement.service.RentalService;

// ✅ XÓA toàn bộ các import Dialog và Swing:
// import otkhongluong.gamestoremanagement.dao.RentalOrderDAO;
// import otkhongluong.gamestoremanagement.view.dialog.RentAddDialog;
// import otkhongluong.gamestoremanagement.view.dialog.RentDetailDialog;
// import otkhongluong.gamestoremanagement.view.dialog.RentEditDialog;
// import otkhongluong.gamestoremanagement.view.dialog.RentExtendDialog;
// import otkhongluong.gamestoremanagement.view.dialog.RentReturnDialog;
// import javax.swing.*;
// import java.awt.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RentController {

    private final RentalService service;
    // ✅ XÓA: private final RentalOrderDAO dao;

    private int[] sortState = new int[7];
    private int sortCol = -1;
    private static final int PAGE_SIZE = 8;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ✅ Constructor chỉ nhận Service
    public RentController() {
        this.service = new RentalService();
    }

    public RentController(RentalService service) {
        this.service = service;
    }

    // ✅ Thêm 2 method delegate — Controller gọi Service, không gọi DAO trực tiếp
    public List<RentalOrder> getAllRentals() {
        return service.getAll();  // RentalService.getAll() đã có sẵn
    }

    public boolean deleteRental(int id) {
        return service.deletePhieuThue(id);  // RentalService.deletePhieuThue() đã có sẵn
    }

    // ✅ GIỮ NGUYÊN toàn bộ phần query/filter/sort/pagination
    public static class RentPageResult {
        public final List<RentalOrder> rows;
        public final int currentPage;
        public final int totalPages;
        public final int totalRows;
        public final boolean fromDateError;
        public final boolean toDateError;

        public RentPageResult(List<RentalOrder> rows, int currentPage,
                              int totalPages, int totalRows,
                              boolean fromDateError, boolean toDateError) {
            this.rows = rows;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalRows = totalRows;
            this.fromDateError = fromDateError;
            this.toDateError = toDateError;
        }

        public boolean hasDateError() { return fromDateError || toDateError; }
    }

    public int[] getSortState() { return sortState; }
    public int getSortCol()     { return sortCol; }

    public RentPageResult onSortChanged(int col, String from, String to, String kw) {
        sortState[col] = (sortState[col] + 1) % 3;
        sortCol = sortState[col] == 0 ? -1 : col;
        for (int i = 0; i < sortState.length; i++)
            if (i != col) sortState[i] = 0;
        return query(from, to, kw, 1);
    }

    public RentPageResult query(String fromStr, String toStr, String keyword, int page) {
        boolean fromErr = false, toErr = false;
        LocalDate from = null, to = null;

        try {
            if (!fromStr.trim().isEmpty()) from = parseDate(fromStr);
        } catch (Exception e) { fromErr = true; }

        try {
            if (!toStr.trim().isEmpty()) to = parseDate(toStr);
        } catch (Exception e) { toErr = true; }

        if (fromErr || toErr) {
            return new RentPageResult(new ArrayList<>(), page, 1, 0, fromErr, toErr);
        }

        boolean asc = sortCol < 0 || sortState[sortCol] != 2;
        List<RentalOrder> filtered = getFilteredRentals(from, to, keyword, sortCol, asc);

        int total = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        if (page > total) page = total;
        if (page < 1) page = 1;

        int fromIdx = (page - 1) * PAGE_SIZE;
        int toIdx   = Math.min(fromIdx + PAGE_SIZE, filtered.size());

        return new RentPageResult(filtered.subList(fromIdx, toIdx),
                                  page, total, filtered.size(), false, false);
    }

    public List<RentalOrder> getFilteredRentals(LocalDate from, LocalDate to,
                                                 String keyword, int sortCol,
                                                 boolean ascending) {
        // ✅ Dùng service.getAll() thay vì dao.findAll()
        List<RentalOrder> all = service.getAll();

        List<RentalOrder> result = all.stream()
            .filter(pt -> matchesDateRange(pt, from, to))
            .filter(pt -> matchesKeyword(pt, keyword))
            .collect(Collectors.toList());

        if (sortCol >= 0) {
            Comparator<RentalOrder> cmp = buildComparator(sortCol);
            if (!ascending) cmp = cmp.reversed();
            result.sort(cmp);
        }
        return result;
    }

    // ✅ XÓA 5 method openXxxDialog — không còn nữa:
    // public void openAddDialog(Component parent) { ... }
    // public void openAddDialogWithCD(Component parent, int maCD) { ... }
    // public void openAddDialogWithGame(Component parent, String tenGame) { ... }
    // public void openEditDialog(Component parent, RentalOrder pt) { ... }
    // public void openReturnDialog(Component parent, int id) { ... }
    // public void openExtendDialog(Component parent, int id) { ... }
    // public void openDetailDialog(Component parent, int id) { ... }

    private boolean matchesDateRange(RentalOrder pt, LocalDate from, LocalDate to) {
        if (from == null && to == null) return true;
        if (pt.getNgayThue() == null) return false;
        LocalDate ngayThue = pt.getNgayThue().toLocalDate();
        if (from != null && ngayThue.isBefore(from)) return false;
        if (to   != null && ngayThue.isAfter(to))   return false;
        return true;
    }

    private boolean matchesKeyword(RentalOrder pt, String keyword) {
        if (keyword == null || keyword.isEmpty()) return true;
        String row = String.join(" ",
            nvl(pt.getMaPTFormatted()),
            nvl(pt.getMaNVFormatted()),
            nvl(pt.getTenKhachHang()),
            nvl(pt.getSoDienThoai()),
            pt.getNgayThue()      != null ? pt.getNgayThue().toString()      : "",
            pt.getNgayTraDuKien() != null ? pt.getNgayTraDuKien().toString() : "",
            nvl(pt.getTrangThai())
        ).toLowerCase();
        return row.contains(keyword.toLowerCase());
    }

    private Comparator<RentalOrder> buildComparator(int col) {
        switch (col) {
            case 0: return Comparator.comparingInt(RentalOrder::getMaPT);
            case 1: return Comparator.comparingInt(RentalOrder::getMaNV);
            case 2: return Comparator.comparing(pt -> nvl(pt.getTenKhachHang()));
            case 3: return Comparator.comparing(pt -> nvl(pt.getSoDienThoai()));
            case 4: return Comparator.comparing(
                        pt -> pt.getNgayThue() != null ? pt.getNgayThue() : LocalDateTime.MIN);
            case 5: return Comparator.comparing(
                        pt -> pt.getNgayTraDuKien() != null ? pt.getNgayTraDuKien() : LocalDateTime.MIN);
            case 6: return Comparator.comparing(pt -> nvl(pt.getTrangThai()));
            default: return Comparator.comparingInt(RentalOrder::getMaPT);
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        return LocalDate.parse(text.trim(), FMT);
    }
}
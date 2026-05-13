package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Invoice;
import otkhongluong.gamestoremanagement.service.InvoiceService;
import otkhongluong.gamestoremanagement.view.dialog.InvoiceAddDialog;
import otkhongluong.gamestoremanagement.view.dialog.InvoiceEditDialog;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController() {
        this.service = new InvoiceService();
    }

    public InvoiceController(InvoiceService service) {
        this.service = service;
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
}
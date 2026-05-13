package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.dao.RentalOrderDAO;
import otkhongluong.gamestoremanagement.model.RentalOrder;
import otkhongluong.gamestoremanagement.service.RentalService;
import otkhongluong.gamestoremanagement.view.dialog.RentAddDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentDetailDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentEditDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentExtendDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentReturnDialog;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RentController {

    private final RentalService   service;
    private final RentalOrderDAO  dao;

    public RentController() {
        this.service = new RentalService();
        this.dao     = new RentalOrderDAO();
    }

    public RentController(RentalService service, RentalOrderDAO dao) {
        this.service = service;
        this.dao     = dao;
    }

    // ── Lấy toàn bộ dữ liệu ──────────────────────────────────
    public List<RentalOrder> getAllRentals() {
        return dao.findAll();
    }

    // ── Filter + Sort ────────────────────────────────────────
    public List<RentalOrder> getFilteredRentals(
            LocalDate from, LocalDate to,
            String keyword,
            int sortCol, boolean ascending) {

        List<RentalOrder> all = dao.findAll();

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

    // ── Xóa ──────────────────────────────────────────────────
    public boolean deleteRental(int id) {
        return dao.delete(id);
    }

    // ── Mở dialog Thêm ────────────────────────────────────────
    public void openAddDialog(Component parent) {
        Frame frame = getFrame(parent);
        new RentAddDialog(frame).setVisible(true);
    }

    // ── Mở dialog Thêm với CD được chọn sẵn theo mã ─────────
    public void openAddDialogWithCD(Component parent, int maCD) {
        Frame frame = getFrame(parent);
        RentAddDialog.openAndPreselectCD(frame, maCD);
    }

    // ── Mở dialog Thêm với CD được chọn sẵn theo tên game ───
    public void openAddDialogWithGame(Component parent, String tenGame) {
        Frame frame = getFrame(parent);
        RentAddDialog.openAndPreselectByGameName(frame, tenGame);
    }

    // ── Mở dialog Sửa ────────────────────────────────────────
    public void openEditDialog(Component parent, RentalOrder pt) {
        Frame frame = getFrame(parent);
        new RentEditDialog(frame, pt).setVisible(true);
    }

    // ── Mở dialog Chi tiết ───────────────────────────────────
    public void openDetailDialog(Component parent, int id) {
        Frame frame = getFrame(parent);
        RentDetailDialog d = new RentDetailDialog(frame, id);
        d.pack();
        d.setLocationRelativeTo(frame);
        d.setVisible(true);
    }

    // ── Mở dialog Trả CD ─────────────────────────────────────
    public void openReturnDialog(Component parent, int id) {
        Frame frame = getFrame(parent);
        new RentReturnDialog(frame, id).setVisible(true);
    }

    // ── Mở dialog Gia hạn ────────────────────────────────────
    public void openExtendDialog(Component parent, int id) {
        Frame frame = getFrame(parent);
        new RentExtendDialog(frame, id).setVisible(true);
    }

    // ── Private: filter helpers ───────────────────────────────
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
            pt.getNgayTraDuKien() != null ? pt.getNgayTraDuKien().toString()  : "",
            nvl(pt.getTrangThai())
        ).toLowerCase();
        return row.contains(keyword.toLowerCase());
    }

    // ── Private: sort ─────────────────────────────────────────
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

    // ── Private: util ─────────────────────────────────────────
    private Frame getFrame(Component parent) {
        return (Frame) SwingUtilities.getWindowAncestor(parent);
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
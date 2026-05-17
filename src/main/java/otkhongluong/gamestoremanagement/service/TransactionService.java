package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.TransactionDAO;
import otkhongluong.gamestoremanagement.model.TransactionDTO;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer cho Transaction (Lịch sử giao dịch).
 * ✅ FIX: dùng TransactionDTO thay vì Object[] thô.
 */
public class TransactionService {

    private final TransactionDAO dao;

    public TransactionService() {
        this.dao = new TransactionDAO();
    }

    // ══════════════════════════════════════════════════════════
    // LOAD
    // ══════════════════════════════════════════════════════════

    /**
     * Lấy toàn bộ lịch sử giao dịch (hóa đơn + phiếu thuê) từ DB.
     */
    public List<TransactionDTO> getAll() {
        return dao.findAll();
    }

    // ══════════════════════════════════════════════════════════
    // FILTER
    // ══════════════════════════════════════════════════════════

    /**
     * Lọc danh sách theo khoảng ngày, từ khóa và loại giao dịch.
     *
     * @param source  danh sách gốc (từ getAll())
     * @param from    ngày bắt đầu, null = không giới hạn
     * @param to      ngày kết thúc, null = không giới hạn
     * @param keyword từ khóa tìm kiếm (mã GD, loại, tên KH); "" = không lọc
     * @param loai    "Tất cả" | "Hóa đơn" | "Phiếu thuê"
     * @return danh sách đã lọc
     */
    public List<TransactionDTO> filter(List<TransactionDTO> source,
                                       LocalDate from, LocalDate to,
                                       String keyword, String loai) {
        if (source == null) return Collections.emptyList();

        final String kw    = (keyword == null) ? "" : keyword.trim().toLowerCase();
        final String loaiF = (loai    == null) ? "Tất cả" : loai;

        return source.stream().filter(dto -> {

            // ── Lọc loại ──────────────────────────────────────
            if (!"Tất cả".equals(loaiF)) {
                if (!nvl(dto.getLoai()).equalsIgnoreCase(loaiF)) return false;
            }

            // ── Lọc ngày ──────────────────────────────────────
            if (dto.getNgay() != null && (from != null || to != null)) {
                LocalDate ngay = dto.getNgay().toLocalDate();
                if (from != null && ngay.isBefore(from)) return false;
                if (to   != null && ngay.isAfter(to))   return false;
            }

            // ── Lọc keyword ───────────────────────────────────
            if (!kw.isEmpty()) {
                String searchTarget = (nvl(dto.getId()) + " "
                        + nvl(dto.getLoai()) + " "
                        + nvl(dto.getTenKhachHang())).toLowerCase();
                if (!searchTarget.contains(kw)) return false;
            }

            return true;
        }).collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════
    // SORT
    // ══════════════════════════════════════════════════════════

    /**
     * Sắp xếp danh sách theo cột và chiều.
     *
     * Ánh xạ colIndex → cột trong bảng TransactionPanel:
     *   0 = Mã GD, 1 = Loại, 2 = Mã NV, 3 = Khách hàng,
     *   4 = Ngày, 5 = Tiền
     *
     * @param source    danh sách cần sort (thao tác in-place)
     * @param colIndex  chỉ số cột; -1 = không sort
     * @param ascending true = tăng dần
     */
    public void sort(List<TransactionDTO> source, int colIndex, boolean ascending) {
        if (source == null || source.isEmpty() || colIndex < 0) return;

        Comparator<TransactionDTO> cmp;
        switch (colIndex) {
            case 0:  // Mã GD
                cmp = Comparator.comparing(dto -> nvl(dto.getId()));
                break;
            case 1:  // Loại
                cmp = Comparator.comparing(dto -> nvl(dto.getLoai()));
                break;
            case 2:  // Mã NV
                cmp = Comparator.comparingInt(TransactionDTO::getMaNV);
                break;
            case 3:  // Tên KH
                cmp = Comparator.comparing(dto -> nvl(dto.getTenKhachHang()));
                break;
            case 4:  // Ngày – null đẩy xuống cuối
                cmp = Comparator.comparing(
                    dto -> dto.getNgay() != null ? dto.getNgay() : java.time.LocalDateTime.MAX);
                break;
            case 5:  // Tiền
                cmp = Comparator.comparingDouble(TransactionDTO::getTien);
                break;
            default:
                return;
        }

        if (!ascending) cmp = cmp.reversed();
        source.sort(cmp);
    }

    // ══════════════════════════════════════════════════════════
    // HELPER
    // ══════════════════════════════════════════════════════════

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
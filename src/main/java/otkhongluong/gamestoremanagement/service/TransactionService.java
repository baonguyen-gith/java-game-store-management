package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.TransactionDAO;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer cho Transaction (Lịch sử giao dịch).
 *
 * Mỗi Object[] trong list đại diện 1 giao dịch, cấu trúc:
 *   [0] String   — Mã GD  ("HD001", "PT001", ...)
 *   [1] String   — Loại   ("Hóa đơn", "Phiếu thuê")
 *   [2] int      — Mã NV
 *   [3] String   — Tên khách hàng
 *   [4] Timestamp— Ngày giao dịch
 *   [5] double   — Tiền
 *   [6] String   — "Xem" (placeholder cho nút)
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
    public List<Object[]> getAll() {
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
    public List<Object[]> filter(List<Object[]> source,
                                  LocalDate from, LocalDate to,
                                  String keyword, String loai) {
        if (source == null) return Collections.emptyList();

        final String kw    = (keyword == null) ? "" : keyword.trim().toLowerCase();
        final String loaiF = (loai    == null) ? "Tất cả" : loai;

        return source.stream().filter(row -> {
            String maGD   = nvl((String) row[0]);
            String loaiGD = nvl((String) row[1]);
            String tenKH  = nvl((String) row[3]);
            Timestamp ts  = (Timestamp) row[4];

            // ── Lọc loại ──────────────────────────────────────
            if (!"Tất cả".equals(loaiF)) {
                if (!loaiGD.equalsIgnoreCase(loaiF)) return false;
            }

            // ── Lọc ngày ──────────────────────────────────────
            if (ts != null && (from != null || to != null)) {
                LocalDate ngay = ts.toLocalDateTime().toLocalDate();
                if (from != null && ngay.isBefore(from)) return false;
                if (to   != null && ngay.isAfter(to))   return false;
            }

            // ── Lọc keyword ───────────────────────────────────
            if (!kw.isEmpty()) {
                String searchTarget = (maGD + " " + loaiGD + " " + tenKH).toLowerCase();
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
    public void sort(List<Object[]> source, int colIndex, boolean ascending) {
        if (source == null || source.isEmpty() || colIndex < 0) return;

        Comparator<Object[]> cmp;
        switch (colIndex) {
            case 0:  // Mã GD – so sánh chuỗi
                cmp = Comparator.comparing(r -> nvl((String) r[0]));
                break;
            case 1:  // Loại
                cmp = Comparator.comparing(r -> nvl((String) r[1]));
                break;
            case 2:  // Mã NV – so sánh int
                cmp = Comparator.comparingInt(r -> (Integer) r[2]);
                break;
            case 3:  // Tên KH
                cmp = Comparator.comparing(r -> nvl((String) r[3]));
                break;
            case 4:  // Ngày – so sánh Timestamp (null đẩy xuống cuối)
                cmp = Comparator.comparingLong(
                    r -> r[4] instanceof Timestamp ? ((Timestamp) r[4]).getTime() : Long.MAX_VALUE);
                break;
            case 5:  // Tiền – so sánh double
                cmp = Comparator.comparingDouble(r -> (Double) r[5]);
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
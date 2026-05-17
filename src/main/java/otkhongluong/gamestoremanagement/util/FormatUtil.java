package otkhongluong.gamestoremanagement.util;

/**
 * FIX: thêm các helper format mã giao dịch để ReportDAO và UserDAO
 * không tự format chuỗi inline — tránh "HD12" vs "HD-0012" không nhất quán.
 */
public class FormatUtil {

    private FormatUtil() {}

    // ── Tiền ──────────────────────────────────────────
    /** "2,500,000 đ" */
    public static String formatTien(Double gia) {
        return gia == null ? "" : String.format("%,.0f đ", gia);
    }

    /** "2,500,000 đ/ngày" */
    public static String formatTienThue(Double gia) {
        return gia == null ? "" : String.format("%,.0f đ/ngày", gia);
    }

    /** long → "12,000,000 VNĐ" */
    public static String formatDoanhThu(long so) {
        return String.format("%,d VNĐ", so);
    }

    // ── Mã định danh ──────────────────────────────────
    /**
     * Format mã chung: prefix + số nguyên.
     * Ví dụ: formatMa("HD", 12) → "HD12"
     */
    public static String formatMa(String prefix, int ma) {
        return prefix + ma;
    }

    /**
     * Format mã có padding 4 chữ số — dùng cho Report.
     * Ví dụ: formatMaPad("CD", 5) → "CD-0005"
     */
    public static String formatMaPad(String prefix, int ma) {
        return prefix + "-" + String.format("%04d", ma);
    }

    /** "HD12" */
    public static String formatMaHD(int maHD)   { return formatMa("HD", maHD); }

    /** "PT5" */
    public static String formatMaPT(int maPT)   { return formatMa("PT", maPT); }

    /** "NV3" */
    public static String formatMaNV(int maNV)   { return maNV > 0 ? formatMa("NV", maNV) : ""; }

    /** "CD-0005" */
    public static String formatMaCD(int maCD)   { return formatMaPad("CD", maCD); }

    /** "PT-0005" */
    public static String formatMaPTPad(int maPT){ return formatMaPad("PT", maPT); }

    // ── Điểm ──────────────────────────────────────────
    /** "cong" → "➕ Cộng", "tru" → "➖ Trừ" */
    public static String formatLoaiDiem(String loai) {
        return "cong".equalsIgnoreCase(loai) ? "➕ Cộng" : "➖ Trừ";
    }
}
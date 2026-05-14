package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.DashboardStats;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO – Chứa tất cả truy vấn liên quan đến Dashboard.
 * Chỉ làm việc với DB, không chứa business logic.
 *
 * Giả sử tên bảng / cột như sau (chỉnh lại nếu DB bạn khác):
 *   HoaDon   : maHD, ngayLap (DATE), tongTien (DECIMAL)
 *   PhieuThue: maPhieu, ngayThue (DATE), tongTien (DECIMAL)
 */
public class DashboardDAO {

    // ───────────────────────────────────────────────────────────────────────
    // SQL – SQL Server dùng CAST(... AS DATE) để so sánh ngày
    // ───────────────────────────────────────────────────────────────────────

    /** Tổng doanh thu hóa đơn bán hôm nay */
    private static final String SQL_DOANH_THU_HOM_NAY =
            "SELECT ISNULL(SUM(tongTien), 0) FROM HoaDon " +
            "WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)";

    /** Tổng doanh thu hóa đơn bán trong tuần hiện tại (T2 → hôm nay) */
    private static final String SQL_DOANH_THU_TUAN =
            "SELECT ISNULL(SUM(tongTien), 0) FROM HoaDon " +
            "WHERE ngayLap >= DATEADD(DAY, 1 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) " +
            "  AND ngayLap <  DATEADD(DAY, 1, CAST(GETDATE() AS DATE))";

    /** Số hóa đơn bán hôm nay */
    private static final String SQL_SO_HOA_DON =
            "SELECT COUNT(*) FROM HoaDon " +
            "WHERE CAST(ngayLap AS DATE) = CAST(GETDATE() AS DATE)";

    /** Số phiếu thuê hôm nay */
    private static final String SQL_SO_PHIEU_THUE =
            "SELECT COUNT(*) FROM PhieuThue " +
            "WHERE CAST(ngayThue AS DATE) = CAST(GETDATE() AS DATE)";

    // ───────────────────────────────────────────────────────────────────────

    /**
     * Lấy toàn bộ số liệu thống kê trong một lần.
     * Mỗi query dùng connection riêng (pattern hiện tại của project).
     *
     * @return DashboardStats với dữ liệu mới nhất, hoặc object rỗng nếu lỗi.
     */
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        stats.setDoanhThuHomNay(queryLong(SQL_DOANH_THU_HOM_NAY));
        stats.setDoanhThuTuan(queryLong(SQL_DOANH_THU_TUAN));
        stats.setSoHoaDonHomNay((int) queryLong(SQL_SO_HOA_DON));
        stats.setSoPhieuThueHomNay((int) queryLong(SQL_SO_PHIEU_THUE));
        return stats;
    }

    // ─── Private helper ────────────────────────────────────────────────────

    /**
     * Thực thi một câu SELECT trả về một giá trị số duy nhất (COUNT / SUM).
     *
     * @param sql Câu truy vấn không có tham số (?)
     * @return Giá trị long, hoặc 0 nếu không có kết quả / lỗi.
     */
    private long queryLong(String sql) {
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] Lỗi truy vấn: " + e.getMessage());
        } finally {
            DBConnection.closeConnection(conn);
        }
        return 0L;
    }
}
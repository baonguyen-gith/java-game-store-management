package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.DashboardStats;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * FIX 1: tên bảng/cột đồng bộ với các DAO khác (HOADON, NgayLap, TongTien, PHIEUTHUE, NgayThue).
 * FIX 2: gộp 4 queries vào 1 connection duy nhất — tránh mở/đóng 4 lần.
 */
public class DashboardDAO {

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        String sqlDoanhThuHomNay =
            "SELECT ISNULL(SUM(TongTien), 0) FROM HOADON " +
            "WHERE CAST(NgayLap AS DATE) = CAST(GETDATE() AS DATE)";

        String sqlDoanhThuTuan =
            "SELECT ISNULL(SUM(TongTien), 0) FROM HOADON " +
            "WHERE NgayLap >= DATEADD(DAY, 1 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)) " +
            "  AND NgayLap <  DATEADD(DAY, 1, CAST(GETDATE() AS DATE))";

        String sqlSoHoaDon =
            "SELECT COUNT(*) FROM HOADON " +
            "WHERE CAST(NgayLap AS DATE) = CAST(GETDATE() AS DATE)";

        String sqlSoPhieuThue =
            "SELECT COUNT(*) FROM PHIEUTHUE " +
            "WHERE CAST(NgayThue AS DATE) = CAST(GETDATE() AS DATE)";

        // Gộp vào 1 connection — tránh mở 4 connection riêng
        try (Connection conn = DBConnection.getConnection()) {

            stats.setDoanhThuHomNay(queryLong(conn, sqlDoanhThuHomNay));
            stats.setDoanhThuTuan(queryLong(conn, sqlDoanhThuTuan));
            stats.setSoHoaDonHomNay((int) queryLong(conn, sqlSoHoaDon));
            stats.setSoPhieuThueHomNay((int) queryLong(conn, sqlSoPhieuThue));

        } catch (SQLException e) {
            System.err.println("[DashboardDAO] Lỗi kết nối: " + e.getMessage());
        }

        return stats;
    }

    // ── Helper dùng connection có sẵn ──────────────────
    private long queryLong(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            System.err.println("[DashboardDAO] Lỗi truy vấn: " + e.getMessage());
        }
        return 0L;
    }
}
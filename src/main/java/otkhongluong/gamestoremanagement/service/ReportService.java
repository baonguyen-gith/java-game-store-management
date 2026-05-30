package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.ReportDAO;
import otkhongluong.gamestoremanagement.controller.ReportController.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ReportService — chuyển raw data từ DAO sang DTO mà Controller/View dùng.
 * Không có SQL ở đây.
 */
public class ReportService {

    private final ReportDAO dao;

    public ReportService() {
        this.dao = new ReportDAO();
    }

    // ── Tab 1 ────────────────────────────────────────────────

    public OverviewKPI getOverviewKPI() {
        return new OverviewKPI(
            dao.getDoanhThuThang(),
            dao.getTienThueThang(),
            dao.getSoPhieuQuaHan(),
            dao.getSoKhachHoatDong()
        );
    }

    public List<ChartPoint> getWeeklyRevenue() {
        List<ChartPoint> result = new ArrayList<>();
        for (Object[] row : dao.getWeeklyRevenue())
            result.add(new ChartPoint((String) row[0], (double) row[1]));
        return result;
    }

    // ── Tab 2 ────────────────────────────────────────────────

    public List<Integer> getAvailableMonthYears() {
        return dao.getAvailableMonthYears();
    }

    public List<MonthlyRow> getMonthlyDetail(int month, int year) {
        List<MonthlyRow> rows = new ArrayList<>();
        for (Object[] r : dao.getMonthlyRows(month, year))
            rows.add(new MonthlyRow((String)r[0], (int)r[1], (double)r[2], (double)r[3]));
        return rows;
    }

    public double[][] getMonthlyChartData(int month, int year) {
        return dao.getMonthlyChartData(month, year);
    }

    // ── Tab 3 ────────────────────────────────────────────────

    public List<Integer> getAvailableYears() {
        return dao.getAvailableYears();
    }

    public List<YearlyRow> getYearlyDetail(int year) {
        List<YearlyRow> rows = new ArrayList<>();
        for (Object[] r : dao.getYearlyRows(year))
            rows.add(new YearlyRow((String)r[0], (int)r[1], (double)r[2], (int)r[3], (double)r[4]));
        return rows;
    }

    public YearlySummary getYearlySummary(int year) {
        double ban = 0, thue = 0;
        for (YearlyRow r : getYearlyDetail(year)) { ban += r.doanhThuBan; thue += r.tienThue; }
        return new YearlySummary(ban, thue);
    }

    public double[][] getYearlyChartData(int year) {
        return dao.getYearlyChartData(year);
    }

    // ── Tab 4 ────────────────────────────────────────────────

    public List<TopGameRow> getTopGameSold() {
        List<TopGameRow> rows = new ArrayList<>();
        for (Object[] r : dao.getTopGameSold())
            rows.add(new TopGameRow((int)r[0], (String)r[1], (String)r[2], (int)r[3], (double)r[4]));
        return rows;
    }

    public List<TopGameRow> getTopGameRented() {
        List<TopGameRow> rows = new ArrayList<>();
        for (Object[] r : dao.getTopGameRented())
            rows.add(new TopGameRow((int)r[0], (String)r[1], (String)r[2], (int)r[3], 0.0));
        return rows;
    }

    // ── Tab 5 ────────────────────────────────────────────────

    public CDStatusKPI getCDStatusKPI() {
        int[] c = dao.getCDStatusCounts();
        return new CDStatusKPI(c[0], c[1], c[2], c[3]);
    }

    public List<CDDetailRow> getCDStatusDetail() {
        List<CDDetailRow> rows = new ArrayList<>();
        for (Object[] r : dao.getCDStatusDetail())
            rows.add(new CDDetailRow((String)r[0], (String)r[1], (String)r[2], (String)r[3], (String)r[4]));
        return rows;
    }

    // ── Tab 6 ────────────────────────────────────────────────

    public OverdueKPI getOverdueKPI() {
        int[] c = dao.getOverdueCounts();
        return new OverdueKPI(c[0], dao.getOverdueTotalFine(), c[1]);
    }

    public List<OverdueRow> getOverdueList() {
        List<OverdueRow> rows = new ArrayList<>();
        for (Object[] r : dao.getOverdueList())
            rows.add(new OverdueRow((String)r[0], (String)r[1], (String)r[2],
                                    (String)r[3], (String)r[4], (int)r[5], (double)r[6]));
        return rows;
    }
    
    // Chốt thời điểm ngay khi quản lý bấm xuất, truyền vào query làm upper bound.
    // Mọi hóa đơn/phiếu thuê tạo sau thời điểm này đều bị loại khỏi báo cáo.
    public Object[] getMonthlyExportData(int month, int year) {
        java.sql.Timestamp snapshot = new java.sql.Timestamp(System.currentTimeMillis());
        List<Object[]> rows = dao.getMonthlyRowsForExport(month, year, snapshot);
        double totalBan = 0, totalThue = 0;
        for (Object[] row : rows) {
            totalBan  += ((Number) row[2]).doubleValue();
            totalThue += ((Number) row[3]).doubleValue();
        }
        return new Object[]{rows, totalBan, totalThue};
    }

    public Object[] getYearlyExportData(int year) {
        java.sql.Timestamp snapshot = new java.sql.Timestamp(System.currentTimeMillis());
        List<Object[]> rows = dao.getYearlyRowsForExport(year, snapshot);
        double totalBan = 0, totalThue = 0;
        for (Object[] row : rows) {
            totalBan  += ((Number) row[2]).doubleValue();
            totalThue += ((Number) row[4]).doubleValue();
        }
        return new Object[]{rows, totalBan, totalThue};
    }


    // ── Tab 7 ────────────────────────────────────────────────

    public List<VIPRow> getVIPCustomers(int year) {
        List<VIPRow> rows = new ArrayList<>();
        int rank = 1;
        for (Object[] r : dao.getVIPCustomers(year)) {
            String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : String.valueOf(rank);
            rows.add(new VIPRow(medal, (String)r[0], (String)r[1], (String)r[2],
                    (int)r[3], (double)r[4], (double)r[5]));
            rank++;
        }
        return rows;
    }
}
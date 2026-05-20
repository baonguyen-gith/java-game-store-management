package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.service.ReportService;
import otkhongluong.gamestoremanagement.util.ExportUtil;
import java.io.IOException;
import java.util.List;

/**
 * ReportController — chỉ uỷ thác cho ReportService.
 * Không còn DBConnection, không còn SQL.
 */
public class ReportController {

    private final ReportService reportService;

    public ReportController() {
        this.reportService = new ReportService();
    }

    // ── Tab 1 ────────────────────────────────────────────────
    public OverviewKPI      getOverviewKPI()          { return reportService.getOverviewKPI(); }
    public List<ChartPoint> getWeeklyRevenue()        { return reportService.getWeeklyRevenue(); }

    // ── Tab 2 ────────────────────────────────────────────────
    public List<Integer>    getAvailableMonthYears()              { return reportService.getAvailableMonthYears(); }
    public List<MonthlyRow> getMonthlyDetail(int m, int y)        { return reportService.getMonthlyDetail(m, y); }
    public double[][]       getMonthlyChartData(int m, int y)     { return reportService.getMonthlyChartData(m, y); }

    // ── Tab 3 ────────────────────────────────────────────────
    public List<Integer>    getAvailableYears()                   { return reportService.getAvailableYears(); }
    public List<YearlyRow>  getYearlyDetail(int y)                { return reportService.getYearlyDetail(y); }
    public YearlySummary    getYearlySummary(int y)               { return reportService.getYearlySummary(y); }
    public double[][]       getYearlyChartData(int y)             { return reportService.getYearlyChartData(y); }

    // ── Tab 4 ────────────────────────────────────────────────
    public List<TopGameRow> getTopGameSold()   { return reportService.getTopGameSold(); }
    public List<TopGameRow> getTopGameRented() { return reportService.getTopGameRented(); }

    // ── Tab 5 ────────────────────────────────────────────────
    public CDStatusKPI       getCDStatusKPI()    { return reportService.getCDStatusKPI(); }
    public List<CDDetailRow> getCDStatusDetail() { return reportService.getCDStatusDetail(); }

    // ── Tab 6 ────────────────────────────────────────────────
    public OverdueKPI       getOverdueKPI()   { return reportService.getOverdueKPI(); }
    public List<OverdueRow> getOverdueList()  { return reportService.getOverdueList(); }

    // ── Tab 7 ────────────────────────────────────────────────
    public List<VIPRow> getVIPCustomers() { return reportService.getVIPCustomers(); }

    // ── Shared util ───────────────────────────────────────────
    public static String formatMoney(double v) { return String.format("%,.0f đ", v); }

    // ==================== DTO (giữ nguyên, không đổi) ====================

    public static class OverviewKPI {
        public final double doanhThuThang, tienThueThang;
        public final int soPhieuQuaHan, soKhachHoatDong;
        public OverviewKPI(double dt, double tt, int qh, int kh) {
            doanhThuThang=dt; tienThueThang=tt; soPhieuQuaHan=qh; soKhachHoatDong=kh;
        }
    }
    public static class ChartPoint {
        public final String label; public final double value;
        public ChartPoint(String l, double v) { label=l; value=v; }
    }
    public static class MonthlyRow {
        public final String ngay; public final int soHD;
        public final double doanhThuBan, tienThue;
        public MonthlyRow(String n, int h, double b, double t) { ngay=n; soHD=h; doanhThuBan=b; tienThue=t; }
        public double tongNgay() { return doanhThuBan + tienThue; }
    }
    public static class YearlyRow {
        public final String tenThang; public final int soHD, soPT;
        public final double doanhThuBan, tienThue;
        public YearlyRow(String n, int h, double b, int p, double t) { tenThang=n; soHD=h; doanhThuBan=b; soPT=p; tienThue=t; }
        public double tongThang() { return doanhThuBan + tienThue; }
    }
    public static class YearlySummary {
        public final double tongBan, tongThue;
        public YearlySummary(double b, double t) { tongBan=b; tongThue=t; }
        public double tongCong() { return tongBan + tongThue; }
    }
    public static class TopGameRow {
        public final int rank, luot; public final String tenGame, theLoai; public final double doanhThu;
        public TopGameRow(int r, String n, String l, int lu, double d) { rank=r; tenGame=n; theLoai=l; luot=lu; doanhThu=d; }
    }
    public static class CDStatusKPI {
        public final int tong, sanSang, dangThue, daBanHong;
        public CDStatusKPI(int t, int ss, int dt, int dbh) { tong=t; sanSang=ss; dangThue=dt; daBanHong=dbh; }
    }
    public static class CDDetailRow {
        public final String maCD, tenGame, theLoai, tinhTrang, trangThai;
        public CDDetailRow(String ma, String ten, String loai, String tt, String tr) { maCD=ma; tenGame=ten; theLoai=loai; tinhTrang=tt; trangThai=tr; }
    }
    public static class OverdueKPI {
        public final int tongQuaHan, maxNgayQuaHan; public final double tongPhat;
        public OverdueKPI(int t, double p, int m) { tongQuaHan=t; tongPhat=p; maxNgayQuaHan=m; }
    }
    public static class OverdueRow {
        public final String maPT, hoTen, sdt, ngayThue, hanTra;
        public final int soNgayQuaHan; public final double tienPhat;
        public OverdueRow(String ma, String ten, String s, String nt, String ht, int ng, double p) {
            maPT=ma; hoTen=ten; sdt=s; ngayThue=nt; hanTra=ht; soNgayQuaHan=ng; tienPhat=p;
        }
    }
    public static class VIPRow {
        public final String medal, hoTen, sdt, email; public final int diemTichLuy;
        public final double tongMua, tongThue;
        public VIPRow(String m, String n, String s, String e, int d, double mu, String th) {
            medal=m; hoTen=n; sdt=s; email=e; diemTichLuy=d; tongMua=mu; tongThue=Double.parseDouble(th);
        }
        public VIPRow(String m, String n, String s, String e, int d, double mu, double th) {
            medal=m; hoTen=n; sdt=s; email=e; diemTichLuy=d; tongMua=mu; tongThue=th;
        }
        public double tongCong() { return tongMua + tongThue; }
    }
    
    public void exportMonthlyExcel(int month, int year, String filePath) throws IOException {
        Object[] data        = reportService.getMonthlyExportData(month, year);
        List<Object[]> rows  = (List<Object[]>) data[0];
        double totalBan      = (double) data[1];
        double totalThue     = (double) data[2];

        ExportUtil.exportMonthlyExcel(filePath, month, year, rows, totalBan, totalThue);
    }

    public void exportYearlyExcel(int year, String filePath) throws IOException {
        Object[] data        = reportService.getYearlyExportData(year);
        List<Object[]> rows  = (List<Object[]>) data[0];
        double totalBan      = (double) data[1];
        double totalThue     = (double) data[2];

        ExportUtil.exportYearlyExcel(filePath, year, rows, totalBan, totalThue);
    }
}
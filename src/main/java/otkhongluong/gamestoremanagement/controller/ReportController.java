package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.RevenueDTO;
import otkhongluong.gamestoremanagement.service.ReportService;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ReportController — trung gian giữa ReportPanel (View) và ReportService / DB.
 *
 * View KHÔNG được gọi DBConnection trực tiếp và KHÔNG được gọi ReportService trực tiếp.
 * Tất cả query, xử lý dữ liệu, format tiền đều nằm ở đây.
 *
 * Phân nhóm method theo từng Tab của ReportPanel:
 *   Tab 1 OVERVIEW    — getOverviewKPI(), getWeeklyRevenue()
 *   Tab 2 MONTHLY     — getMonthlyDetail(), getAvailableMonthYears()
 *   Tab 3 YEARLY      — getYearlyDetail(), getAvailableYears()
 *   Tab 4 TOP GAME    — getTopGameSold(), getTopGameRented()
 *   Tab 5 CD STATUS   — getCDStatusKPI(), getCDStatusDetail()
 *   Tab 6 OVERDUE     — getOverdueKPI(), getOverdueList()
 *   Tab 7 VIP         — getVIPCustomers()
 *   SHARED UTILS      — formatMoney()
 */
public class ReportController {

    private final ReportService reportService;

    public ReportController() {
        this.reportService = new ReportService();
    }

    // =================================================================
    // DTO nội bộ — View dùng các class này để hiển thị, không dùng raw SQL
    // =================================================================

    /** KPI tổng quan Tab 1 */
    public static class OverviewKPI {
        public final double doanhThuThang;
        public final double tienThueThang;
        public final int    soPhieuQuaHan;
        public final int    soKhachHoatDong;
        public OverviewKPI(double dt, double tt, int qh, int kh) {
            doanhThuThang   = dt;
            tienThueThang   = tt;
            soPhieuQuaHan   = qh;
            soKhachHoatDong = kh;
        }
    }

    /** Một điểm dữ liệu trên bar chart (nhãn + giá trị) */
    public static class ChartPoint {
        public final String label;
        public final double value;
        public ChartPoint(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    /** Một dòng trong bảng doanh thu tháng */
    public static class MonthlyRow {
        public final String ngay;
        public final int    soHD;
        public final double doanhThuBan;
        public final double tienThue;
        public MonthlyRow(String ngay, int soHD, double ban, double thue) {
            this.ngay        = ngay;
            this.soHD        = soHD;
            this.doanhThuBan = ban;
            this.tienThue    = thue;
        }
        public double tongNgay() { return doanhThuBan + tienThue; }
    }

    /** Một dòng trong bảng doanh thu năm */
    public static class YearlyRow {
        public final String tenThang;
        public final int    soHD;
        public final double doanhThuBan;
        public final int    soPT;
        public final double tienThue;
        public YearlyRow(String ten, int hd, double ban, int pt, double thue) {
            this.tenThang    = ten;
            this.soHD        = hd;
            this.doanhThuBan = ban;
            this.soPT        = pt;
            this.tienThue    = thue;
        }
        public double tongThang() { return doanhThuBan + tienThue; }
    }

    /** Tổng năm cho các badge hiển thị */
    public static class YearlySummary {
        public final double tongBan;
        public final double tongThue;
        public YearlySummary(double ban, double thue) {
            this.tongBan  = ban;
            this.tongThue = thue;
        }
        public double tongCong() { return tongBan + tongThue; }
    }

    /** Một dòng top game */
    public static class TopGameRow {
        public final int    rank;
        public final String tenGame;
        public final String theLoai;
        public final int    luot;
        public final double doanhThu; // 0 nếu là bảng thuê
        public TopGameRow(int rank, String ten, String loai, int luot, double dt) {
            this.rank     = rank;
            this.tenGame  = ten;
            this.theLoai  = loai;
            this.luot     = luot;
            this.doanhThu = dt;
        }
    }

    /** KPI tình trạng CD Tab 5 */
    public static class CDStatusKPI {
        public final int tong;
        public final int sanSang;
        public final int dangThue;
        public final int daBanHong;
        public CDStatusKPI(int t, int ss, int dt, int dbh) {
            tong      = t;
            sanSang   = ss;
            dangThue  = dt;
            daBanHong = dbh;
        }
    }

    /** Một dòng chi tiết CD */
    public static class CDDetailRow {
        public final String maCD;
        public final String tenGame;
        public final String theLoai;
        public final String tinhTrang;
        public final String trangThai;
        public CDDetailRow(String ma, String ten, String loai, String tt, String tr) {
            this.maCD      = ma;
            this.tenGame   = ten;
            this.theLoai   = loai;
            this.tinhTrang = tt;
            this.trangThai = tr;
        }
    }

    /** KPI quá hạn Tab 6 */
    public static class OverdueKPI {
        public final int    tongQuaHan;
        public final double tongPhat;
        public final int    maxNgayQuaHan;
        public OverdueKPI(int tqh, double tp, int max) {
            tongQuaHan   = tqh;
            tongPhat     = tp;
            maxNgayQuaHan = max;
        }
    }

    /** Một dòng phiếu quá hạn */
    public static class OverdueRow {
        public final String maPT;
        public final String hoTen;
        public final String sdt;
        public final String ngayThue;
        public final String hanTra;
        public final int    soNgayQuaHan;
        public final double tienPhat;
        public OverdueRow(String ma, String ten, String sdt, String nt, String ht, int ng, double phat) {
            this.maPT          = ma;
            this.hoTen         = ten;
            this.sdt           = sdt;
            this.ngayThue      = nt;
            this.hanTra        = ht;
            this.soNgayQuaHan  = ng;
            this.tienPhat      = phat;
        }
    }

    /** Một dòng khách VIP */
    public static class VIPRow {
        public final String medal;
        public final String hoTen;
        public final String sdt;
        public final String email;
        public final int    diemTichLuy;
        public final double tongMua;
        public final double tongThue;
        public VIPRow(String medal, String ten, String sdt, String email, int diem, double mua, double thue) {
            this.medal       = medal;
            this.hoTen       = ten;
            this.sdt         = sdt;
            this.email       = email;
            this.diemTichLuy = diem;
            this.tongMua     = mua;
            this.tongThue    = thue;
        }
        public double tongCong() { return tongMua + tongThue; }
    }

    // =================================================================
    // TAB 1 — TỔNG QUAN
    // =================================================================

    public OverviewKPI getOverviewKPI() {
        double doanhThuThang = queryDouble(
            "SELECT ISNULL(SUM(TongTien),0) FROM HOADON " +
            "WHERE MONTH(NgayLap)=MONTH(GETDATE()) AND YEAR(NgayLap)=YEAR(GETDATE()) " +
            "AND TrangThai='DaThanhToan'");

        double tienThueThang = queryDouble(
            "SELECT ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) " +
            "FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT=ct.MaPT " +
            "WHERE MONTH(pt.NgayThue)=MONTH(GETDATE()) AND YEAR(pt.NgayThue)=YEAR(GETDATE()) " +
            "AND pt.TrangThai='DaTra'");

        int soPhieuQuaHan = queryInt(
            "SELECT COUNT(*) FROM PHIEUTHUE " +
            "WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");

        int soKhachHoatDong = queryInt(
            "SELECT COUNT(DISTINCT MaKH) FROM HOADON " +
            "WHERE MONTH(NgayLap)=MONTH(GETDATE()) AND YEAR(NgayLap)=YEAR(GETDATE()) " +
            "AND MaKH IS NOT NULL");

        return new OverviewKPI(doanhThuThang, tienThueThang, soPhieuQuaHan, soKhachHoatDong);
    }

    /** 7 điểm dữ liệu cho mini bar chart doanh thu bán 7 ngày gần nhất. */
    public List<ChartPoint> getWeeklyRevenue() {
        List<ChartPoint> result = new ArrayList<>();
        DateTimeFormatter fmt   = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate today         = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            double val  = queryDouble(
                "SELECT ISNULL(SUM(TongTien),0) FROM HOADON " +
                "WHERE CAST(NgayLap AS DATE)='" + d + "' AND TrangThai='DaThanhToan'");
            result.add(new ChartPoint(d.format(fmt), val));
        }
        return result;
    }

    // =================================================================
    // TAB 2 — DOANH THU THÁNG
    // =================================================================

    /** Danh sách năm có hóa đơn — dùng cho combobox Tab 2. */
    public List<Integer> getAvailableMonthYears() {
        return getYearsWithData("HOADON", "NgayLap");
    }

    /** Dữ liệu bảng + chart cho tháng/năm được chọn. */
    public List<MonthlyRow> getMonthlyDetail(int month, int year) {
        List<MonthlyRow> rows = new ArrayList<>();

        Map<Integer, double[]> dayMap  = new TreeMap<>();
        Map<Integer, Double>   rentMap = new HashMap<>();

        try (Connection con = DBConnection.getConnection()) {
            // Doanh thu bán
            PreparedStatement ps = con.prepareStatement(
                "SELECT DAY(NgayLap) as D, COUNT(*) as SoHD, ISNULL(SUM(TongTien),0) as DT " +
                "FROM HOADON WHERE MONTH(NgayLap)=? AND YEAR(NgayLap)=? AND TrangThai='DaThanhToan' " +
                "GROUP BY DAY(NgayLap)");
            ps.setInt(1, month); ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                dayMap.put(rs.getInt("D"), new double[]{rs.getInt("SoHD"), rs.getDouble("DT")});

            // Doanh thu thuê — chỉ DaTra
            PreparedStatement ps2 = con.prepareStatement(
                "SELECT DAY(pt.NgayThue) as D, " +
                "ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as TT " +
                "FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT=ct.MaPT " +
                "WHERE MONTH(pt.NgayThue)=? AND YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' " +
                "GROUP BY DAY(pt.NgayThue)");
            ps2.setInt(1, month); ps2.setInt(2, year);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) rentMap.put(rs2.getInt("D"), rs2.getDouble("TT"));

        } catch (Exception ex) { ex.printStackTrace(); }

        for (int d = 1; d <= 31; d++) {
            double ban  = dayMap.containsKey(d) ? dayMap.get(d)[1] : 0;
            double thue = rentMap.getOrDefault(d, 0.0);
            if (ban == 0 && thue == 0) continue;
            int soHD = dayMap.containsKey(d) ? (int) dayMap.get(d)[0] : 0;
            String label = String.format("%02d/%02d/%04d", d, month, year);
            rows.add(new MonthlyRow(label, soHD, ban, thue));
        }
        return rows;
    }

    /**
     * Dữ liệu riêng cho MonthlyBarChart (ban[] + thue[] theo ngày, index = ngày-1).
     * Trả về double[2][31]: [0]=ban, [1]=thue.
     */
    public double[][] getMonthlyChartData(int month, int year) {
        double[] ban  = new double[31];
        double[] thue = new double[31];
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT DAY(NgayLap) as D, ISNULL(SUM(TongTien),0) as V FROM HOADON " +
                "WHERE MONTH(NgayLap)=? AND YEAR(NgayLap)=? AND TrangThai='DaThanhToan' " +
                "GROUP BY DAY(NgayLap)");
            ps.setInt(1, month); ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ban[rs.getInt("D") - 1] = rs.getDouble("V");

            PreparedStatement ps2 = con.prepareStatement(
                "SELECT DAY(pt.NgayThue) as D, " +
                "ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as V " +
                "FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT=ct.MaPT " +
                "WHERE MONTH(pt.NgayThue)=? AND YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' " +
                "GROUP BY DAY(pt.NgayThue)");
            ps2.setInt(1, month); ps2.setInt(2, year);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) thue[rs2.getInt("D") - 1] = rs2.getDouble("V");
        } catch (Exception ex) { ex.printStackTrace(); }
        return new double[][]{ban, thue};
    }

    // =================================================================
    // TAB 3 — DOANH THU NĂM
    // =================================================================

    /** Danh sách năm có dữ liệu (gom cả bán + thuê hoàn thành), giảm dần. */
    public List<Integer> getAvailableYears() {
        List<Integer> years = getYearsWithData("HOADON", "NgayLap");
        for (int y : getYearsFiltered("PHIEUTHUE", "NgayThue", "TrangThai='DaTra'")) {
            if (!years.contains(y)) years.add(y);
        }
        Collections.sort(years, Collections.reverseOrder());
        return years;
    }

    /** Dữ liệu bảng 12 tháng cho năm được chọn. */
    public List<YearlyRow> getYearlyDetail(int year) {
        double[] banArr  = new double[12];
        double[] thueArr = new double[12];
        int[]    hdArr   = new int[12];
        int[]    ptArr   = new int[12];

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT MONTH(NgayLap) as M, COUNT(*) as SoHD, ISNULL(SUM(TongTien),0) as DT " +
                "FROM HOADON WHERE YEAR(NgayLap)=? AND TrangThai='DaThanhToan' GROUP BY MONTH(NgayLap)");
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int m = rs.getInt("M") - 1;
                banArr[m] = rs.getDouble("DT");
                hdArr[m]  = rs.getInt("SoHD");
            }

            PreparedStatement ps2 = con.prepareStatement(
                "SELECT MONTH(pt.NgayThue) as M, COUNT(DISTINCT pt.MaPT) as SoPT, " +
                "ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as TT " +
                "FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT=ct.MaPT " +
                "WHERE YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' GROUP BY MONTH(pt.NgayThue)");
            ps2.setInt(1, year);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                int m = rs2.getInt("M") - 1;
                thueArr[m] = rs2.getDouble("TT");
                ptArr[m]   = rs2.getInt("SoPT");
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        String[] tenThang = {"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                             "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};
        List<YearlyRow> rows = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            if (banArr[i] == 0 && thueArr[i] == 0) continue;
            rows.add(new YearlyRow(tenThang[i], hdArr[i], banArr[i], ptArr[i], thueArr[i]));
        }
        return rows;
    }

    /** Tổng năm để cập nhật badge Bán / Thuê / Tổng. */
    public YearlySummary getYearlySummary(int year) {
        List<YearlyRow> rows = getYearlyDetail(year);
        double ban = 0, thue = 0;
        for (YearlyRow r : rows) { ban += r.doanhThuBan; thue += r.tienThue; }
        return new YearlySummary(ban, thue);
    }

    /**
     * Dữ liệu cho YearlyBarChart (banArr[12] + thueArr[12]).
     * Trả về double[2][12]: [0]=ban, [1]=thue.
     */
    public double[][] getYearlyChartData(int year) {
        double[] ban  = new double[12];
        double[] thue = new double[12];
        for (YearlyRow r : getYearlyDetail(year)) {
            // index lấy lại từ tên tháng qua getYearlyDetail đã đúng thứ tự
            // Để tránh parse tên, ta query lại đơn giản
        }
        // Query trực tiếp để lấy đúng index tháng
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT MONTH(NgayLap) as M, ISNULL(SUM(TongTien),0) as DT " +
                "FROM HOADON WHERE YEAR(NgayLap)=? AND TrangThai='DaThanhToan' GROUP BY MONTH(NgayLap)");
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ban[rs.getInt("M") - 1] = rs.getDouble("DT");

            PreparedStatement ps2 = con.prepareStatement(
                "SELECT MONTH(pt.NgayThue) as M, " +
                "ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as TT " +
                "FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT=ct.MaPT " +
                "WHERE YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' GROUP BY MONTH(pt.NgayThue)");
            ps2.setInt(1, year);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) thue[rs2.getInt("M") - 1] = rs2.getDouble("TT");
        } catch (Exception ex) { ex.printStackTrace(); }
        return new double[][]{ban, thue};
    }

    // =================================================================
    // TAB 4 — TOP GAME
    // =================================================================

    public List<TopGameRow> getTopGameSold() {
        List<TopGameRow> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT TOP 15 g.TenGame, g.TheLoai, " +
                "ISNULL(SUM(ct.SoLuong),0) as LuotBan, ISNULL(SUM(ct.SoLuong*ct.DonGia),0) as DT " +
                "FROM GAME g JOIN SANPHAM sp ON g.MaGame=sp.MaGame " +
                "JOIN CTHOADON ct ON sp.MaSP=ct.MaSP " +
                "JOIN HOADON h ON ct.MaHD=h.MaHD AND h.TrangThai='DaThanhToan' " +
                "GROUP BY g.TenGame, g.TheLoai ORDER BY DT DESC");
            int rank = 1;
            while (rs.next())
                rows.add(new TopGameRow(rank++, rs.getString("TenGame"), rs.getString("TheLoai"),
                    rs.getInt("LuotBan"), rs.getDouble("DT")));
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    public List<TopGameRow> getTopGameRented() {
        List<TopGameRow> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT TOP 15 g.TenGame, g.TheLoai, COUNT(ctp.MaCD) as LuotThue " +
                "FROM GAME g JOIN SANPHAM sp ON g.MaGame=sp.MaGame " +
                "JOIN CD c ON sp.MaSP=c.MaSP " +
                "JOIN CTPHIEUTHUE ctp ON c.MaCD=ctp.MaCD " +
                "GROUP BY g.TenGame, g.TheLoai ORDER BY LuotThue DESC");
            int rank = 1;
            while (rs.next())
                rows.add(new TopGameRow(rank++, rs.getString("TenGame"), rs.getString("TheLoai"),
                    rs.getInt("LuotThue"), 0));
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // =================================================================
    // TAB 5 — TÌNH TRẠNG CD
    // =================================================================

    public CDStatusKPI getCDStatusKPI() {
        int sanSang  = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='SanSang'");
        int dangThue = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='DangThue'");
        int daBan    = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='DaBan'");
        int hong     = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='Hong'");
        return new CDStatusKPI(sanSang + dangThue + daBan + hong, sanSang, dangThue, daBan + hong);
    }

    public List<CDDetailRow> getCDStatusDetail() {
        List<CDDetailRow> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT c.MaCD, g.TenGame, g.TheLoai, c.TinhTrang, c.TrangThai " +
                "FROM CD c JOIN SANPHAM sp ON c.MaSP=sp.MaSP " +
                "JOIN GAME g ON sp.MaGame=g.MaGame ORDER BY c.TrangThai, g.TenGame");
            while (rs.next())
                rows.add(new CDDetailRow(
                    "CD-" + String.format("%04d", rs.getInt("MaCD")),
                    rs.getString("TenGame"), rs.getString("TheLoai"),
                    rs.getString("TinhTrang"), rs.getString("TrangThai")));
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // =================================================================
    // TAB 6 — QUÁ HẠN
    // =================================================================

    public OverdueKPI getOverdueKPI() {
        int    tong   = queryInt("SELECT COUNT(*) FROM PHIEUTHUE WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");
        double phat   = queryDouble("SELECT ISNULL(SUM(TienPhat),0) FROM PHIEUTHUE WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");
        int    maxNgay = queryInt("SELECT ISNULL(MAX(DATEDIFF(day,NgayTraDuKien,GETDATE())),0) FROM PHIEUTHUE WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");
        return new OverdueKPI(tong, phat, maxNgay);
    }

    public List<OverdueRow> getOverdueList() {
        List<OverdueRow> rows = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT pt.MaPT, kh.HoTen, kh.SDT, pt.NgayThue, pt.NgayTraDuKien, " +
                "DATEDIFF(day,pt.NgayTraDuKien,GETDATE()) as SoNgay, ISNULL(pt.TienPhat,0) as Phat " +
                "FROM PHIEUTHUE pt JOIN KHACHHANG kh ON pt.MaKH=kh.MaKH " +
                "WHERE pt.TrangThai='DangThue' AND pt.NgayTraDuKien < GETDATE() ORDER BY SoNgay DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String nt = rs.getTimestamp("NgayThue")       != null
                    ? rs.getTimestamp("NgayThue").toLocalDateTime().format(fmt) : "";
                String ht = rs.getTimestamp("NgayTraDuKien")  != null
                    ? rs.getTimestamp("NgayTraDuKien").toLocalDateTime().format(fmt) : "";
                rows.add(new OverdueRow(
                    "PT-" + String.format("%04d", rs.getInt("MaPT")),
                    rs.getString("HoTen"), rs.getString("SDT"),
                    nt, ht, rs.getInt("SoNgay"), rs.getDouble("Phat")));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // =================================================================
    // TAB 7 — KHÁCH VIP
    // =================================================================

    public List<VIPRow> getVIPCustomers() {
        List<VIPRow> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT kh.HoTen, kh.SDT, kh.Email, kh.DiemTichLuy, " +
                "ISNULL(b.TongMua,0) as TongMua, ISNULL(r.TongThue,0) as TongThue " +
                "FROM KHACHHANG kh " +
                "LEFT JOIN (SELECT MaKH, SUM(TongTien) as TongMua FROM HOADON " +
                "           WHERE TrangThai='DaThanhToan' GROUP BY MaKH) b ON kh.MaKH=b.MaKH " +
                "LEFT JOIN (SELECT pt.MaKH, " +
                "           ISNULL(SUM(ct.DonGiaThue),0)+ISNULL(SUM(pt.TienPhat),0) as TongThue " +
                "           FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT=ct.MaPT " +
                "           WHERE pt.TrangThai='DaTra' GROUP BY pt.MaKH) r ON kh.MaKH=r.MaKH " +
                "WHERE kh.HoTen IS NOT NULL " +
                "ORDER BY (ISNULL(b.TongMua,0)+ISNULL(r.TongThue,0)) DESC");
            int rank = 1;
            while (rs.next()) {
                double mua  = rs.getDouble("TongMua");
                double thue = rs.getDouble("TongThue");
                if (mua == 0 && thue == 0) continue;
                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : String.valueOf(rank);
                rows.add(new VIPRow(medal, rs.getString("HoTen"), rs.getString("SDT"),
                    rs.getString("Email"), rs.getInt("DiemTichLuy"), mua, thue));
                rank++;
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // =================================================================
    // SHARED UTILS — dùng chung cho View khi format hiển thị
    // =================================================================

    public static String formatMoney(double v) {
        return String.format("%,.0f đ", v);
    }

    // =================================================================
    // PRIVATE HELPERS
    // =================================================================

    private double queryDouble(String sql) {
        try (Connection con = DBConnection.getConnection();
             ResultSet rs   = con.createStatement().executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }

    private int queryInt(String sql) {
        try (Connection con = DBConnection.getConnection();
             ResultSet rs   = con.createStatement().executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }

    private List<Integer> getYearsWithData(String table, String dateCol) {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(" + dateCol + ") as Y FROM " + table +
                     " WHERE " + dateCol + " IS NOT NULL ORDER BY Y DESC";
        try (Connection con = DBConnection.getConnection();
             ResultSet rs   = con.createStatement().executeQuery(sql)) {
            while (rs.next()) years.add(rs.getInt("Y"));
        } catch (Exception ex) { ex.printStackTrace(); }
        return years;
    }

    private List<Integer> getYearsFiltered(String table, String dateCol, String whereExtra) {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(" + dateCol + ") as Y FROM " + table +
                     " WHERE " + dateCol + " IS NOT NULL AND " + whereExtra + " ORDER BY Y DESC";
        try (Connection con = DBConnection.getConnection();
             ResultSet rs   = con.createStatement().executeQuery(sql)) {
            while (rs.next()) years.add(rs.getInt("Y"));
        } catch (Exception ex) { ex.printStackTrace(); }
        return years;
    }
}
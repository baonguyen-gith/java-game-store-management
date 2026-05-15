package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ReportDAO — toàn bộ SQL báo cáo nằm ở đây.
 * Service và Controller không được chạm vào DBConnection.
 */
public class ReportDAO {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ==================== Tab 1: TỔNG QUAN ====================

    public double getDoanhThuThang() {
        return queryDouble(
            "SELECT ISNULL(SUM(TongTien),0) FROM HOADON " +
            "WHERE MONTH(NgayLap)=MONTH(GETDATE()) AND YEAR(NgayLap)=YEAR(GETDATE()) " +
            "AND TrangThai='DaThanhToan'");
    }

    public double getTienThueThang() {
        return queryDouble(
            "SELECT ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) " +
            "FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT=ct.MaPT " +
            "WHERE MONTH(pt.NgayThue)=MONTH(GETDATE()) AND YEAR(pt.NgayThue)=YEAR(GETDATE()) " +
            "AND pt.TrangThai='DaTra'");
    }

    public int getSoPhieuQuaHan() {
        return queryInt(
            "SELECT COUNT(*) FROM PHIEUTHUE " +
            "WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");
    }

    public int getSoKhachHoatDong() {
        return queryInt(
            "SELECT COUNT(DISTINCT MaKH) FROM HOADON " +
            "WHERE MONTH(NgayLap)=MONTH(GETDATE()) AND YEAR(NgayLap)=YEAR(GETDATE()) " +
            "AND MaKH IS NOT NULL");
    }

    /** Trả về List<Object[]> {label, value} cho 7 ngày gần nhất */
    public List<Object[]> getWeeklyRevenue() {
        List<Object[]> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            double val = queryDouble(
                "SELECT ISNULL(SUM(TongTien),0) FROM HOADON " +
                "WHERE CAST(NgayLap AS DATE)='" + d + "' AND TrangThai='DaThanhToan'");
            result.add(new Object[]{d.format(fmt), val});
        }
        return result;
    }

    // ==================== Tab 2: THÁNG ====================

    public List<Integer> getAvailableMonthYears() {
        return getDistinctYears("HOADON", "NgayLap", null);
    }

    /** Trả List<Object[]> {label, soHD, doanhThuBan, tienThue} */
    public List<Object[]> getMonthlyRows(int month, int year) {
        Map<Integer, double[]> dayMap  = new TreeMap<>();
        Map<Integer, Double>   rentMap = new HashMap<>();

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT DAY(NgayLap) as D, COUNT(*) as SoHD, ISNULL(SUM(TongTien),0) as DT " +
                "FROM HOADON WHERE MONTH(NgayLap)=? AND YEAR(NgayLap)=? AND TrangThai='DaThanhToan' " +
                "GROUP BY DAY(NgayLap)");
            ps.setInt(1, month); ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                dayMap.put(rs.getInt("D"), new double[]{rs.getInt("SoHD"), rs.getDouble("DT")});

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

        List<Object[]> rows = new ArrayList<>();
        for (int d = 1; d <= 31; d++) {
            double ban  = dayMap.containsKey(d) ? dayMap.get(d)[1] : 0;
            double thue = rentMap.getOrDefault(d, 0.0);
            if (ban == 0 && thue == 0) continue;
            int soHD = dayMap.containsKey(d) ? (int) dayMap.get(d)[0] : 0;
            rows.add(new Object[]{
                String.format("%02d/%02d/%04d", d, month, year), soHD, ban, thue
            });
        }
        return rows;
    }

    /** Trả double[2][31]: [0]=ban, [1]=thue */
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

    // ==================== Tab 3: NĂM ====================

    public List<Integer> getAvailableYears() {
        List<Integer> years = getDistinctYears("HOADON", "NgayLap", null);
        for (int y : getDistinctYears("PHIEUTHUE", "NgayThue", "TrangThai='DaTra'")) {
            if (!years.contains(y)) years.add(y);
        }
        Collections.sort(years, Collections.reverseOrder());
        return years;
    }

    /** Trả List<Object[]> {tenThang, soHD, doanhThuBan, soPT, tienThue} */
    public List<Object[]> getYearlyRows(int year) {
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

        String[] ten = {"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                        "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            if (banArr[i] == 0 && thueArr[i] == 0) continue;
            rows.add(new Object[]{ten[i], hdArr[i], banArr[i], ptArr[i], thueArr[i]});
        }
        return rows;
    }

    /** Trả double[2][12]: [0]=ban, [1]=thue */
    public double[][] getYearlyChartData(int year) {
        double[] ban  = new double[12];
        double[] thue = new double[12];
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

    // ==================== Tab 4: TOP GAME ====================

    /** Trả List<Object[]> {rank, tenGame, theLoai, luotBan, doanhThu} */
    public List<Object[]> getTopGameSold() {
        List<Object[]> rows = new ArrayList<>();
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
                rows.add(new Object[]{rank++, rs.getString("TenGame"),
                    rs.getString("TheLoai"), rs.getInt("LuotBan"), rs.getDouble("DT")});
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    /** Trả List<Object[]> {rank, tenGame, theLoai, luotThue, 0.0} */
    public List<Object[]> getTopGameRented() {
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT TOP 15 g.TenGame, g.TheLoai, COUNT(ctp.MaCD) as LuotThue " +
                "FROM GAME g JOIN SANPHAM sp ON g.MaGame=sp.MaGame " +
                "JOIN CD c ON sp.MaSP=c.MaSP " +
                "JOIN CTPHIEUTHUE ctp ON c.MaCD=ctp.MaCD " +
                "GROUP BY g.TenGame, g.TheLoai ORDER BY LuotThue DESC");
            int rank = 1;
            while (rs.next())
                rows.add(new Object[]{rank++, rs.getString("TenGame"),
                    rs.getString("TheLoai"), rs.getInt("LuotThue"), 0.0});
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // ==================== Tab 5: CD STATUS ====================

    /** Trả int[] {tong, sanSang, dangThue, daBanHong} */
    public int[] getCDStatusCounts() {
        int sanSang  = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='SanSang'");
        int dangThue = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='DangThue'");
        int daBan    = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='DaBan'");
        int hong     = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='Hong'");
        return new int[]{sanSang + dangThue + daBan + hong, sanSang, dangThue, daBan + hong};
    }

    /** Trả List<Object[]> {maCD, tenGame, theLoai, tinhTrang, trangThai} */
    public List<Object[]> getCDStatusDetail() {
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT c.MaCD, g.TenGame, g.TheLoai, c.TinhTrang, c.TrangThai " +
                "FROM CD c JOIN SANPHAM sp ON c.MaSP=sp.MaSP " +
                "JOIN GAME g ON sp.MaGame=g.MaGame ORDER BY c.TrangThai, g.TenGame");
            while (rs.next())
                rows.add(new Object[]{
                    "CD-" + String.format("%04d", rs.getInt("MaCD")),
                    rs.getString("TenGame"), rs.getString("TheLoai"),
                    rs.getString("TinhTrang"), rs.getString("TrangThai")});
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // ==================== Tab 6: QUÁ HẠN ====================

    /** Trả int[] {tong, maxNgay} và double tongPhat */
    public int[] getOverdueCounts() {
        int tong   = queryInt("SELECT COUNT(*) FROM PHIEUTHUE WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");
        int maxNgay = queryInt("SELECT ISNULL(MAX(DATEDIFF(day,NgayTraDuKien,GETDATE())),0) FROM PHIEUTHUE WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");
        return new int[]{tong, maxNgay};
    }

    public double getOverdueTotalFine() {
        return queryDouble("SELECT ISNULL(SUM(TienPhat),0) FROM PHIEUTHUE WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");
    }

    /** Trả List<Object[]> {maPT, hoTen, sdt, ngayThue, hanTra, soNgay, tienPhat} */
    public List<Object[]> getOverdueList() {
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT pt.MaPT, kh.HoTen, kh.SDT, pt.NgayThue, pt.NgayTraDuKien, " +
                "DATEDIFF(day,pt.NgayTraDuKien,GETDATE()) as SoNgay, ISNULL(pt.TienPhat,0) as Phat " +
                "FROM PHIEUTHUE pt JOIN KHACHHANG kh ON pt.MaKH=kh.MaKH " +
                "WHERE pt.TrangThai='DangThue' AND pt.NgayTraDuKien < GETDATE() ORDER BY SoNgay DESC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String nt = rs.getTimestamp("NgayThue") != null
                    ? rs.getTimestamp("NgayThue").toLocalDateTime().format(FMT) : "";
                String ht = rs.getTimestamp("NgayTraDuKien") != null
                    ? rs.getTimestamp("NgayTraDuKien").toLocalDateTime().format(FMT) : "";
                rows.add(new Object[]{
                    "PT-" + String.format("%04d", rs.getInt("MaPT")),
                    rs.getString("HoTen"), rs.getString("SDT"),
                    nt, ht, rs.getInt("SoNgay"), rs.getDouble("Phat")});
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // ==================== Tab 7: VIP ====================

    /** Trả List<Object[]> {hoTen, sdt, email, diemTichLuy, tongMua, tongThue} */
    public List<Object[]> getVIPCustomers() {
        List<Object[]> rows = new ArrayList<>();
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
            while (rs.next()) {
                double mua  = rs.getDouble("TongMua");
                double thue = rs.getDouble("TongThue");
                if (mua == 0 && thue == 0) continue;
                rows.add(new Object[]{
                    rs.getString("HoTen"), rs.getString("SDT"), rs.getString("Email"),
                    rs.getInt("DiemTichLuy"), mua, thue});
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // ==================== PRIVATE HELPERS ====================

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

    private List<Integer> getDistinctYears(String table, String dateCol, String whereExtra) {
        List<Integer> years = new ArrayList<>();
        String where = (whereExtra != null) ? " AND " + whereExtra : "";
        String sql   = "SELECT DISTINCT YEAR(" + dateCol + ") as Y FROM " + table
                     + " WHERE " + dateCol + " IS NOT NULL" + where + " ORDER BY Y DESC";
        try (Connection con = DBConnection.getConnection();
             ResultSet rs   = con.createStatement().executeQuery(sql)) {
            while (rs.next()) years.add(rs.getInt("Y"));
        } catch (Exception ex) { ex.printStackTrace(); }
        return years;
    }
}
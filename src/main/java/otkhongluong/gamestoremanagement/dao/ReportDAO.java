package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.util.DBConnection;
import otkhongluong.gamestoremanagement.util.FormatUtil;

import java.sql.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * FIX 1: getDistinctYears() dùng whitelist enum thay vì nối String tự do vào SQL.
 * FIX 2: formatMa dùng FormatUtil thay vì tự format inline.
 */
public class ReportDAO {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Whitelist bảng được phép truy vấn — tránh SQL injection qua tên bảng
    private enum ReportTable {
        HOADON("HOADON", "NgayLap", null),
        PHIEUTHUE_DATRA("PHIEUTHUE", "NgayThue", "TrangThai='DaTra'");

        final String table;
        final String dateCol;
        final String whereExtra;

        ReportTable(String table, String dateCol, String whereExtra) {
            this.table      = table;
            this.dateCol    = dateCol;
            this.whereExtra = whereExtra;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Tab 1: TỔNG QUAN
    // ══════════════════════════════════════════════════════════════════

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

    public List<Object[]> getWeeklyRevenue() {
        List<Object[]> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate today = LocalDate.now();

        String sql =
            "SELECT ISNULL(SUM(TongTien),0) FROM HOADON " +
            "WHERE CAST(NgayLap AS DATE) = ? AND TrangThai='DaThanhToan'";

        for (int i = 6; i >= 0; i--) {
            LocalDate d   = today.minusDays(i);
            double    val = queryDoubleByDate(sql, d);
            result.add(new Object[]{d.format(fmt), val});
        }
        return result;
    }

    // ══════════════════════════════════════════════════════════════════
    // Tab 2: THÁNG
    // ══════════════════════════════════════════════════════════════════

    public List<Integer> getAvailableMonthYears() {
        List<Integer> years = getDistinctYears(ReportTable.HOADON);
        for (int y : getDistinctYears(ReportTable.PHIEUTHUE_DATRA)) {
            if (!years.contains(y)) years.add(y);
        }
        years.sort(Comparator.reverseOrder());
        return years;
    }

    public List<Object[]> getMonthlyRows(int month, int year) {
        Map<Integer, double[]> dayMap  = new TreeMap<>();
        Map<Integer, Double>   rentMap = new HashMap<>();

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT DAY(NgayLap) as D, COUNT(*) as SoHD, " +
                    "ISNULL(SUM(TongTien),0) as DT " +
                    "FROM HOADON " +
                    "WHERE MONTH(NgayLap)=? AND YEAR(NgayLap)=? AND TrangThai='DaThanhToan' " +
                    "GROUP BY DAY(NgayLap)")) {
                ps.setInt(1, month); ps.setInt(2, year);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        dayMap.put(rs.getInt("D"),
                            new double[]{rs.getInt("SoHD"), rs.getDouble("DT")});
                }
            }
            try (PreparedStatement ps2 = con.prepareStatement(
                    "SELECT DAY(pt.NgayThue) as D, " +
                    "ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as TT " +
                    "FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT=ct.MaPT " +
                    "WHERE MONTH(pt.NgayThue)=? AND YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' " +
                    "GROUP BY DAY(pt.NgayThue)")) {
                ps2.setInt(1, month); ps2.setInt(2, year);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while (rs2.next())
                        rentMap.put(rs2.getInt("D"), rs2.getDouble("TT"));
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        List<Object[]> rows = new ArrayList<>();
        for (int d = 1; d <= 31; d++) {
            double ban  = dayMap.containsKey(d) ? dayMap.get(d)[1] : 0;
            double thue = rentMap.getOrDefault(d, 0.0);
            if (ban == 0 && thue == 0) continue;
            int soHD = dayMap.containsKey(d) ? (int) dayMap.get(d)[0] : 0;
            rows.add(new Object[]{
                String.format("%02d/%02d/%04d", d, month, year),
                soHD, ban, thue
            });
        }
        return rows;
    }

    public double[][] getMonthlyChartData(int month, int year) {
        double[] ban  = new double[31];
        double[] thue = new double[31];

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT DAY(NgayLap) as D, ISNULL(SUM(TongTien),0) as V " +
                    "FROM HOADON " +
                    "WHERE MONTH(NgayLap)=? AND YEAR(NgayLap)=? AND TrangThai='DaThanhToan' " +
                    "GROUP BY DAY(NgayLap)")) {
                ps.setInt(1, month); ps.setInt(2, year);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ban[rs.getInt("D") - 1] = rs.getDouble("V");
                }
            }
            try (PreparedStatement ps2 = con.prepareStatement(
                    "SELECT DAY(pt.NgayThue) as D, " +
                    "ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as V " +
                    "FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT=ct.MaPT " +
                    "WHERE MONTH(pt.NgayThue)=? AND YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' " +
                    "GROUP BY DAY(pt.NgayThue)")) {
                ps2.setInt(1, month); ps2.setInt(2, year);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while (rs2.next()) thue[rs2.getInt("D") - 1] = rs2.getDouble("V");
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        return new double[][]{ban, thue};
    }

    // ══════════════════════════════════════════════════════════════════
    // Tab 3: NĂM
    // ══════════════════════════════════════════════════════════════════

    public List<Integer> getAvailableYears() {
        List<Integer> years = getDistinctYears(ReportTable.HOADON);
        for (int y : getDistinctYears(ReportTable.PHIEUTHUE_DATRA)) {
            if (!years.contains(y)) years.add(y);
        }
        Collections.sort(years, Collections.reverseOrder());
        return years;
    }

    public List<Object[]> getYearlyRows(int year) {
        double[] banArr  = new double[12];
        double[] thueArr = new double[12];
        int[]    hdArr   = new int[12];
        int[]    ptArr   = new int[12];

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT MONTH(NgayLap) as M, COUNT(*) as SoHD, " +
                    "ISNULL(SUM(TongTien),0) as DT " +
                    "FROM HOADON WHERE YEAR(NgayLap)=? AND TrangThai='DaThanhToan' " +
                    "GROUP BY MONTH(NgayLap)")) {
                ps.setInt(1, year);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int m = rs.getInt("M") - 1;
                        banArr[m] = rs.getDouble("DT"); hdArr[m] = rs.getInt("SoHD");
                    }
                }
            }
            try (PreparedStatement ps2 = con.prepareStatement(
                    "SELECT MONTH(pt.NgayThue) as M, COUNT(DISTINCT pt.MaPT) as SoPT, " +
                    "ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as TT " +
                    "FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT=ct.MaPT " +
                    "WHERE YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' " +
                    "GROUP BY MONTH(pt.NgayThue)")) {
                ps2.setInt(1, year);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while (rs2.next()) {
                        int m = rs2.getInt("M") - 1;
                        thueArr[m] = rs2.getDouble("TT"); ptArr[m] = rs2.getInt("SoPT");
                    }
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        String[] ten = {
            "Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
            "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"
        };
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            if (banArr[i] == 0 && thueArr[i] == 0) continue;
            rows.add(new Object[]{ten[i], hdArr[i], banArr[i], ptArr[i], thueArr[i]});
        }
        return rows;
    }

    public double[][] getYearlyChartData(int year) {
        double[] ban  = new double[12];
        double[] thue = new double[12];

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT MONTH(NgayLap) as M, ISNULL(SUM(TongTien),0) as DT " +
                    "FROM HOADON WHERE YEAR(NgayLap)=? AND TrangThai='DaThanhToan' " +
                    "GROUP BY MONTH(NgayLap)")) {
                ps.setInt(1, year);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ban[rs.getInt("M") - 1] = rs.getDouble("DT");
                }
            }
            try (PreparedStatement ps2 = con.prepareStatement(
                    "SELECT MONTH(pt.NgayThue) as M, " +
                    "ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as TT " +
                    "FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT=ct.MaPT " +
                    "WHERE YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' " +
                    "GROUP BY MONTH(pt.NgayThue)")) {
                ps2.setInt(1, year);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while (rs2.next()) thue[rs2.getInt("M") - 1] = rs2.getDouble("TT");
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        return new double[][]{ban, thue};
    }

    // ══════════════════════════════════════════════════════════════════
    // Tab 4: TOP GAME
    // ══════════════════════════════════════════════════════════════════

    public List<Object[]> getTopGameSold() {
        List<Object[]> rows = new ArrayList<>();
        String sql =
            "SELECT TOP 15 g.TenGame, g.TheLoai, " +
            "ISNULL(SUM(ct.SoLuong),0) as LuotBan, " +
            "ISNULL(SUM(ct.SoLuong * ct.DonGia),0) as DT " +
            "FROM GAME g " +
            "JOIN SANPHAM sp ON g.MaGame = sp.MaGame " +
            "JOIN CTHOADON ct ON sp.MaSP = ct.MaSP " +
            "JOIN HOADON h ON ct.MaHD = h.MaHD AND h.TrangThai='DaThanhToan' " +
            "GROUP BY g.TenGame, g.TheLoai ORDER BY DT DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int rank = 1;
            while (rs.next())
                rows.add(new Object[]{rank++, rs.getString("TenGame"), rs.getString("TheLoai"),
                    rs.getInt("LuotBan"), rs.getDouble("DT")});
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    public List<Object[]> getTopGameRented() {
        List<Object[]> rows = new ArrayList<>();
        String sql =
            "SELECT TOP 15 g.TenGame, g.TheLoai, COUNT(ctp.MaCD) as LuotThue " +
            "FROM GAME g " +
            "JOIN SANPHAM sp ON g.MaGame = sp.MaGame " +
            "JOIN CD c ON sp.MaSP = c.MaSP " +
            "JOIN CTPHIEUTHUE ctp ON c.MaCD = ctp.MaCD " +
            "GROUP BY g.TenGame, g.TheLoai ORDER BY LuotThue DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int rank = 1;
            while (rs.next())
                rows.add(new Object[]{rank++, rs.getString("TenGame"), rs.getString("TheLoai"),
                    rs.getInt("LuotThue"), 0.0});
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // ══════════════════════════════════════════════════════════════════
    // Tab 5: TÌNH TRẠNG ĐĨA CD
    // ══════════════════════════════════════════════════════════════════

    public int[] getCDStatusCounts() {
        int sanSang  = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='SanSang'");
        int dangThue = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='DangThue'");
        int daBan    = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='DaBan'");
        int hong     = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='Hong'");
        return new int[]{sanSang + dangThue + daBan + hong, sanSang, dangThue, daBan + hong};
    }

    public List<Object[]> getCDStatusDetail() {
        List<Object[]> rows = new ArrayList<>();
        String sql =
            "SELECT c.MaCD, g.TenGame, g.TheLoai, c.TinhTrang, c.TrangThai " +
            "FROM CD c " +
            "JOIN SANPHAM sp ON c.MaSP = sp.MaSP " +
            "JOIN GAME g ON sp.MaGame = g.MaGame " +
            "ORDER BY c.TrangThai, g.TenGame";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                rows.add(new Object[]{
                    FormatUtil.formatMaCD(rs.getInt("MaCD")),   // FIX: dùng FormatUtil
                    rs.getString("TenGame"),
                    rs.getString("TheLoai"),
                    rs.getString("TinhTrang"),
                    rs.getString("TrangThai")
                });
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // ══════════════════════════════════════════════════════════════════
    // Tab 6: QUÁ HẠN
    // ══════════════════════════════════════════════════════════════════

    public int[] getOverdueCounts() {
        int tong = queryInt(
            "SELECT COUNT(*) FROM PHIEUTHUE " +
            "WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");
        int maxNgay = queryInt(
            "SELECT ISNULL(MAX(DATEDIFF(day,NgayTraDuKien,GETDATE())),0) " +
            "FROM PHIEUTHUE WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");
        return new int[]{tong, maxNgay};
    }

    public double getOverdueTotalFine() {
        return queryDouble(
            "SELECT ISNULL(SUM( " +
            "    pt.TienPhat + " +                          // phạt cũ đã lưu (gia hạn trước)
            "    DATEDIFF(day, pt.NgayTraDuKien, GETDATE()) " +
            "        * dongia.TongGia * 1.5 " +            // phạt trễ mới tính động
            "), 0) " +
            "FROM PHIEUTHUE pt " +
            "JOIN ( " +
            "    SELECT ct.MaPT, SUM(sp.GiaThueNgay) AS TongGia " +
            "    FROM CTPHIEUTHUE ct " +
            "    JOIN CD      cd ON ct.MaCD   = cd.MaCD " +
            "    JOIN SANPHAM sp ON cd.MaSP   = sp.MaSP " +
            "    GROUP BY ct.MaPT " +
            ") dongia ON dongia.MaPT = pt.MaPT " +
            "WHERE pt.TrangThai = 'DangThue' " +
            "  AND pt.NgayTraDuKien < GETDATE()");
    }

    public List<Object[]> getOverdueList() {
        List<Object[]> rows = new ArrayList<>();
        String sql =
            "SELECT pt.MaPT, kh.HoTen, kh.SDT, pt.NgayThue, pt.NgayTraDuKien, " +
            "DATEDIFF(day, pt.NgayTraDuKien, GETDATE()) AS SoNgay, " +
            "pt.TienPhat + " +
            "    DATEDIFF(day, pt.NgayTraDuKien, GETDATE()) * dongia.TongGia * 1.5 AS Phat " +
            "FROM PHIEUTHUE pt " +
            "JOIN KHACHHANG kh ON pt.MaKH = kh.MaKH " +
            "JOIN ( " +
            "    SELECT ct.MaPT, SUM(sp.GiaThueNgay) AS TongGia " +
            "    FROM CTPHIEUTHUE ct " +
            "    JOIN CD      cd ON ct.MaCD = cd.MaCD " +
            "    JOIN SANPHAM sp ON cd.MaSP = sp.MaSP " +
            "    GROUP BY ct.MaPT " +
            ") dongia ON dongia.MaPT = pt.MaPT " +
            "WHERE pt.TrangThai = 'DangThue' " +
            "  AND pt.NgayTraDuKien < GETDATE() " +
            "ORDER BY SoNgay DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nt = rs.getTimestamp("NgayThue") != null
                    ? rs.getTimestamp("NgayThue").toLocalDateTime().format(FMT) : "";
                String ht = rs.getTimestamp("NgayTraDuKien") != null
                    ? rs.getTimestamp("NgayTraDuKien").toLocalDateTime().format(FMT) : "";
                rows.add(new Object[]{
                    FormatUtil.formatMaPTPad(rs.getInt("MaPT")),
                    rs.getString("HoTen"), rs.getString("SDT"),
                    nt, ht, rs.getInt("SoNgay"), rs.getDouble("Phat")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }
    
    public List<Object[]> getDueSoonList() {
        List<Object[]> rows = new ArrayList<>();
        String sql =
            "SELECT pt.MaPT, kh.HoTen, kh.SDT, pt.NgayThue, pt.NgayTraDuKien, " +
            "DATEDIFF(minute, GETDATE(), pt.NgayTraDuKien) AS SoPhut " +
            "FROM PHIEUTHUE pt JOIN KHACHHANG kh ON pt.MaKH = kh.MaKH " +
            "WHERE pt.TrangThai='DangThue' " +
            "  AND pt.NgayTraDuKien >= GETDATE() " +
            "  AND pt.NgayTraDuKien < DATEADD(day, 1, GETDATE()) " +
            "ORDER BY pt.NgayTraDuKien ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nt = rs.getTimestamp("NgayThue") != null
                    ? rs.getTimestamp("NgayThue").toLocalDateTime().format(FMT) : "";
                String ht = rs.getTimestamp("NgayTraDuKien") != null
                    ? rs.getTimestamp("NgayTraDuKien").toLocalDateTime().format(FMT) : "";
                int soPhut = rs.getInt("SoPhut");
                String conLai = soPhut >= 60
                    ? (soPhut / 60) + " giờ " + (soPhut % 60) + " phút"
                    : soPhut + " phút";
                rows.add(new Object[]{
                    FormatUtil.formatMaPTPad(rs.getInt("MaPT")),
                    rs.getString("HoTen"), rs.getString("SDT"),
                    nt, ht, conLai
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // ══════════════════════════════════════════════════════════════════
    // Tab 7: KHÁCH HÀNG VIP
    // ══════════════════════════════════════════════════════════════════

    public List<Object[]> getVIPCustomers() {
        List<Object[]> rows = new ArrayList<>();
        String sql =
            "SELECT kh.HoTen, kh.SDT, kh.Email, kh.DiemTichLuy, " +
            "ISNULL(b.TongMua,0) as TongMua, ISNULL(r.TongThue,0) as TongThue " +
            "FROM KHACHHANG kh " +
            "LEFT JOIN (" +
            "    SELECT MaKH, SUM(TongTien) as TongMua FROM HOADON " +
            "    WHERE TrangThai='DaThanhToan' GROUP BY MaKH" +
            ") b ON kh.MaKH = b.MaKH " +
            "LEFT JOIN (" +
            "    SELECT pt.MaKH, " +
            "    ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as TongThue " +
            "    FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
            "    WHERE pt.TrangThai='DaTra' GROUP BY pt.MaKH" +
            ") r ON kh.MaKH = r.MaKH " +
            "WHERE kh.HoTen IS NOT NULL " +
            "ORDER BY (ISNULL(b.TongMua,0) + ISNULL(r.TongThue,0)) DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double mua  = rs.getDouble("TongMua");
                double thue = rs.getDouble("TongThue");
                if (mua == 0 && thue == 0) continue;
                rows.add(new Object[]{
                    rs.getString("HoTen"), rs.getString("SDT"), rs.getString("Email"),
                    rs.getInt("DiemTichLuy"), mua, thue
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    // ══════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════

    private double queryDouble(String sql) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }

    private int queryInt(String sql) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }

    private double queryDoubleByDate(String sql, LocalDate date) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }

    /**
     * FIX: dùng whitelist enum thay vì String tự do — tránh SQL injection qua tên bảng.
     */
    private List<Integer> getDistinctYears(ReportTable t) {
        List<Integer> years = new ArrayList<>();
        // Tên bảng/cột lấy từ enum cố định — không có user input
        String where = (t.whereExtra != null) ? " AND " + t.whereExtra : "";
        String sql   =
            "SELECT DISTINCT YEAR(" + t.dateCol + ") as Y " +
            "FROM " + t.table +
            " WHERE " + t.dateCol + " IS NOT NULL" + where +
            " ORDER BY Y DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) years.add(rs.getInt("Y"));
        } catch (Exception ex) { ex.printStackTrace(); }
        return years;
    }
    
    /**
     * Truy vấn doanh thu tháng để xuất Excel.
     *
     * Nhận snapshotTime = thời điểm quản lý bấm "Xuất Excel".
     * Tất cả filter đều dùng NgayLap < snapshotTime và NgayThue < snapshotTime
     * → bất kỳ hóa đơn/phiếu thuê nào được tạo SAU thời điểm đó đều bị loại,
     *   hoàn toàn không phụ thuộc vào isolation level hay DB config.
     *
     * Schema trả về: { String ngay "dd/MM/yyyy", int soHD, double doanhThuBan, double tienThue }
     */
    public List<Object[]> getMonthlyRowsForExport(int month, int year, Timestamp snapshotTime) {
        String sql =
            "WITH ban AS ( " +
            "    SELECT DAY(NgayLap) AS D, COUNT(*) AS SoHD, " +
            "           ISNULL(SUM(TongTien), 0) AS DT " +
            "    FROM HOADON " +
            "    WHERE MONTH(NgayLap)=? AND YEAR(NgayLap)=? " +
            "      AND TrangThai='DaThanhToan' " +
            "      AND NgayLap < ? " +
            "    GROUP BY DAY(NgayLap) " +
            "), thue AS ( " +
            "    SELECT DAY(pt.NgayThue) AS D, " +
            "           ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) AS TT " +
            "    FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
            "    WHERE MONTH(pt.NgayThue)=? AND YEAR(pt.NgayThue)=? " +
            "      AND pt.TrangThai='DaTra' " +
            "      AND pt.NgayThue < ? " +
            "    GROUP BY DAY(pt.NgayThue) " +
            ") " +
            "SELECT COALESCE(b.D, t.D) AS D, " +
            "       ISNULL(b.SoHD, 0)  AS SoHD, " +
            "       ISNULL(b.DT, 0)    AS DT, " +
            "       ISNULL(t.TT, 0)    AS TT " +
            "FROM ban b FULL OUTER JOIN thue t ON b.D = t.D " +
            "WHERE ISNULL(b.DT,0) + ISNULL(t.TT,0) > 0 " +
            "ORDER BY D";

        List<Object[]> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, month); ps.setInt(2, year); ps.setTimestamp(3, snapshotTime); // ban
            ps.setInt(4, month); ps.setInt(5, year); ps.setTimestamp(6, snapshotTime); // thue
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int d = rs.getInt("D");
                    rows.add(new Object[]{
                        String.format("%02d/%02d/%04d", d, month, year),
                        rs.getInt("SoHD"), rs.getDouble("DT"), rs.getDouble("TT")
                    });
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }

    /**
     * Truy vấn doanh thu năm để xuất Excel.
     *
     * Cùng cơ chế snapshot timestamp với getMonthlyRowsForExport.
     *
     * Schema trả về: { String tenThang, int soHD, double doanhThuBan, int soPT, double tienThue }
     */
    public List<Object[]> getYearlyRowsForExport(int year, Timestamp snapshotTime) {
        String sql =
            "WITH ban AS ( " +
            "    SELECT MONTH(NgayLap) AS M, COUNT(*) AS SoHD, " +
            "           ISNULL(SUM(TongTien), 0) AS DT " +
            "    FROM HOADON " +
            "    WHERE YEAR(NgayLap)=? AND TrangThai='DaThanhToan' " +
            "      AND NgayLap < ? " +
            "    GROUP BY MONTH(NgayLap) " +
            "), thue AS ( " +
            "    SELECT MONTH(pt.NgayThue) AS M, COUNT(DISTINCT pt.MaPT) AS SoPT, " +
            "           ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) AS TT " +
            "    FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
            "    WHERE YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' " +
            "      AND pt.NgayThue < ? " +
            "    GROUP BY MONTH(pt.NgayThue) " +
            ") " +
            "SELECT COALESCE(b.M, t.M) AS M, " +
            "       ISNULL(b.SoHD, 0)  AS SoHD, " +
            "       ISNULL(b.DT,   0)  AS DT, " +
            "       ISNULL(t.SoPT, 0)  AS SoPT, " +
            "       ISNULL(t.TT,   0)  AS TT " +
            "FROM ban b FULL OUTER JOIN thue t ON b.M = t.M " +
            "WHERE ISNULL(b.DT,0) + ISNULL(t.TT,0) > 0 " +
            "ORDER BY M";

        String[] tenThang = {
            "Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
            "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"
        };

        List<Object[]> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year); ps.setTimestamp(2, snapshotTime); // ban
            ps.setInt(3, year); ps.setTimestamp(4, snapshotTime); // thue
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int m = rs.getInt("M");
                    rows.add(new Object[]{
                        tenThang[m - 1],
                        rs.getInt("SoHD"), rs.getDouble("DT"),
                        rs.getInt("SoPT"), rs.getDouble("TT")
                    });
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }
    
    public List<Object[]> getVIPCustomers(int year) {
        List<Object[]> rows = new ArrayList<>();
        String sql =
            "SELECT kh.HoTen, kh.SDT, kh.Email, kh.DiemTichLuy, " +
            "ISNULL(b.TongMua,0) as TongMua, ISNULL(r.TongThue,0) as TongThue " +
            "FROM KHACHHANG kh " +
            "LEFT JOIN (" +
            "    SELECT MaKH, SUM(TongTien) as TongMua FROM HOADON " +
            "    WHERE TrangThai='DaThanhToan' AND YEAR(NgayLap)=? GROUP BY MaKH" +
            ") b ON kh.MaKH = b.MaKH " +
            "LEFT JOIN (" +
            "    SELECT pt.MaKH, " +
            "    ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as TongThue " +
            "    FROM PHIEUTHUE pt JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
            "    WHERE pt.TrangThai='DaTra' AND YEAR(pt.NgayThue)=? GROUP BY pt.MaKH" +
            ") r ON kh.MaKH = r.MaKH " +
            "WHERE kh.HoTen IS NOT NULL " +
            "AND (ISNULL(b.TongMua,0) + ISNULL(r.TongThue,0)) > 0 " +
            "ORDER BY (ISNULL(b.TongMua,0) + ISNULL(r.TongThue,0)) DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getString("HoTen"), rs.getString("SDT"), rs.getString("Email"),
                        rs.getInt("DiemTichLuy"), rs.getDouble("TongMua"), rs.getDouble("TongThue")
                    });
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return rows;
    }
}
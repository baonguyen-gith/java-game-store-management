package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.util.DBConnection;
import otkhongluong.gamestoremanagement.util.UIStyle;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * ReportPanel — Màn hình Thống kê / Báo cáo
 *
 * Tab 1: Tổng quan       — KPI cards + mini bar chart doanh thu 7 ngày
 * Tab 2: Doanh thu tháng — Bảng + bar chart từng ngày, lọc tháng/năm
 * Tab 3: Doanh thu năm   — Bar chart 12 tháng, combobox chỉ năm có dữ liệu
 * Tab 4: Top Game        — Ranking game bán / thuê nhiều nhất
 * Tab 5: Tình trạng CD   — Summary KPI + bảng chi tiết
 * Tab 6: Quá hạn         — Phiếu thuê quá hạn, tiền phạt
 * Tab 7: Khách VIP       — Top khách hàng theo tổng chi tiêu
 *
 * FIX:
 *   - Tất cả query SUM(TienCoc+TienPhat) FROM PHIEUTHUE đều lọc TrangThai='DaTra'
 *     → chỉ tính phiếu đã hoàn thành, không tính cọc đang giữ
 *   - Nút Refresh rebuild lại tab đang xem
 *   - makeTabButton lưu key vào clientProperty để Refresh biết tab nào đang active
 */
public class ReportPanel extends JPanel {

    // ── Palette (đồng bộ UIStyle) ─────────────────────────────────────────────
    private static final Color BG_DARK      = UIStyle.BG_MAIN;
    private static final Color BG_CARD      = UIStyle.BG_CARD;
    private static final Color ACCENT       = UIStyle.ACCENT;
    private static final Color ACCENT_LIGHT = UIStyle.ACCENT_LIGHT;
    private static final Color TEXT_WHITE   = UIStyle.TEXT_PRIMARY;
    private static final Color TEXT_MUTED   = UIStyle.TEXT_MUTED;
    private static final Color BORDER_CARD  = UIStyle.BORDER_CARD;

    private static final Color COLOR_REVENUE  = new Color(120,  80, 220);
    private static final Color COLOR_RENT     = new Color( 56, 189, 248);
    private static final Color COLOR_OVERDUE  = new Color(251, 113, 133);
    private static final Color COLOR_CUSTOMER = new Color( 52, 211, 153);
    private static final Color COLOR_ROM      = new Color(251, 191,  36);

    private static final Color TBL_HEADER = new Color(155, 135, 245);
    private static final Color TBL_ROW_A  = new Color( 28,  24,  64);
    private static final Color TBL_ROW_B  = new Color( 22,  19,  54);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_HEADING = UIStyle.FONT_HEADING;
    private static final Font FONT_BODY    = UIStyle.FONT_BODY;
    private static final Font FONT_H       = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_CELL    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_KPI_NUM = new Font("Segoe UI", Font.BOLD,  26);
    private static final Font FONT_KPI_LBL = new Font("Segoe UI", Font.PLAIN, 12);

    // ── State ─────────────────────────────────────────────────────────────────
    private JButton    activeTab;
    private JPanel     contentStack;
    private CardLayout cardLayout;

    // ═════════════════════════════════════════════════════════════════════════
    public ReportPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(22, 22, 22, 22));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HEADER + TAB BAR
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG_DARK);
        wrap.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Thống kê & Báo cáo");
        title.setFont(FONT_HEADING);
        title.setForeground(TEXT_WHITE);

        JPanel sep = UIStyle.accentSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));

        JPanel titleWrap = new JPanel();
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));
        titleWrap.setBackground(BG_DARK);
        titleWrap.add(title);
        titleWrap.add(Box.createVerticalStrut(6));
        titleWrap.add(sep);
        titleWrap.add(Box.createVerticalStrut(14));

        // ── Tab bar + Refresh ──
        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tabBar.setBackground(BG_DARK);

        String[] tabs = {"Tổng quan", "Doanh thu tháng", "Doanh thu năm",
                         "Top Game", "Tình trạng CD", "Quá hạn", "Khách VIP"};
        String[] keys = {"OVERVIEW", "MONTHLY", "YEARLY",
                         "TOPGAME", "CD_STATUS", "OVERDUE", "VIP"};

        for (int i = 0; i < tabs.length; i++) {
            JButton btn = makeTabButton(tabs[i], keys[i]);
            tabBar.add(btn);
            if (i == 0) { setActiveTab(btn); activeTab = btn; }
        }

        // ── Nút Refresh ──────────────────────────────────────────────────────
        tabBar.add(Box.createHorizontalStrut(12));
        JButton btnRefresh = new JButton("⟳  Làm mới") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hov = getModel().isRollover();
                g2.setColor(hov ? new Color(55, 45, 110) : new Color(38, 32, 80));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                setForeground(ACCENT_LIGHT);
                super.paintComponent(g);
            }
        };
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setOpaque(false);
        btnRefresh.setBorder(new EmptyBorder(7, 14, 7, 14));
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> doRefresh());
        tabBar.add(btnRefresh);

        titleWrap.add(tabBar);
        wrap.add(titleWrap, BorderLayout.CENTER);
        return wrap;
    }

    /** Rebuild toàn bộ contentStack, giữ nguyên tab đang active. */
    private void doRefresh() {
        String currentKey = activeTab != null
            ? (String) activeTab.getClientProperty("key")
            : "OVERVIEW";
        if (currentKey == null) currentKey = "OVERVIEW";

        contentStack.removeAll();
        contentStack.add(buildOverview(),  "OVERVIEW");
        contentStack.add(buildMonthly(),   "MONTHLY");
        contentStack.add(buildYearly(),    "YEARLY");
        contentStack.add(buildTopGame(),   "TOPGAME");
        contentStack.add(buildCDStatus(),  "CD_STATUS");
        contentStack.add(buildOverdue(),   "OVERDUE");
        contentStack.add(buildVIP(),       "VIP");
        cardLayout.show(contentStack, currentKey);
        contentStack.revalidate();
        contentStack.repaint();
    }

    private JButton makeTabButton(String label, String key) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                g2.setColor(active ? ACCENT : new Color(38, 32, 80));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                setForeground(active ? Color.WHITE : TEXT_MUTED);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("key", key); // ✅ lưu key để Refresh dùng
        btn.addActionListener(e -> {
            setActiveTab(btn);
            cardLayout.show(contentStack, key);
        });
        return btn;
    }

    private void setActiveTab(JButton btn) {
        if (activeTab != null) { activeTab.putClientProperty("active", false); activeTab.repaint(); }
        activeTab = btn;
        btn.putClientProperty("active", true);
        btn.repaint();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CONTENT STACK
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildContent() {
        cardLayout   = new CardLayout();
        contentStack = new JPanel(cardLayout);
        contentStack.setBackground(BG_DARK);
        contentStack.add(buildOverview(),  "OVERVIEW");
        contentStack.add(buildMonthly(),   "MONTHLY");
        contentStack.add(buildYearly(),    "YEARLY");
        contentStack.add(buildTopGame(),   "TOPGAME");
        contentStack.add(buildCDStatus(),  "CD_STATUS");
        contentStack.add(buildOverdue(),   "OVERDUE");
        contentStack.add(buildVIP(),       "VIP");
        cardLayout.show(contentStack, "OVERVIEW");
        return contentStack;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 1 — TỔNG QUAN
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildOverview() {
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(BG_DARK);

        // Doanh thu bán: TongTien đã trừ TienGiam → tiền thực thu ✅
        double doanhThuThang = queryDouble(
            "SELECT ISNULL(SUM(TongTien),0) FROM HOADON " +
            "WHERE MONTH(NgayLap)=MONTH(GETDATE()) AND YEAR(NgayLap)=YEAR(GETDATE()) " +
            "AND TrangThai='DaThanhToan'");

        // Thu thuê: chỉ tính phiếu DaTra — đã hoàn thành ✅ (FIX: bỏ phiếu DangThue)
        double tienThueThang = queryDouble(
            "SELECT ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) " +
            "FROM PHIEUTHUE pt " +
            "JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
            "WHERE MONTH(pt.NgayThue)=MONTH(GETDATE()) AND YEAR(pt.NgayThue)=YEAR(GETDATE()) " +
            "AND pt.TrangThai='DaTra'");

        int soPhieuQuaHan = queryInt(
            "SELECT COUNT(*) FROM PHIEUTHUE WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");

        int soKhachHoatDong = queryInt(
            "SELECT COUNT(DISTINCT MaKH) FROM HOADON " +
            "WHERE MONTH(NgayLap)=MONTH(GETDATE()) AND YEAR(NgayLap)=YEAR(GETDATE()) " +
            "AND MaKH IS NOT NULL");

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setBackground(BG_DARK);
        kpiRow.add(makeKPICard("Doanh thu tháng",  formatMoney(doanhThuThang), "Bán hàng (đã TT)",       COLOR_REVENUE,  "▲"));
        kpiRow.add(makeKPICard("Thu thuê tháng",   formatMoney(tienThueThang), "Phiếu thuê hoàn thành",  COLOR_RENT,     "▲"));
        kpiRow.add(makeKPICard("Phiếu quá hạn",   String.valueOf(soPhieuQuaHan),   "Cần xử lý ngay",    COLOR_OVERDUE,  "⚠"));
        kpiRow.add(makeKPICard("Khách hoạt động",  String.valueOf(soKhachHoatDong), "Có GD tháng này",  COLOR_CUSTOMER, "♦"));

        JPanel chartCard = makeCard();
        chartCard.setLayout(new BorderLayout(0, 10));
        chartCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel chartTitle = new JLabel("Doanh thu 7 ngày gần nhất");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chartTitle.setForeground(TEXT_WHITE);

        MiniBarChart chart = buildWeeklyChart();
        chart.setPreferredSize(new Dimension(0, 200));

        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(chart,      BorderLayout.CENTER);

        root.add(kpiRow,    BorderLayout.NORTH);
        root.add(chartCard, BorderLayout.CENTER);
        return scrollWrap(root);
    }

    private MiniBarChart buildWeeklyChart() {
        double[] values = new double[7];
        String[] labels = new String[7];
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            labels[6 - i] = d.format(fmt);
            values[6 - i] = queryDouble(
                "SELECT ISNULL(SUM(TongTien),0) FROM HOADON " +
                "WHERE CAST(NgayLap AS DATE)='" + d + "' AND TrangThai='DaThanhToan'");
        }
        return new MiniBarChart(values, labels, COLOR_REVENUE);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 2 — DOANH THU THÁNG
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildMonthly() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG_DARK);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterBar.setBackground(BG_DARK);

        int nowMonth = LocalDate.now().getMonthValue();
        int nowYear  = LocalDate.now().getYear();

        JComboBox<Integer> cboMonth = makeCombo(new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12});
        cboMonth.setSelectedItem(nowMonth);

        List<Integer> yearsM = getYearsWithData("HOADON", "NgayLap");
        JComboBox<Integer> cboYear = makeCombo(yearsM.toArray(new Integer[0]));
        if (yearsM.contains(nowYear)) cboYear.setSelectedItem(nowYear);

        filterBar.add(makeFilterLabel("Tháng:")); filterBar.add(cboMonth);
        filterBar.add(Box.createHorizontalStrut(8));
        filterBar.add(makeFilterLabel("Năm:"));   filterBar.add(cboYear);

        JLabel chartTitle = new JLabel();
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chartTitle.setForeground(TEXT_WHITE);
        MonthlyBarChart chartPanel = new MonthlyBarChart(nowMonth, nowYear);
        chartPanel.setPreferredSize(new Dimension(0, 220));
        JPanel chartCard = makeCard();
        chartCard.setLayout(new BorderLayout(0, 8));
        chartCard.setBorder(new EmptyBorder(14, 18, 14, 18));
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(chartPanel, BorderLayout.CENTER);

        String[] cols = {"Ngày", "Số HĐ", "Doanh thu bán", "Thu thuê (hoàn thành)", "Tổng ngày"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = makeStyledTable(model);
        JScrollPane sp = styledScrollPane(table);

        Runnable load = () -> {
            int m = (Integer) cboMonth.getSelectedItem();
            int y = cboYear.getSelectedItem() != null ? (Integer) cboYear.getSelectedItem() : nowYear;
            chartTitle.setText("Doanh thu từng ngày — Tháng " + m + "/" + y);
            chartPanel.setMonthYear(m, y);
            chartPanel.repaint();
            model.setRowCount(0);
            try (Connection con = DBConnection.getConnection()) {
                // Bán — TongTien đã trừ giảm giá ✅
                PreparedStatement ps = con.prepareStatement(
                    "SELECT DAY(NgayLap) as D, COUNT(*) as SoHD, ISNULL(SUM(TongTien),0) as DT " +
                    "FROM HOADON WHERE MONTH(NgayLap)=? AND YEAR(NgayLap)=? AND TrangThai='DaThanhToan' " +
                    "GROUP BY DAY(NgayLap)");
                ps.setInt(1, m); ps.setInt(2, y);
                ResultSet rs = ps.executeQuery();
                Map<Integer, double[]> dayMap = new TreeMap<>();
                while (rs.next()) dayMap.put(rs.getInt("D"), new double[]{rs.getInt("SoHD"), rs.getDouble("DT")});

                // Thuê — chỉ DaTra ✅ (FIX)
                PreparedStatement ps2 = con.prepareStatement(
                    "SELECT DAY(pt.NgayThue) as D, " +
                    "ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as TT " +
                    "FROM PHIEUTHUE pt " +
                    "JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
                    "WHERE MONTH(pt.NgayThue)=? AND YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' " +
                    "GROUP BY DAY(pt.NgayThue)");
                ps2.setInt(1, m); ps2.setInt(2, y);
                ResultSet rs2 = ps2.executeQuery();
                Map<Integer, Double> rentMap = new HashMap<>();
                while (rs2.next()) rentMap.put(rs2.getInt("D"), rs2.getDouble("TT"));

                for (int d = 1; d <= 31; d++) {
                    double ban  = dayMap.containsKey(d) ? dayMap.get(d)[1] : 0;
                    double thue = rentMap.getOrDefault(d, 0.0);
                    if (ban == 0 && thue == 0) continue;
                    int soHD = dayMap.containsKey(d) ? (int) dayMap.get(d)[0] : 0;
                    model.addRow(new Object[]{
                        String.format("%02d/%02d/%04d", d, m, y),
                        soHD, formatMoney(ban), formatMoney(thue), formatMoney(ban + thue)
                    });
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        };

        cboMonth.addActionListener(e -> load.run());
        cboYear.addActionListener(e -> load.run());
        load.run();

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(BG_DARK);
        body.add(chartCard, BorderLayout.NORTH);
        body.add(sp,        BorderLayout.CENTER);

        root.add(filterBar, BorderLayout.NORTH);
        root.add(body,      BorderLayout.CENTER);
        return root;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 3 — DOANH THU NĂM
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildYearly() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG_DARK);

        List<Integer> years = getYearsWithData("HOADON", "NgayLap");
        // Gom thêm năm từ phiếu thuê đã hoàn thành ✅ (FIX)
        for (int y : getYearsWithDataFiltered("PHIEUTHUE", "NgayThue", "TrangThai='DaTra'")) {
            if (!years.contains(y)) years.add(y);
        }
        Collections.sort(years, Collections.reverseOrder());

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        filterBar.setBackground(BG_DARK);

        JComboBox<Integer> cboYear = makeCombo(years.isEmpty()
            ? new Integer[]{LocalDate.now().getYear()}
            : years.toArray(new Integer[0]));
        if (!years.isEmpty()) cboYear.setSelectedItem(years.get(0));

        JLabel lblBan  = makeSummaryBadge("Tổng bán: —",  COLOR_REVENUE);
        JLabel lblThue = makeSummaryBadge("Tổng thuê: —", COLOR_RENT);
        JLabel lblAll  = makeSummaryBadge("Tổng cộng: —", ACCENT_LIGHT);

        filterBar.add(makeFilterLabel("Năm:"));
        filterBar.add(cboYear);
        filterBar.add(Box.createHorizontalStrut(16));
        filterBar.add(lblBan);
        filterBar.add(lblThue);
        filterBar.add(lblAll);

        YearlyBarChart chartPanel = new YearlyBarChart(
            years.isEmpty() ? LocalDate.now().getYear() : years.get(0));
        chartPanel.setPreferredSize(new Dimension(0, 260));

        JPanel chartCard = makeCard();
        chartCard.setLayout(new BorderLayout(0, 10));
        chartCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel chartTitle = new JLabel();
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chartTitle.setForeground(TEXT_WHITE);
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(chartPanel, BorderLayout.CENTER);

        String[] cols = {"Tháng", "Số HĐ bán", "Doanh thu bán", "Số PT thuê", "Thu thuê (hoàn thành)", "Tổng tháng"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? TBL_ROW_A : TBL_ROW_B);
                    c.setForeground(TEXT_WHITE);
                    c.setFont(FONT_CELL);
                    String first = model.getValueAt(row, 0).toString();
                    if (first.startsWith("TỔNG")) {
                        c.setFont(new Font("Segoe UI", Font.BOLD, 13));
                        c.setForeground(ACCENT_LIGHT);
                    }
                    if (col == 5 && !first.startsWith("TỔNG")) {
                        c.setFont(new Font("Segoe UI", Font.BOLD, 13));
                        c.setForeground(ACCENT_LIGHT);
                    }
                } else {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        styleTable(table);
        JScrollPane sp = styledScrollPane(table);

        String[] tenThang = {"Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                             "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"};

        Runnable load = () -> {
            int y = cboYear.getSelectedItem() != null
                ? (Integer) cboYear.getSelectedItem()
                : LocalDate.now().getYear();
            chartTitle.setText("Doanh thu 12 tháng — Năm " + y);
            chartPanel.setYear(y);

            double[] banArr  = new double[12];
            double[] thueArr = new double[12];
            int[]    hdArr   = new int[12];
            int[]    ptArr   = new int[12];

            try (Connection con = DBConnection.getConnection()) {
                // Bán ✅
                PreparedStatement ps = con.prepareStatement(
                    "SELECT MONTH(NgayLap) as M, COUNT(*) as SoHD, ISNULL(SUM(TongTien),0) as DT " +
                    "FROM HOADON WHERE YEAR(NgayLap)=? AND TrangThai='DaThanhToan' GROUP BY MONTH(NgayLap)");
                ps.setInt(1, y);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int m = rs.getInt("M") - 1;
                    banArr[m] = rs.getDouble("DT");
                    hdArr[m]  = rs.getInt("SoHD");
                }

                // Thuê — chỉ DaTra ✅ (FIX)
                PreparedStatement ps2 = con.prepareStatement(
                    "SELECT MONTH(pt.NgayThue) as M, COUNT(DISTINCT pt.MaPT) as SoPT, " +
                    "ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as TT " +
                    "FROM PHIEUTHUE pt " +
                    "JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
                    "WHERE YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' " +
                    "GROUP BY MONTH(pt.NgayThue)");
                ps2.setInt(1, y);
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    int m = rs2.getInt("M") - 1;
                    thueArr[m] = rs2.getDouble("TT");
                    ptArr[m]   = rs2.getInt("SoPT");
                }
            } catch (Exception ex) { ex.printStackTrace(); }

            chartPanel.setData(banArr, thueArr);
            chartPanel.repaint();

            model.setRowCount(0);
            double tongBan = 0, tongThue = 0;
            for (int i = 0; i < 12; i++) {
                if (banArr[i] == 0 && thueArr[i] == 0) continue;
                model.addRow(new Object[]{
                    tenThang[i], hdArr[i], formatMoney(banArr[i]),
                    ptArr[i],    formatMoney(thueArr[i]),
                    formatMoney(banArr[i] + thueArr[i])
                });
                tongBan  += banArr[i];
                tongThue += thueArr[i];
            }
            model.addRow(new Object[]{
                "TỔNG NĂM " + y, "", formatMoney(tongBan),
                "", formatMoney(tongThue), formatMoney(tongBan + tongThue)
            });

            lblBan.setText("  Bán: "   + formatMoney(tongBan)  + "  ");
            lblThue.setText("  Thuê: " + formatMoney(tongThue) + "  ");
            lblAll.setText("  Tổng: "  + formatMoney(tongBan + tongThue) + "  ");
        };

        cboYear.addActionListener(e -> load.run());
        if (!years.isEmpty()) load.run();

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(BG_DARK);
        body.add(chartCard, BorderLayout.NORTH);
        body.add(sp,        BorderLayout.CENTER);

        root.add(filterBar, BorderLayout.NORTH);
        root.add(body,      BorderLayout.CENTER);
        return root;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 4 — TOP GAME
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildTopGame() {
        JPanel root = new JPanel(new GridLayout(1, 2, 16, 0));
        root.setBackground(BG_DARK);

        // Top bán ✅
        String[] colsBan = {"#", "Tên Game", "Thể loại", "Lượt bán", "Doanh thu"};
        DefaultTableModel mBan = new DefaultTableModel(colsBan, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT TOP 15 g.TenGame, g.TheLoai, " +
                "ISNULL(SUM(ct.SoLuong),0) as LuotBan, ISNULL(SUM(ct.SoLuong*ct.DonGia),0) as DT " +
                "FROM GAME g JOIN SANPHAM sp ON g.MaGame=sp.MaGame " +
                "JOIN CTHOADON ct ON sp.MaSP=ct.MaSP " +
                "JOIN HOADON h ON ct.MaHD=h.MaHD AND h.TrangThai='DaThanhToan' " +
                "GROUP BY g.TenGame, g.TheLoai ORDER BY DT DESC");
            int rank = 1;
            while (rs.next()) mBan.addRow(new Object[]{
                rank++, rs.getString("TenGame"), rs.getString("TheLoai"),
                rs.getInt("LuotBan"), formatMoney(rs.getDouble("DT"))});
        } catch (Exception ex) { ex.printStackTrace(); }

        // Top thuê ✅ (thống kê lượt, không lọc trạng thái — hợp lý)
        String[] colsThue = {"#", "Tên Game", "Thể loại", "Lượt thuê"};
        DefaultTableModel mThue = new DefaultTableModel(colsThue, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT TOP 15 g.TenGame, g.TheLoai, COUNT(ctp.MaCD) as LuotThue " +
                "FROM GAME g JOIN SANPHAM sp ON g.MaGame=sp.MaGame " +
                "JOIN CD c ON sp.MaSP=c.MaSP " +
                "JOIN CTPHIEUTHUE ctp ON c.MaCD=ctp.MaCD " +
                "GROUP BY g.TenGame, g.TheLoai ORDER BY LuotThue DESC");
            int rank = 1;
            while (rs.next()) mThue.addRow(new Object[]{
                rank++, rs.getString("TenGame"), rs.getString("TheLoai"), rs.getInt("LuotThue")});
        } catch (Exception ex) { ex.printStackTrace(); }

        JPanel leftCard = makeCard();
        leftCard.setLayout(new BorderLayout(0, 10));
        leftCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel t1 = new JLabel("🏆  Top Game Bán Chạy");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 14)); t1.setForeground(COLOR_REVENUE);
        leftCard.add(t1, BorderLayout.NORTH);
        leftCard.add(styledScrollPane(makeRankTable(mBan, COLOR_REVENUE)), BorderLayout.CENTER);

        JPanel rightCard = makeCard();
        rightCard.setLayout(new BorderLayout(0, 10));
        rightCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel t2 = new JLabel("🎮  Top Game Cho Thuê Nhiều");
        t2.setFont(new Font("Segoe UI", Font.BOLD, 14)); t2.setForeground(COLOR_RENT);
        rightCard.add(t2, BorderLayout.NORTH);
        rightCard.add(styledScrollPane(makeRankTable(mThue, COLOR_RENT)), BorderLayout.CENTER);

        root.add(leftCard);
        root.add(rightCard);
        return root;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 5 — TÌNH TRẠNG CD
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildCDStatus() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG_DARK);

        int sanSang  = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='SanSang'");
        int dangThue = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='DangThue'");
        int daBan    = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='DaBan'");
        int hong     = queryInt("SELECT COUNT(*) FROM CD WHERE TrangThai='Hong'");

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow.setBackground(BG_DARK);
        kpiRow.add(makeKPICard("Tổng CD",     String.valueOf(sanSang+dangThue+daBan+hong), "Toàn bộ đĩa",    ACCENT,         "📀"));
        kpiRow.add(makeKPICard("Sẵn sàng",   String.valueOf(sanSang),                      "Có thể cho thuê", COLOR_CUSTOMER, "✔"));
        kpiRow.add(makeKPICard("Đang thuê",  String.valueOf(dangThue),                    "Khách đang giữ",   COLOR_RENT,     "↗"));
        kpiRow.add(makeKPICard("Đã bán / Hỏng", String.valueOf(daBan + hong),             "Không còn dùng",  COLOR_OVERDUE,  "✖"));

        String[] cols = {"Mã CD", "Game", "Thể loại", "Tình trạng", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT c.MaCD, g.TenGame, g.TheLoai, c.TinhTrang, c.TrangThai " +
                "FROM CD c JOIN SANPHAM sp ON c.MaSP=sp.MaSP " +
                "JOIN GAME g ON sp.MaGame=g.MaGame ORDER BY c.TrangThai, g.TenGame");
            while (rs.next()) model.addRow(new Object[]{
                "CD-" + String.format("%04d", rs.getInt("MaCD")),
                rs.getString("TenGame"), rs.getString("TheLoai"),
                rs.getString("TinhTrang"), rs.getString("TrangThai")});
        } catch (Exception ex) { ex.printStackTrace(); }

        JTable table = makeStyledTable(model);
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String val = v == null ? "" : v.toString();
                setBackground(row % 2 == 0 ? TBL_ROW_A : TBL_ROW_B);
                Color fg;
                switch (val) {
                    case "SanSang":  fg = COLOR_CUSTOMER; break;
                    case "DangThue": fg = COLOR_RENT;     break;
                    case "DaBan":    fg = TEXT_MUTED;     break;
                    case "Hong":     fg = COLOR_OVERDUE;  break;
                    default:         fg = TEXT_MUTED;     break;
                }
                setForeground(fg);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (sel) { setBackground(ACCENT); setForeground(Color.WHITE); }
                return this;
            }
        });

        root.add(kpiRow,                   BorderLayout.NORTH);
        root.add(styledScrollPane(table),  BorderLayout.CENTER);
        return root;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 6 — QUÁ HẠN
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildOverdue() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG_DARK);

        int    tongQH   = queryInt("SELECT COUNT(*) FROM PHIEUTHUE WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");
        double tongPhat  = queryDouble("SELECT ISNULL(SUM(TienPhat),0) FROM PHIEUTHUE WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");
        int    maxNgay   = queryInt("SELECT ISNULL(MAX(DATEDIFF(day,NgayTraDuKien,GETDATE())),0) FROM PHIEUTHUE WHERE TrangThai='DangThue' AND NgayTraDuKien < GETDATE()");

        JPanel kpiRow = new JPanel(new GridLayout(1, 3, 16, 0));
        kpiRow.setBackground(BG_DARK);
        kpiRow.add(makeKPICard("Phiếu quá hạn",     String.valueOf(tongQH),  "Cần liên hệ khách",  COLOR_OVERDUE, "⚠"));
        kpiRow.add(makeKPICard("Tiền phạt dự kiến", formatMoney(tongPhat),  "Chưa thu",             COLOR_ROM,     "₫"));
        kpiRow.add(makeKPICard("Quá hạn lâu nhất",  maxNgay + " ngày",     "Phiếu lâu nhất",       COLOR_OVERDUE, "⏰"));

        String[] cols = {"Mã PT", "Khách hàng", "SĐT", "Ngày thuê", "Hạn trả", "Quá hạn (ngày)", "Tiền phạt"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT pt.MaPT, kh.HoTen, kh.SDT, pt.NgayThue, pt.NgayTraDuKien, " +
                "DATEDIFF(day,pt.NgayTraDuKien,GETDATE()) as SoNgay, ISNULL(pt.TienPhat,0) as Phat " +
                "FROM PHIEUTHUE pt JOIN KHACHHANG kh ON pt.MaKH=kh.MaKH " +
                "WHERE pt.TrangThai='DangThue' AND pt.NgayTraDuKien < GETDATE() ORDER BY SoNgay DESC");
            ResultSet rs = ps.executeQuery();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            while (rs.next()) {
                model.addRow(new Object[]{
                    "PT-" + String.format("%04d", rs.getInt("MaPT")),
                    rs.getString("HoTen"), rs.getString("SDT"),
                    rs.getTimestamp("NgayThue") != null
                        ? rs.getTimestamp("NgayThue").toLocalDateTime().format(fmt) : "",
                    rs.getTimestamp("NgayTraDuKien") != null
                        ? rs.getTimestamp("NgayTraDuKien").toLocalDateTime().format(fmt) : "",
                    rs.getInt("SoNgay"),
                    formatMoney(rs.getDouble("Phat"))
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        JTable table = makeStyledTable(model);
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(row % 2 == 0 ? TBL_ROW_A : TBL_ROW_B);
                setForeground(COLOR_OVERDUE);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setHorizontalAlignment(CENTER);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (sel) { setBackground(ACCENT); setForeground(Color.WHITE); }
                return this;
            }
        });

        root.add(kpiRow,                  BorderLayout.NORTH);
        root.add(styledScrollPane(table), BorderLayout.CENTER);
        return root;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 7 — KHÁCH VIP
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildVIP() {
        String[] cols = {"#", "Khách hàng", "SĐT", "Email", "Điểm tích lũy", "Tổng mua", "Tổng thuê (hoàn thành)", "Tổng cộng"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        try (Connection con = DBConnection.getConnection()) {
            // TongMua: TongTien đã trừ giảm giá ✅
            // TongThue: chỉ DaTra ✅ (FIX)
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT kh.HoTen, kh.SDT, kh.Email, kh.DiemTichLuy, " +
                "ISNULL(b.TongMua,0) as TongMua, ISNULL(r.TongThue,0) as TongThue " +
                "FROM KHACHHANG kh " +
                "LEFT JOIN (SELECT MaKH, SUM(TongTien) as TongMua FROM HOADON " +
                "           WHERE TrangThai='DaThanhToan' GROUP BY MaKH) b ON kh.MaKH=b.MaKH " +
                "LEFT JOIN (SELECT pt.MaKH, " +
                "           ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as TongThue " +
                "           FROM PHIEUTHUE pt " +
                "           JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
                "           WHERE pt.TrangThai='DaTra' GROUP BY pt.MaKH) r ON kh.MaKH=r.MaKH " +
                "WHERE kh.HoTen IS NOT NULL " +
                "ORDER BY (ISNULL(b.TongMua,0)+ISNULL(r.TongThue,0)) DESC");
            int rank = 1;
            while (rs.next()) {
                double mua = rs.getDouble("TongMua"), thue = rs.getDouble("TongThue");
                if (mua == 0 && thue == 0) continue; // bỏ qua khách chưa có GD
                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : String.valueOf(rank);
                model.addRow(new Object[]{medal, rs.getString("HoTen"), rs.getString("SDT"),
                    rs.getString("Email"), rs.getInt("DiemTichLuy"),
                    formatMoney(mua), formatMoney(thue), formatMoney(mua + thue)});
                rank++;
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? TBL_ROW_A : TBL_ROW_B);
                    Color fg;
                    switch (row) {
                        case 0: fg = new Color(255, 215,   0); break;
                        case 1: fg = new Color(192, 192, 192); break;
                        case 2: fg = new Color(205, 127,  50); break;
                        default: fg = TEXT_WHITE;               break;
                    }
                    c.setForeground(fg);
                    c.setFont(FONT_CELL);
                    if (col == 7) {
                        c.setFont(new Font("Segoe UI", Font.BOLD, 13));
                        if (!isRowSelected(row)) c.setForeground(ACCENT_LIGHT);
                    }
                } else {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        styleTable(table);

        JPanel card = makeCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel title = new JLabel("👑  Bảng xếp hạng khách hàng — Tổng chi tiêu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(255, 215, 0));
        card.add(title, BorderLayout.NORTH);
        card.add(styledScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // INNER: MiniBarChart
    // ═════════════════════════════════════════════════════════════════════════
    static class MiniBarChart extends JPanel {
        double[] values; String[] labels; Color barColor;
        MiniBarChart(double[] v, String[] l, Color c) { values=v; labels=l; barColor=c; setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (values == null || values.length == 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight(), n=values.length;
            int padL=62, padR=10, padT=18, padB=36;
            int cW=w-padL-padR, cH=h-padT-padB;
            double max = Arrays.stream(values).max().orElse(1); if (max==0) max=1;
            int barW = Math.max(4, cW/n - 10);
            for (int i=0;i<=4;i++) {
                int y=padT+cH-(int)(cH*i/4.0);
                g2.setColor(new Color(255,255,255,20)); g2.drawLine(padL,y,w-padR,y);
                g2.setColor(TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
                g2.drawString(moneyShort(max*i/4), 2, y+4);
            }
            for (int i=0;i<n;i++) {
                int x=padL+i*(cW/n)+(cW/n-barW)/2;
                int bh=(int)(cH*values[i]/max); if (bh<2&&values[i]>0) bh=2;
                int y=padT+cH-bh;
                g2.setPaint(new GradientPaint(x,y,barColor.brighter(),x,padT+cH,barColor.darker()));
                g2.fill(new RoundRectangle2D.Float(x,y,barW,bh,5,5));
                if (values[i]>0) {
                    String val=moneyShort(values[i]);
                    g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,9));
                    FontMetrics fm=g2.getFontMetrics();
                    g2.drawString(val, x+(barW-fm.stringWidth(val))/2, y-3);
                }
                if (labels!=null&&i<labels.length) {
                    g2.setColor(TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                    FontMetrics fm=g2.getFontMetrics();
                    g2.drawString(labels[i], x+(barW-fm.stringWidth(labels[i]))/2, h-padB+14);
                }
            }
            g2.dispose();
        }
        static String moneyShort(double v) {
            if (v>=1_000_000_000) return String.format("%.1fB",v/1_000_000_000);
            if (v>=1_000_000)     return String.format("%.0fM",v/1_000_000);
            if (v>=1_000)         return String.format("%.0fK",v/1_000);
            return String.format("%.0f",v);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // INNER: MonthlyBarChart
    // ═════════════════════════════════════════════════════════════════════════
    class MonthlyBarChart extends JPanel {
        private int month, year;
        MonthlyBarChart(int m, int y) { month=m; year=y; setOpaque(false); }
        void setMonthYear(int m, int y) { month=m; year=y; }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            double[] ban=new double[31], thue=new double[31];
            try (Connection con=DBConnection.getConnection()) {
                PreparedStatement ps=con.prepareStatement(
                    "SELECT DAY(NgayLap) as D, ISNULL(SUM(TongTien),0) as V FROM HOADON " +
                    "WHERE MONTH(NgayLap)=? AND YEAR(NgayLap)=? AND TrangThai='DaThanhToan' GROUP BY DAY(NgayLap)");
                ps.setInt(1,month); ps.setInt(2,year);
                ResultSet rs=ps.executeQuery();
                while(rs.next()) ban[rs.getInt("D")-1]=rs.getDouble("V");

                // FIX: chỉ DaTra ✅
                PreparedStatement ps2 = con.prepareStatement(
                    "SELECT DAY(pt.NgayThue) as D, " +
                    "ISNULL(SUM(ct.DonGiaThue),0) + ISNULL(SUM(pt.TienPhat),0) as V " +
                    "FROM PHIEUTHUE pt " +
                    "JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
                    "WHERE MONTH(pt.NgayThue)=? AND YEAR(pt.NgayThue)=? AND pt.TrangThai='DaTra' " +
                    "GROUP BY DAY(pt.NgayThue)");
                ps2.setInt(1, month); ps2.setInt(2, year);
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) thue[rs2.getInt("D") - 1] = rs2.getDouble("V");
            } catch(Exception ex) { ex.printStackTrace(); }

            int days = java.time.YearMonth.of(year,month).lengthOfMonth();
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            drawGrouped(g2, ban, thue, days,
                buildDayLabels(days), new Color[]{COLOR_REVENUE,COLOR_RENT}, new String[]{"Bán","Thuê (hoàn thành)"});
            g2.dispose();
        }
        private String[] buildDayLabels(int days) {
            String[] l = new String[days];
            for (int i=0;i<days;i++) l[i]=String.valueOf(i+1);
            return l;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // INNER: YearlyBarChart
    // ═════════════════════════════════════════════════════════════════════════
    class YearlyBarChart extends JPanel {
        private int year;
        private double[] banData=new double[12], thueData=new double[12];
        YearlyBarChart(int y) { year=y; setOpaque(false); }
        void setYear(int y)   { year=y; }
        void setData(double[] b, double[] t) { banData=b; thueData=t; }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            String[] lbl={"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};
            drawGrouped(g2, banData, thueData, 12, lbl,
                new Color[]{COLOR_REVENUE,COLOR_RENT}, new String[]{"Bán","Thuê (hoàn thành)"});
            g2.dispose();
        }
    }

    // ── Shared grouped bar renderer ───────────────────────────────────────────
    private void drawGrouped(Graphics2D g2, double[] a, double[] b, int n,
                              String[] labels, Color[] colors, String[] seriesNames) {
        int w=getWidth(), h=getHeight();
        int padL=64, padR=14, padT=20, padB=46;
        int cW=w-padL-padR, cH=h-padT-padB;
        if (cW<=0||cH<=0) return;
        double max=1;
        for (int i=0;i<n;i++) max=Math.max(max,Math.max(a[i],b[i]));

        int groupW=cW/n;
        int barW=Math.max(3, groupW/3);

        for (int i=0;i<=4;i++) {
            int y=padT+cH-(int)(cH*i/4.0);
            g2.setColor(new Color(255,255,255,18)); g2.drawLine(padL,y,w-padR,y);
            g2.setColor(TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
            g2.drawString(MiniBarChart.moneyShort(max*i/4), 2, y+4);
        }

        for (int i=0;i<n;i++) {
            double[] vals={a[i],b[i]};
            int gx=padL+i*groupW;
            for (int s=0;s<2;s++) {
                int bx=gx+s*(barW+2)+(groupW-2*barW-2)/2;
                int bh=(int)(cH*vals[s]/max);
                if (bh<2&&vals[s]>0) bh=2;
                int by=padT+cH-bh;
                g2.setPaint(new GradientPaint(bx,by,colors[s].brighter(),bx,padT+cH,colors[s].darker()));
                g2.fill(new RoundRectangle2D.Float(bx,by,barW,bh,4,4));
            }
            if (labels!=null&&i<labels.length) {
                g2.setColor(TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(labels[i], gx+(groupW-fm.stringWidth(labels[i]))/2, h-padB+14);
            }
        }

        int lx=padL;
        for (int s=0;s<seriesNames.length;s++) {
            g2.setColor(colors[s]);
            g2.fillRoundRect(lx, h-padB+24, 10, 10, 3, 3);
            g2.setColor(TEXT_MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
            g2.drawString(seriesNames[s], lx+14, h-padB+33);
            lx+=100;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS — UI
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel makeCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) { UIStyle.paintCard(g, this); }
        };
        card.setOpaque(false);
        return card;
    }

    private JPanel makeKPICard(String label, String value, String sub, Color accent, String icon) {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,accent,0,getHeight(),accent.darker()));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),4,4));
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(4, 0)); bar.setOpaque(false);

        JLabel lblVal  = new JLabel(icon + "  " + value);
        lblVal.setFont(FONT_KPI_NUM); lblVal.setForeground(accent);
        JLabel lblLbl  = new JLabel(label);
        lblLbl.setFont(new Font("Segoe UI",Font.BOLD,13)); lblLbl.setForeground(TEXT_WHITE);
        JLabel lblSub  = new JLabel(sub);
        lblSub.setFont(FONT_KPI_LBL); lblSub.setForeground(TEXT_MUTED);

        JPanel text = new JPanel(); text.setLayout(new BoxLayout(text,BoxLayout.Y_AXIS)); text.setOpaque(false);
        text.add(lblLbl); text.add(Box.createVerticalStrut(6));
        text.add(lblVal); text.add(Box.createVerticalStrut(4)); text.add(lblSub);

        card.add(bar,  BorderLayout.WEST);
        card.add(Box.createHorizontalStrut(14), BorderLayout.BEFORE_LINE_BEGINS);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JTable makeStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        styleTable(table);
        return table;
    }

    private JTable makeRankTable(DefaultTableModel model, Color accentCol) {
        JTable table = makeStyledTable(model);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t,Object v,boolean sel,boolean foc,int row,int col) {
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                setBackground(row%2==0?TBL_ROW_A:TBL_ROW_B);
                setForeground(accentCol); setFont(new Font("Segoe UI",Font.BOLD,14));
                setHorizontalAlignment(CENTER); setBorder(new EmptyBorder(0,4,0,4));
                if(sel){setBackground(ACCENT);setForeground(Color.WHITE);}
                return this;
            }
        });
        return table;
    }

    private void styleTable(JTable table) {
        table.setFont(FONT_CELL); table.setRowHeight(38);
        table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0,0));
        table.setSelectionBackground(ACCENT); table.setSelectionForeground(Color.WHITE);
        table.setBackground(TBL_ROW_B); table.setForeground(TEXT_WHITE);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t,Object v,boolean sel,boolean foc,int r,int c) {
                JLabel lbl=new JLabel(v==null?"":v.toString());
                lbl.setFont(FONT_H); lbl.setForeground(Color.WHITE);
                lbl.setBackground(TBL_HEADER); lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(10,12,10,12)); return lbl;
            }
        });
        header.setBackground(TBL_HEADER);
        header.setPreferredSize(new Dimension(0,42));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t,Object v,boolean sel,boolean foc,int row,int col) {
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                setBackground(row%2==0?TBL_ROW_A:TBL_ROW_B);
                setForeground(TEXT_WHITE); setFont(FONT_CELL);
                setBorder(new EmptyBorder(0,12,0,12));
                if(sel){setBackground(ACCENT);setForeground(Color.WHITE);}
                return this;
            }
        });
    }

    private JScrollPane styledScrollPane(JTable table) {
        JScrollPane sp=new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_CARD,1,true));
        sp.setBackground(TBL_ROW_B); sp.getViewport().setBackground(TBL_ROW_B);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel scrollWrap(JPanel p) {
        JScrollPane sp=new JScrollPane(p);
        sp.setBorder(null); sp.getViewport().setBackground(BG_DARK); sp.setBackground(BG_DARK);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(BG_DARK); wrap.add(sp); return wrap;
    }

    private <T> JComboBox<T> makeCombo(T[] items) {
        JComboBox<T> cb=new JComboBox<>(items);
        cb.setBackground(new Color(38,32,80)); cb.setForeground(TEXT_WHITE);
        cb.setFont(FONT_BODY); cb.setFocusable(false);
        cb.setPreferredSize(new Dimension(110,34)); return cb;
    }

    private JLabel makeFilterLabel(String text) {
        JLabel lbl=new JLabel(text);
        lbl.setFont(new Font("Segoe UI",Font.BOLD,12)); lbl.setForeground(TEXT_MUTED); return lbl;
    }

    private JLabel makeSummaryBadge(String text, Color color) {
        JLabel lbl=new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),30));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                g2.setColor(color); g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,8,8));
                g2.dispose(); super.paintComponent(g);
            }
        };
        lbl.setForeground(color); lbl.setFont(new Font("Segoe UI",Font.BOLD,12));
        lbl.setBorder(new EmptyBorder(5,10,5,10)); lbl.setOpaque(false); return lbl;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS — DB
    // ═════════════════════════════════════════════════════════════════════════
    private double queryDouble(String sql) {
        try (Connection con=DBConnection.getConnection();
             ResultSet rs=con.createStatement().executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }

    private int queryInt(String sql) {
        try (Connection con=DBConnection.getConnection();
             ResultSet rs=con.createStatement().executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }

    /** Lấy danh sách năm có dữ liệu từ bảng bất kỳ. */
    private List<Integer> getYearsWithData(String table, String dateCol) {
        List<Integer> years=new ArrayList<>();
        String sql="SELECT DISTINCT YEAR("+dateCol+") as Y FROM "+table+
                   " WHERE "+dateCol+" IS NOT NULL ORDER BY Y DESC";
        try (Connection con=DBConnection.getConnection();
             ResultSet rs=con.createStatement().executeQuery(sql)) {
            while (rs.next()) years.add(rs.getInt("Y"));
        } catch (Exception ex) { ex.printStackTrace(); }
        return years;
    }

    /**
     * Lấy danh sách năm có dữ liệu với điều kiện WHERE bổ sung.
     * Dùng cho PHIEUTHUE chỉ lấy năm có phiếu DaTra.
     */
    private List<Integer> getYearsWithDataFiltered(String table, String dateCol, String whereExtra) {
        List<Integer> years=new ArrayList<>();
        String sql="SELECT DISTINCT YEAR("+dateCol+") as Y FROM "+table+
                   " WHERE "+dateCol+" IS NOT NULL AND "+whereExtra+" ORDER BY Y DESC";
        try (Connection con=DBConnection.getConnection();
             ResultSet rs=con.createStatement().executeQuery(sql)) {
            while (rs.next()) years.add(rs.getInt("Y"));
        } catch (Exception ex) { ex.printStackTrace(); }
        return years;
    }

    private static String formatMoney(double v) { return String.format("%,.0f đ", v); }
}
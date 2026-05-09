package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.model.HoaDon;
import otkhongluong.gamestoremanagement.service.HoaDonService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;

public class BillDetailDialog extends JDialog {

    /* ── Colors ── */
    private static final Color BG_DARK       = new Color(35, 20, 85);
    private static final Color BG_CARD       = new Color(55, 35, 110);
    private static final Color PURPLE_HEADER = new Color(155, 135, 245);
    private static final Color PURPLE_ROW    = new Color(245, 242, 255);
    private static final Color PURPLE_ALT    = Color.WHITE;
    private static final Color ACCENT        = new Color(130, 90, 230);
    private static final Color TEXT_WHITE    = Color.WHITE;
    private static final Color TEXT_MUTED    = new Color(160, 148, 200);
    private static final Color BTN_CANCEL    = new Color(252, 129, 129);
    private static final Color COLOR_GREEN   = new Color(40, 167, 69);
    private static final Color COLOR_REVENUE = new Color(80, 200, 160);
    private static final Color COLOR_WARN    = new Color(255, 165, 0);

    /* ── Fonts ── */
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_CELL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_BIG    = new Font("Segoe UI", Font.BOLD, 20);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /* ── Info labels ── */
    private JLabel lblTenKH, lblSDT, lblTrangThai;
    private JLabel lblNgayLap, lblMaNV, lblSoLuongSP;
    private JLabel lblTongTien;

    /* ── Table ── */
    private JTable table;
    private DefaultTableModel tableModel;

    private final int maHD;

    // ═══════════════════════════════════════════════════════════
    public BillDetailDialog(Frame parent, int maHD) {
        super(parent, "Chi tiết hóa đơn", true);
        this.maHD = maHD;

        setMinimumSize(new Dimension(860, 580));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        getRootPane().setBorder(BorderFactory.createLineBorder(ACCENT, 2));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadData();
    }

    // ── HEADER ──────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(18, 24, 10, 24));

        JLabel title = new JLabel("CHI TIẾT HÓA ĐƠN  —  HD" + String.format("%03d", maHD));
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_WHITE);
        p.add(title, BorderLayout.WEST);

        JPanel sep = new JPanel();
        sep.setBackground(PURPLE_HEADER);
        sep.setPreferredSize(new Dimension(0, 1));
        p.add(sep, BorderLayout.SOUTH);
        return p;
    }

    // ── BODY ────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(14, 24, 0, 24));

        // Row 1: KH, SDT, TrangThai
        lblTenKH     = cardValue();
        lblSDT       = cardValue();
        lblTrangThai = cardValue();
        JPanel row1 = infoRow(
            infoCard("Khách hàng",    lblTenKH),
            infoCard("Số điện thoại", lblSDT),
            infoCard("Trạng thái",    lblTrangThai)
        );
        row1.setPreferredSize(new Dimension(0, 68));

        // Row 2: NgayLap, MaNV, SoLuong
        lblNgayLap   = cardValue();
        lblMaNV      = cardValue();
        lblSoLuongSP = cardValue();
        JPanel row2 = infoRow(
            infoCard("Ngày lập",        lblNgayLap),
            infoCard("Mã nhân viên",    lblMaNV),
            infoCard("Số lượng sản phẩm", lblSoLuongSP)
        );
        row2.setPreferredSize(new Dimension(0, 68));

        JPanel topBlock = new JPanel(new GridLayout(2, 1, 0, 8));
        topBlock.setBackground(BG_DARK);
        topBlock.add(row1);
        topBlock.add(row2);
        p.add(topBlock, BorderLayout.NORTH);

        // Table chi tiết
        p.add(buildTable(), BorderLayout.CENTER);

        // Tổng tiền
        p.add(buildSummary(), BorderLayout.SOUTH);
        return p;
    }

    // ── TABLE ───────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = {"Tên Game", "Loại sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? PURPLE_ROW : PURPLE_ALT);
                    c.setForeground(new Color(40, 40, 40));
                }
                // căn phải cột số
                if (c instanceof JLabel && col >= 2) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                }
                return c;
            }
        };

        table.setFont(FONT_CELL);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setBackground(PURPLE_ALT);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString());
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(PURPLE_HEADER);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
                if (c >= 2) lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                return lbl;
            }
        });
        header.setBackground(PURPLE_HEADER);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = {220, 160, 80, 140, 140};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(ACCENT, 1, true));
        sp.setBackground(Color.WHITE);
        sp.getViewport().setBackground(PURPLE_ALT);
        return sp;
    }

    // ── SUMMARY ─────────────────────────────────────────────────
    private JPanel buildSummary() {
        lblTongTien = new JLabel("—");
        lblTongTien.setFont(FONT_BIG);
        lblTongTien.setForeground(COLOR_REVENUE);

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(new Color(30, 90, 70));
        card.setBorder(new CompoundBorder(
            new LineBorder(COLOR_REVENUE, 2, true),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(new Color(30, 90, 70));

        JLabel titleLbl = new JLabel("TỔNG TIỀN HÓA ĐƠN");
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(COLOR_REVENUE);
        topRow.add(titleLbl, BorderLayout.WEST);

        card.add(topRow,     BorderLayout.NORTH);
        card.add(lblTongTien, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(0, 68));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG_DARK);
        wrap.setBorder(new EmptyBorder(10, 0, 0, 0));
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    // ── FOOTER ──────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(10, 24, 16, 24));

        JPanel sep = new JPanel();
        sep.setBackground(PURPLE_HEADER);
        sep.setPreferredSize(new Dimension(0, 1));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnRow.setBackground(BG_DARK);

        RoundBtn btnClose = new RoundBtn("Đóng", BTN_CANCEL, TEXT_WHITE);
        btnClose.setPreferredSize(new Dimension(110, 40));
        btnClose.addActionListener(e -> dispose());
        btnRow.add(btnClose);

        p.add(sep,    BorderLayout.NORTH);
        p.add(btnRow, BorderLayout.CENTER);
        return p;
    }

    // ── LOAD DATA ───────────────────────────────────────────────
    private void loadData() {
        HoaDon hd = new HoaDonService().getHoaDonById(maHD);
        if (hd == null) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy hóa đơn HD" + String.format("%03d", maHD),
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        lblTenKH.setText(nvl(hd.getTenKhachHang()));
        lblSDT.setText(nvl(hd.getSoDienThoai()));

        String tt = nvl(hd.getTrangThai());
        lblTrangThai.setText(tt);
        lblTrangThai.setForeground(
            tt.equalsIgnoreCase("DaThanhToan") ? COLOR_GREEN : COLOR_WARN
        );

        lblNgayLap.setText(hd.getNgayLap() != null ? hd.getNgayLap().format(FMT) : "—");
        lblMaNV.setText("NV" + String.format("%03d", hd.getMaNV()));

        int soLuong = hd.getDanhSachChiTiet() != null ? hd.getDanhSachChiTiet().size() : 0;
        lblSoLuongSP.setText(soLuong + " sản phẩm");

        // Fill table
        tableModel.setRowCount(0);
        if (hd.getDanhSachChiTiet() != null) {
            for (HoaDon.ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                double thanhTien = ct.getSoLuong() * ct.getDonGia();
                tableModel.addRow(new Object[]{
                    ct.getTenGame(),
                    ct.getLoaiSanPham(),
                    ct.getSoLuong(),
                    String.format("%,.0f đ", ct.getDonGia()),
                    String.format("%,.0f đ", thanhTien)
                });
            }
        }

        lblTongTien.setText(String.format("%,.0f VNĐ", hd.getTongTien()));
    }

    // ── UI Helpers ───────────────────────────────────────────────
    private JPanel infoRow(JPanel... cards) {
        JPanel row = new JPanel(new GridLayout(1, cards.length, 10, 0));
        row.setBackground(BG_DARK);
        for (JPanel c : cards) row.add(c);
        return row;
    }

    private JPanel infoCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(TEXT_MUTED);
        card.add(titleLbl,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel cardValue() {
        JLabel l = new JLabel("—");
        l.setFont(FONT_HEADER);
        l.setForeground(TEXT_WHITE);
        return l;
    }

    private String nvl(String s) { return s == null || s.isBlank() ? "—" : s; }

    // ── Round Button ─────────────────────────────────────────────
    static class RoundBtn extends JButton {
        private final Color bg, fg;
        RoundBtn(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg; this.fg = fg;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
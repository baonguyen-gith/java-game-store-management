package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.TransactionDTO;
import otkhongluong.gamestoremanagement.controller.TransactionController;
import otkhongluong.gamestoremanagement.controller.TransactionController.PageResult;
import otkhongluong.gamestoremanagement.view.dialog.InvoiceDetailDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentDetailDialog;
import otkhongluong.gamestoremanagement.util.RoundButton;
import otkhongluong.gamestoremanagement.util.IconUtils;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * View — Màn hình Lịch sử giao dịch.
 *
 * Không chứa bất kỳ logic filter / sort / phân trang nào.
 * Mọi sự kiện người dùng đều được ủy quyền cho {@link TransactionController}.
 */
public class TransactionPanel extends JPanel {

    // ══════════════════════════════════════════════════════════
    // Constants
    // ══════════════════════════════════════════════════════════
    private static final Color BG_DARK       = new Color(35, 20, 85);
    private static final Color BG_CARD       = Color.WHITE;
    private static final Color PURPLE_HEADER = new Color(155, 135, 245);
    private static final Color PURPLE_ROW    = new Color(245, 242, 255);
    private static final Color PURPLE_ALT    = Color.WHITE;
    private static final Color ACCENT        = new Color(130, 90, 230);
    private static final Color TEXT_WHITE    = Color.WHITE;
    private static final Color TEXT_MUTED    = new Color(120, 120, 140);
    private static final Color INPUT_BG      = Color.WHITE;
    private static final Color INPUT_ERROR   = new Color(255, 200, 200);

    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_CELL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] COLS = {
        "Mã GD", "Loại", "Mã NV", "Khách hàng", "Ngày", "Tiền", "Chi tiết"
    };

    // ══════════════════════════════════════════════════════════
    // MVC
    // ══════════════════════════════════════════════════════════
    private final TransactionController controller;

    // ══════════════════════════════════════════════════════════
    // State thuần View
    // ══════════════════════════════════════════════════════════

    /** Dữ liệu của trang đang hiển thị — dùng khi mở dialog. */
    private List<TransactionDTO> currentPageData = new ArrayList<>();

    // ══════════════════════════════════════════════════════════
    // Components
    // ══════════════════════════════════════════════════════════
    private JTextField txtFrom, txtTo, txtSearch;
    private JComboBox<String> cmbLoai;

    private JTable table;
    private DefaultTableModel tableModel;

    private JPanel paginationPanel;
    private JLabel lblPageInfo;

    // ══════════════════════════════════════════════════════════
    // Constructor
    // ══════════════════════════════════════════════════════════
    public TransactionPanel() {
        this(new TransactionController());
    }

    /** Constructor cho phép inject Controller (dễ test). */
    public TransactionPanel(TransactionController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        // Tải dữ liệu lần đầu
        render(controller.loadAll());
    }

    // ══════════════════════════════════════════════════════════
    // BUILD UI
    // ══════════════════════════════════════════════════════════
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        left.setBackground(BG_DARK);

        txtFrom = datePicker();
        txtTo   = datePicker();

        // ── Listener chung: bất kỳ thay đổi filter nào → gọi controller ──
        KeyAdapter filterListener = new KeyAdapter() {
            public void keyReleased(KeyEvent e) { fireFilterChanged(); }
        };
        txtFrom.addKeyListener(filterListener);
        txtTo.addKeyListener(filterListener);

        left.add(labeledField("Từ ngày", txtFrom));
        left.add(labeledField("Đến ngày", txtTo));
        left.add(buildSearchField());
        left.add(buildLoaiFilter());

        // ── Nút Refresh ──
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        right.setBackground(BG_DARK);

        RoundButton btnRefresh = new RoundButton("Làm mới", ACCENT, TEXT_WHITE);
        btnRefresh.setIcon(IconUtils.getRefreshIcon(18, BG_DARK));
        btnRefresh.setPreferredSize(new Dimension(120, 38));
        btnRefresh.setToolTipText("Tải lại dữ liệu từ cơ sở dữ liệu");
        btnRefresh.addActionListener(e -> {
            txtFrom.setText("");
            txtTo.setText("");
            txtSearch.setText("");
            if (cmbLoai != null) cmbLoai.setSelectedIndex(0);
            txtFrom.setBackground(INPUT_BG);
            txtTo.setBackground(INPUT_BG);
            render(controller.loadAll());
        });
        right.add(btnRefresh);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildSearchField() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(BG_DARK);

        JLabel lbl = new JLabel("Tìm kiếm từ khóa");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_WHITE);
        p.add(lbl, BorderLayout.NORTH);

        txtSearch = new JTextField(18) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(FONT_CELL);
                    g2.drawString("Tìm theo mã, loại...", 10, getHeight() / 2 + 5);
                }
                g2.dispose();
            }
        };
        txtSearch.setBackground(INPUT_BG);
        txtSearch.setForeground(new Color(40, 40, 40));
        txtSearch.setCaretColor(ACCENT);
        txtSearch.setFont(FONT_CELL);
        txtSearch.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        txtSearch.setOpaque(false);
        txtSearch.setPreferredSize(new Dimension(0, 38));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { fireFilterChanged(); }
        });

        p.add(txtSearch, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildLoaiFilter() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(BG_DARK);

        JLabel lbl = new JLabel("Loại giao dịch");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_WHITE);
        p.add(lbl, BorderLayout.NORTH);

        cmbLoai = new JComboBox<>(new String[]{"Tất cả", "Hóa đơn", "Phiếu thuê"});
        cmbLoai.setFont(FONT_CELL);
        cmbLoai.setBackground(INPUT_BG);
        cmbLoai.setPreferredSize(new Dimension(130, 38));
        cmbLoai.addActionListener(e -> fireFilterChanged());
        p.add(cmbLoai, BorderLayout.CENTER);
        return p;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return c == 6; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                    ((JLabel) c).setBorder(new EmptyBorder(0, 12, 0, 12));
                }
                if (isRowSelected(row)) {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? PURPLE_ROW : PURPLE_ALT);
                    c.setForeground(new Color(40, 40, 40));
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

        // ── Sort header ──
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new SortHeaderRenderer());
        header.setBackground(PURPLE_HEADER);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setReorderingAllowed(false);

        header.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int col = header.columnAtPoint(e.getPoint());
                // Cột "Chi tiết" (index 6) không sort
                if (col < 0 || col == COLS.length - 1) return;
                render(controller.onSortChanged(
                    col, keyword(), loai(), txtFrom.getText(), txtTo.getText()
                ));
                header.repaint();
            }
        });

        // ── Column widths ──
        int[] widths = {100, 110, 75, 150, 110, 130, 75};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // ── "Xem" column ──
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer("Xem", ACCENT));
        table.getColumnModel().getColumn(6).setCellEditor(
            new ButtonEditor(new JCheckBox(), table, this)
        );

        // ── Double click → chi tiết ──
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && row < currentPageData.size())
                        openDetail(currentPageData.get(row));
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(PURPLE_HEADER, 1, true));
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(PURPLE_ALT);
        return sp;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(12, 0, 0, 0));

        paginationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        paginationPanel.setBackground(BG_DARK);

        lblPageInfo = new JLabel();
        lblPageInfo.setFont(FONT_LABEL);
        lblPageInfo.setForeground(TEXT_WHITE);

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftSide.setBackground(BG_DARK);
        leftSide.add(paginationPanel);
        leftSide.add(lblPageInfo);
        bar.add(leftSide, BorderLayout.WEST);
        return bar;
    }

    // ══════════════════════════════════════════════════════════
    // RENDER — nhận PageResult từ Controller và cập nhật UI
    // ══════════════════════════════════════════════════════════

    /**
     * Điểm duy nhất để cập nhật toàn bộ UI.
     * Được gọi sau mỗi lần Controller trả về PageResult.
     */
    private void render(PageResult result) {
        // ── Highlight lỗi ngày ──
        txtFrom.setBackground(result.fromDateError ? INPUT_ERROR : INPUT_BG);
        txtTo.setBackground(result.toDateError     ? INPUT_ERROR : INPUT_BG);

        if (result.hasDateError()) return; // Không render bảng khi ngày sai

        // ── Điền bảng ──
        tableModel.setRowCount(0);
        currentPageData = new ArrayList<>(result.rows);
        for (TransactionDTO row : result.rows) {
            String ngayStr = row.getNgay() != null
                ? row.getNgay().toLocalDate().format(FMT) : "";
            tableModel.addRow(new Object[]{
                row.getId(),
                row.getLoai(),
                "NV" + String.format("%03d", row.getMaNV()),
                row.getTenKhachHang() != null ? row.getTenKhachHang() : "",
                ngayStr,
                String.format("%,.0f đ", row.getTien()),
                "Xem"
            });
        }

        // ── Pagination ──
        rebuildPagination(result);
        table.getTableHeader().repaint();
    }

    private void rebuildPagination(PageResult result) {
        paginationPanel.removeAll();

        RoundButton btnPrev = new RoundButton("<", INPUT_BG, new Color(80, 80, 80));
        btnPrev.setPreferredSize(new Dimension(34, 34));
        btnPrev.setEnabled(result.currentPage > 1);
        btnPrev.addActionListener(e ->
            render(controller.onPageChanged(-1, keyword(), loai(),
                txtFrom.getText(), txtTo.getText()))
        );

        RoundButton btnNext = new RoundButton(">", INPUT_BG, new Color(80, 80, 80));
        btnNext.setPreferredSize(new Dimension(34, 34));
        btnNext.setEnabled(result.currentPage < result.totalPages);
        btnNext.addActionListener(e ->
            render(controller.onPageChanged(+1, keyword(), loai(),
                txtFrom.getText(), txtTo.getText()))
        );

        paginationPanel.add(btnPrev);
        paginationPanel.add(btnNext);

        lblPageInfo.setText(
            "Trang " + result.currentPage + " / " + result.totalPages
            + "  (" + result.totalRows + " bản ghi)"
        );

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    // ══════════════════════════════════════════════════════════
    // EVENT FIRING
    // ══════════════════════════════════════════════════════════

    /** Gom tất cả filter-change events vào một chỗ. */
    private void fireFilterChanged() {
        render(controller.onFilterChanged(
            keyword(), loai(), txtFrom.getText(), txtTo.getText()
        ));
    }

    // ══════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════
    private String keyword() {
        return txtSearch == null ? "" : txtSearch.getText();
    }
    private String loai() {
        return cmbLoai == null ? "Tất cả" : (String) cmbLoai.getSelectedItem();
    }

    /** Mở dialog chi tiết đúng loại giao dịch. */
    void openDetail(TransactionDTO dataRow) {
        String id   = dataRow.getId();
        String loai = dataRow.getLoai();
        int numId   = Integer.parseInt(id.replaceAll("\\D", ""));
        Window parent = SwingUtilities.getWindowAncestor(this);

        if (loai != null && loai.contains("Hóa")) {
            InvoiceDetailDialog d = new InvoiceDetailDialog((Frame) parent, numId);
            d.pack();
            d.setLocationRelativeTo(parent);
            d.setVisible(true);
        } else {
            RentDetailDialog d = new RentDetailDialog((Frame) parent, numId);
            d.pack();
            d.setLocationRelativeTo(parent);
            d.setVisible(true);
        }
    }

    // ── Sort header renderer — đọc state từ Controller ────────
    private class SortHeaderRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            int[] state = controller.getSortState();
            int   sCol  = controller.getSortCol();

            String arrow = "";
            if (sCol == c) {
                arrow = state[c] == 1 ? "  ▲" : state[c] == 2 ? "  ▼" : "";
            } else if (c < COLS.length - 1) {
                arrow = "";
            }

            JLabel lbl = new JLabel((v == null ? "" : v.toString()) + arrow);
            lbl.setFont(FONT_HEADER);
            lbl.setForeground(Color.WHITE);
            lbl.setBackground(PURPLE_HEADER);
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return lbl;
        }
    }

    // ══════════════════════════════════════════════════════════
    // INNER CLASSES (UI helpers — không thay đổi so với bản gốc)
    // ══════════════════════════════════════════════════════════

    private JPanel labeledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(BG_DARK);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_WHITE);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField datePicker() {
        JTextField tf = new JTextField(11) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
                if (getText().trim().isEmpty()) {
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(FONT_CELL);
                    g2.drawString("dd/mm/yyyy", 10, getHeight() / 2 + 5);
                }
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 13));
                g2.drawString("📅", getWidth() - 26, getHeight() / 2 + 5);
                g2.dispose();
            }
        };
        tf.setBackground(INPUT_BG);
        tf.setForeground(new Color(40, 40, 40));
        tf.setCaretColor(ACCENT);
        tf.setFont(FONT_CELL);
        tf.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(6, 10, 6, 30)
        ));
        tf.setOpaque(false);
        tf.setPreferredSize(new Dimension(0, 38));
        return tf;
    }

    static class ButtonRenderer extends JButton implements TableCellRenderer {
        ButtonRenderer(String text, Color bg) {
            super(text);
            setOpaque(true);
            setBackground(bg);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(new EmptyBorder(4, 10, 4, 10));
            setFocusPainted(false);
        }
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            return this;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        private int currentRow;
        private final TransactionPanel panel;

        ButtonEditor(JCheckBox checkBox, JTable table, TransactionPanel panel) {
            super(checkBox);
            this.panel = panel;
            JButton btn = new JButton("Xem");
            btn.setBackground(new Color(130, 90, 230));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                fireEditingStopped();
                if (currentRow >= 0 && currentRow < panel.currentPageData.size())
                    panel.openDetail(panel.currentPageData.get(currentRow));
            });
            editorComponent = btn;
        }

        public Object getCellEditorValue() { return "Xem"; }

        public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int r, int c) {
            currentRow = r;
            return editorComponent;
        }
    }
}
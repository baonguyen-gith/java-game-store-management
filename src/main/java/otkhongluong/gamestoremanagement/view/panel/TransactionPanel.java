package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.dao.TransactionDAO;
import otkhongluong.gamestoremanagement.view.dialog.InvoiceDetailDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentDetailDialog;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionPanel extends JPanel {

    /* ── Colors ── */
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

    /* ── Fonts ── */
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_CELL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ── Columns ── */
    private static final String[] COLS = {
        "Mã GD", "Loại", "Mã NV", "Khách hàng", "Ngày", "Tiền", "Chi tiết"
    };

    /* ── Sorting ── */
    private final int[] sortState = new int[COLS.length];
    private int sortCol = -1;

    /* ── Pagination ── */
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;

    /* ── Data ── */
    // Mỗi Object[]: [0]=ID string, [1]=Loai, [2]=Timestamp, [3]=Double, [4]="Xem"
    private List<Object[]> allData = new ArrayList<>();
    private List<Object[]> currentPageData = new ArrayList<>();
    private final TransactionDAO dao = new TransactionDAO();

    /* ── Components ── */
    private JTextField txtFrom, txtTo, txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private JPanel paginationPanel;
    private JLabel lblPageInfo;

    // ═══════════════════════════════════════════════════════════
    public TransactionPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        loadData();
    }

    // ══════════════════════════════════════════════════════════
    // TOP BAR
    // ══════════════════════════════════════════════════════════
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        left.setBackground(BG_DARK);

        txtFrom = datePicker();
        txtTo   = datePicker();

        KeyAdapter dateListener = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                currentPage = 1;
                validateDates();
                renderPage();
            }
        };
        txtFrom.addKeyListener(dateListener);
        txtTo.addKeyListener(dateListener);

        left.add(labeledField("Từ ngày", txtFrom));
        left.add(labeledField("Đến ngày", txtTo));
        left.add(buildSearchField());
        left.add(buildLoaiFilter());

        bar.add(left, BorderLayout.WEST);

        // ── Nút Refresh (góc phải) ──
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        right.setBackground(BG_DARK);

        RoundButton btnRefresh = new RoundButton("⟳  Làm mới", ACCENT, TEXT_WHITE);
        btnRefresh.setPreferredSize(new Dimension(120, 38));
        btnRefresh.setToolTipText("Tải lại dữ liệu từ cơ sở dữ liệu");
        btnRefresh.addActionListener(e -> {
            txtFrom.setText("");
            txtTo.setText("");
            txtSearch.setText("");
            if (cmbLoai != null) cmbLoai.setSelectedIndex(0);
            loadData();
        });
        right.add(btnRefresh);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

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
                    g2.drawString("dd/MM/yyyy", 10, getHeight() / 2 + 5);
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
            public void keyReleased(KeyEvent e) { currentPage = 1; renderPage(); }
        });

        p.add(txtSearch, BorderLayout.CENTER);
        return p;
    }

    /* ComboBox lọc theo loại giao dịch */
    private JComboBox<String> cmbLoai;

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
        cmbLoai.addActionListener(e -> { currentPage = 1; renderPage(); });
        p.add(cmbLoai, BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════
    // TABLE
    // ══════════════════════════════════════════════════════════
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return c == 6; }
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

        // Sort header
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                String arrow = "";
                if (sortCol == c) {
                    arrow = sortState[c] == 1 ? "  ▲" : sortState[c] == 2 ? "  ▼" : "";
                } else {
                    if (c < COLS.length - 1) arrow = "  ⇅";
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
        });
        header.setBackground(PURPLE_HEADER);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setReorderingAllowed(false);

        header.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int col = header.columnAtPoint(e.getPoint());
                if (col < 0 || col == COLS.length - 1) return;
                sortState[col] = (sortState[col] + 1) % 3;
                sortCol = sortState[col] == 0 ? -1 : col;
                for (int i = 0; i < sortState.length; i++)
                    if (i != col) sortState[i] = 0;
                currentPage = 1;
                renderPage();
                header.repaint();
            }
        });

        // Column widths
        int[] widths = {100, 110, 75, 150, 110, 130, 75};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // "Xem" button column
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer("Xem", ACCENT));
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox(), table, this));

        // Double click → detail
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) openDetail(currentPageData.get(row));
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(PURPLE_HEADER, 1, true));
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(PURPLE_ALT);
        return sp;
    }

    // ══════════════════════════════════════════════════════════
    // BOTTOM BAR: pagination
    // ══════════════════════════════════════════════════════════
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

    private void rebuildPagination(int totalPages) {
        paginationPanel.removeAll();

        RoundButton btnPrev = new RoundButton("<", INPUT_BG, new Color(80, 80, 80));
        btnPrev.setPreferredSize(new Dimension(34, 34));
        btnPrev.setEnabled(currentPage > 1);
        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; renderPage(); } });
        paginationPanel.add(btnPrev);

        RoundButton btnNext = new RoundButton(">", INPUT_BG, new Color(80, 80, 80));
        btnNext.setPreferredSize(new Dimension(34, 34));
        btnNext.setEnabled(currentPage < totalPages);
        btnNext.addActionListener(e -> { if (currentPage < totalPages) { currentPage++; renderPage(); } });
        paginationPanel.add(btnNext);

        lblPageInfo.setText("Trang " + currentPage + " / " + Math.max(1, totalPages));

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    // ══════════════════════════════════════════════════════════
    // DATA PIPELINE
    // ══════════════════════════════════════════════════════════
    private void loadData() {
        allData = dao.findAll();
        currentPage = 1;
        Arrays.fill(sortState, 0);
        sortCol = -1;
        renderPage();
    }

    private LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        return LocalDate.parse(text.trim(), FMT);
    }

    private boolean validateDates() {
        boolean ok = true;
        for (JTextField tf : new JTextField[]{txtFrom, txtTo}) {
            String txt = tf.getText().trim();
            if (txt.isEmpty()) { tf.setBackground(INPUT_BG); continue; }
            try {
                LocalDate.parse(txt, FMT);
                tf.setBackground(INPUT_BG);
            } catch (DateTimeParseException ex) {
                tf.setBackground(INPUT_ERROR);
                ok = false;
            }
        }
        return ok;
    }

    private List<Object[]> getFilteredData() {
        if (allData == null) return Collections.emptyList();

        LocalDate from = null, to = null;
        try { from = parseDate(txtFrom == null ? null : txtFrom.getText()); } catch (Exception ignored) {}
        try { to   = parseDate(txtTo   == null ? null : txtTo.getText());   } catch (Exception ignored) {}

        final LocalDate fFrom = from, fTo = to;
        String kw = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();
        String loai = cmbLoai == null ? "Tất cả" : (String) cmbLoai.getSelectedItem();

        List<Object[]> result = allData.stream().filter(row -> {
            String id   = nvl((String) row[0]);
            String loaiGD = nvl((String) row[1]);
            Timestamp ts = (Timestamp) row[4];

            // filter loại
            if (!"Tất cả".equals(loai) && !loaiGD.contains(loai.replace("Hóa đơn", "Hóa").replace("Phiếu thuê", "Phiếu")))
                if (!loaiGD.equalsIgnoreCase(loai)) return false;

            // filter ngày
            if ((fFrom != null || fTo != null) && ts != null) {
                LocalDate ngay = ts.toLocalDateTime().toLocalDate();
                if (fFrom != null && ngay.isBefore(fFrom)) return false;
                if (fTo   != null && ngay.isAfter(fTo))   return false;
            }

            // filter keyword
            if (!kw.isEmpty()) {
                String flat = (id + " " + loaiGD + " " + nvl((String) row[3])).toLowerCase();
                if (!flat.contains(kw)) return false;
            }

            return true;
        }).collect(Collectors.toList());

        // sort
        if (sortCol >= 0) {
            Comparator<Object[]> cmp = buildComparator(sortCol);
            if (sortState[sortCol] == 2) cmp = cmp.reversed();
            result.sort(cmp);
        }

        return result;
    }

    private Comparator<Object[]> buildComparator(int col) {
        switch (col) {
            case 0: return Comparator.comparing(r -> nvl((String) r[0]));
            case 1: return Comparator.comparing(r -> nvl((String) r[1]));
            case 2: return Comparator.comparingInt(r -> (Integer) r[2]);
            case 3: return Comparator.comparing(r -> nvl((String) r[3]));
            case 4: return Comparator.comparing(r -> r[4] != null ? ((Timestamp) r[4]).getTime() : 0L);
            case 5: return Comparator.comparingDouble(r -> (Double) r[5]);
            default: return Comparator.comparing(r -> nvl((String) r[0]));
        }
    }

    private void renderPage() {
        if (!validateDates()) return;

        tableModel.setRowCount(0);

        List<Object[]> filtered = getFilteredData();

        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        if (currentPage > totalPages) currentPage = totalPages;

        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filtered.size());

        currentPageData = new ArrayList<>(filtered.subList(from, to));

        for (Object[] row : currentPageData) {
            String ngayStr = "";
            if (row[4] instanceof Timestamp) {
                ngayStr = ((Timestamp) row[4]).toLocalDateTime().toLocalDate().format(FMT);
            }
            tableModel.addRow(new Object[]{
                row[0],                                      // Mã GD
                row[1],                                      // Loại
                "NV" + String.format("%03d", (Integer)row[2]), // Mã NV
                nvl((String) row[3]),                        // Khách hàng
                ngayStr,                                     // Ngày
                String.format("%,.0f đ", (Double) row[5]),   // Tiền
                "Xem"
            });
        }

        rebuildPagination(totalPages);
        table.getTableHeader().repaint();
    }

    // ── Mở dialog đúng loại ────────────────────────────────────
    void openDetail(Object[] dataRow) {
        String id   = (String) dataRow[0]; // "HD001" hoặc "PT001"
        String loai = (String) dataRow[1];
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

    // ── Helpers ────────────────────────────────────────────────
    private String nvl(String s) { return s == null ? "" : s; }

    // ══════════════════════════════════════════════════════════
    // INNER CLASSES
    // ══════════════════════════════════════════════════════════
    static class RoundButton extends JButton {
        private final Color bg, fg;
        RoundButton(String text, Color bg, Color fg) {
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
            g2.setColor(!isEnabled() ? bg.darker() : getModel().isRollover() ? bg.brighter() : bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            super.paintComponent(g2);
            g2.dispose();
        }
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
                if (currentRow >= 0 && currentRow < panel.currentPageData.size()) {
                    panel.openDetail(panel.currentPageData.get(currentRow));
                }
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
package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.view.dialog.RentDetailDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentEditDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentReturnDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentAddDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentExtendDialog;
import otkhongluong.gamestoremanagement.dao.PhieuThueDAO;
import otkhongluong.gamestoremanagement.model.PhieuThue;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class RentPanel extends JPanel {

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
    private static final Color BTN_EDIT      = new Color(99, 179, 237);
    private static final Color BTN_DELETE    = new Color(252, 129, 129);
    private static final Color BTN_ADD       = new Color(104, 211, 145);
    private static final Color BTN_RETURN    = new Color(255, 165, 0);
    private static final Color BTN_EXTEND    = new Color(104, 175, 255);

    /* ── Fonts ── */
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_CELL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ── Column names ── */
    private static final String[] COLS = {
        "Mã PT", "Mã NV", "Khách hàng", "SĐT",
        "Ngày thuê", "Ngày trả DK", "Trạng thái", "Chi tiết"
    };

    /* ── Sorting: 0=none, 1=asc, 2=desc per column ── */
    private final int[] sortState = new int[COLS.length];
    private int sortCol = -1;

    /* ── Pagination ── */
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;

    /* ── Data ── */
    private List<PhieuThue> allData = new ArrayList<>();
    private List<PhieuThue> currentPageData = new ArrayList<>();
    private final PhieuThueDAO dao = new PhieuThueDAO();

    /* ── Components ── */
    private JTextField txtFrom, txtTo, txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private JPanel paginationPanel;
    private JLabel lblPageInfo;

    // ═══════════════════════════════════════════════════════════
    public RentPanel() {
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

        // LEFT: filters
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

        left.add(labeledField("Từ ngày (Ngày thuê)", txtFrom));
        left.add(labeledField("Đến ngày (Ngày thuê)", txtTo));
        left.add(buildSearchField());

        // RIGHT: action buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        right.setBackground(BG_DARK);

        RoundButton btnReturn = new RoundButton("Trả CD", BTN_RETURN, Color.WHITE);
        btnReturn.setPreferredSize(new Dimension(118, 38));
        btnReturn.addActionListener(e -> {
            int row = table.getSelectedRow();
            int id = (row >= 0)
                ? parseMa(tableModel.getValueAt(row, 0).toString()) : -1;
            new RentReturnDialog((Frame) SwingUtilities.getWindowAncestor(this), id).setVisible(true);
            loadData();
        });

        RoundButton btnExtend = new RoundButton("Gia hạn", BTN_EXTEND, Color.WHITE);
        btnExtend.setPreferredSize(new Dimension(118, 38));
        btnExtend.addActionListener(e -> {
            int row = table.getSelectedRow();
            int id = (row >= 0)
                ? parseMa(tableModel.getValueAt(row, 0).toString()) : -1;
            new RentExtendDialog((Frame) SwingUtilities.getWindowAncestor(this), id).setVisible(true);
            loadData();
        });

        RoundButton btnAdd = new RoundButton("+  Thêm", BTN_ADD, new Color(30, 30, 30));
        btnAdd.setPreferredSize(new Dimension(105, 38));
        btnAdd.addActionListener(e -> {
            new RentAddDialog((Frame) SwingUtilities.getWindowAncestor(this)).setVisible(true);
            loadData();
        });

        right.add(btnReturn);
        right.add(btnExtend);
        right.add(btnAdd);

        bar.add(left,  BorderLayout.WEST);
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

        txtSearch = new JTextField(22) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(FONT_CELL);
                    g2.drawString("Tìm kiếm theo từ khóa", 10, getHeight() / 2 + 5);
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

    // ══════════════════════════════════════════════════════════
    // TABLE with sort headers
    // ══════════════════════════════════════════════════════════
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return c == 7; }
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

        // ── Sort header renderer ──
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                String arrow = "";
                if (sortCol == c) {
                    arrow = sortState[c] == 1 ? "  ▲" : sortState[c] == 2 ? "  ▼" : "";
                } else {
                    // hiện mờ 2 mũi tên nếu chưa sort cột này (chỉ với cột có thể sort)
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

        // Click header → sort (trừ cột "Chi tiết")
        header.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int col = header.columnAtPoint(e.getPoint());
                if (col < 0 || col == COLS.length - 1) return; // bỏ qua cột Chi tiết
                // 3-state cycle: 0→1→2→0
                sortState[col] = (sortState[col] + 1) % 3;
                sortCol = sortState[col] == 0 ? -1 : col;
                // reset các cột khác
                for (int i = 0; i < sortState.length; i++)
                    if (i != col) sortState[i] = 0;
                currentPage = 1;
                renderPage();
                header.repaint();
            }
        });

        // Column widths
        int[] widths = {75, 75, 160, 115, 115, 115, 110, 75};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // "Chi tiết" button column
        table.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer("Xem", ACCENT));
        table.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox(), table));

        // Double click → detail
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row < 0) return;
                    openDetail(parseMa(tableModel.getValueAt(row, 0).toString()));
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
    // BOTTOM BAR: pagination (left) + Sửa / Xóa (right)
    // ══════════════════════════════════════════════════════════
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(12, 0, 0, 0));

        // LEFT: pagination
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

        // RIGHT: Sửa + Xóa
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(BG_DARK);

        RoundButton btnEdit = new RoundButton("Sửa", BTN_EDIT, Color.WHITE);
        btnEdit.setPreferredSize(new Dimension(105, 38));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn phiếu thuê để sửa!"); return; }
            PhieuThue pt = currentPageData.get(row);
            new RentEditDialog((Frame) SwingUtilities.getWindowAncestor(this), pt).setVisible(true);
            loadData();
        });

        RoundButton btnDelete = new RoundButton("Xóa", BTN_DELETE, Color.WHITE);
        btnDelete.setPreferredSize(new Dimension(105, 38));
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn phiếu thuê để xóa!"); return; }

            String maPTStr = tableModel.getValueAt(row, 0).toString();
            int id = parseMa(maPTStr);
            PhieuThue pt = currentPageData.get(row);

            if ("DangThue".equalsIgnoreCase(pt.getTrangThai())) {
                JOptionPane.showMessageDialog(this,
                    "Không thể xóa phiếu thuê đang hoạt động!\nVui lòng trả CD trước khi xóa.",
                    "Không cho phép", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa " + maPTStr + "?\nKhách hàng: " + pt.getTenKhachHang(),
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            System.out.println("[DELETE] maPT = " + id);
            boolean ok = dao.delete(id);
            System.out.println("[DELETE] result = " + ok);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã xóa " + maPTStr);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        bar.add(btnPanel, BorderLayout.EAST);
        return bar;
    }

    // ══════════════════════════════════════════════════════════
    // PAGINATION rebuild
    // ══════════════════════════════════════════════════════════
    private void rebuildPagination(int totalPages) {
        paginationPanel.removeAll();

        // Nút "<" — về trang trước
        RoundButton btnPrev = new RoundButton("<", INPUT_BG, new Color(80, 80, 80));
        btnPrev.setPreferredSize(new Dimension(34, 34));
        btnPrev.setEnabled(currentPage > 1);
        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; renderPage(); } });
        paginationPanel.add(btnPrev);

        // Nút ">" — sang trang sau
        RoundButton btnNext = new RoundButton(">", INPUT_BG, new Color(80, 80, 80));
        btnNext.setPreferredSize(new Dimension(34, 34));
        btnNext.setEnabled(currentPage < totalPages);
        btnNext.addActionListener(e -> { if (currentPage < totalPages) { currentPage++; renderPage(); } });
        paginationPanel.add(btnNext);

        // Label "Trang X / Y"
        lblPageInfo.setText("Trang " + currentPage + " / " + Math.max(1, totalPages));

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    // ══════════════════════════════════════════════════════════
    // DATA PIPELINE: load → filter → sort → page → render
    // ══════════════════════════════════════════════════════════
    private void loadData() {
        allData = dao.findAll();
        currentPage = 1;
        Arrays.fill(sortState, 0);
        sortCol = -1;
        renderPage();
    }

    /** Parse LocalDate từ ô nhập, trả null nếu rỗng, throw nếu sai định dạng */
    private LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        return LocalDate.parse(text.trim(), FMT); // throws DateTimeParseException nếu sai
    }

    /** Validate 2 ô ngày, highlight đỏ nếu sai, trả false nếu có lỗi */
    private boolean validateDates() {
        boolean ok = true;
        for (JTextField tf : new JTextField[]{txtFrom, txtTo}) {
            String txt = tf.getText().trim();
            if (txt.isEmpty()) {
                tf.setBackground(INPUT_BG);
                continue;
            }
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

    private List<PhieuThue> getFilteredData() {
        if (allData == null) return Collections.emptyList();

        // 1. Date filter (chỉ lọc nếu cả 2 ô hợp lệ hoặc chỉ 1 ô có giá trị hợp lệ)
        LocalDate from = null, to = null;
        try { from = parseDate(txtFrom == null ? null : txtFrom.getText()); } catch (Exception ignored) {}
        try { to   = parseDate(txtTo   == null ? null : txtTo.getText());   } catch (Exception ignored) {}

        final LocalDate fFrom = from, fTo = to;

        // 2. Keyword filter — tìm trên TẤT CẢ các cột hiển thị
        String kw = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();

        List<PhieuThue> result = allData.stream().filter(pt -> {
            // date filter
            if (fFrom != null || fTo != null) {
                if (pt.getNgayThue() == null) return false;
                LocalDate ngayThue = pt.getNgayThue().toLocalDate();
                if (fFrom != null && ngayThue.isBefore(fFrom)) return false;
                if (fTo   != null && ngayThue.isAfter(fTo))   return false;
            }
            // keyword filter — mọi cột
            if (!kw.isEmpty()) {
                String row = String.join(" ",
                    nvl(pt.getMaPTFormatted()),
                    nvl(pt.getMaNVFormatted()),
                    nvl(pt.getTenKhachHang()),
                    nvl(pt.getSoDienThoai()),
                    pt.getNgayThue()       != null ? pt.getNgayThue().format(FMT)       : "",
                    pt.getNgayTraDuKien()  != null ? pt.getNgayTraDuKien().format(FMT)  : "",
                    nvl(pt.getTrangThai())
                ).toLowerCase();
                if (!row.contains(kw)) return false;
            }
            return true;
        }).collect(Collectors.toList());

        // 3. Sort
        if (sortCol >= 0) {
            Comparator<PhieuThue> cmp = buildComparator(sortCol);
            if (sortState[sortCol] == 2) cmp = cmp.reversed();
            result.sort(cmp);
        }

        return result;
    }

    private Comparator<PhieuThue> buildComparator(int col) {
        switch (col) {
            case 0: return Comparator.comparingInt(PhieuThue::getMaPT);
            case 1: return Comparator.comparingInt(PhieuThue::getMaNV);
            case 2: return Comparator.comparing(pt -> nvl(pt.getTenKhachHang()));
            case 3: return Comparator.comparing(pt -> nvl(pt.getSoDienThoai()));
            case 4: return Comparator.comparing(pt -> pt.getNgayThue() != null ? pt.getNgayThue() : java.time.LocalDateTime.MIN);
            case 5: return Comparator.comparing(pt -> pt.getNgayTraDuKien() != null ? pt.getNgayTraDuKien() : java.time.LocalDateTime.MIN);
            case 6: return Comparator.comparing(pt -> nvl(pt.getTrangThai()));
            default: return Comparator.comparingInt(PhieuThue::getMaPT);
        }
    }

    private void renderPage() {
        // Không render nếu có ô ngày lỗi
        if (!validateDates()) return;

        tableModel.setRowCount(0);

        List<PhieuThue> filtered = getFilteredData();

        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        if (currentPage > totalPages) currentPage = totalPages;

        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filtered.size());

        currentPageData = new ArrayList<>(filtered.subList(from, to));

        for (PhieuThue pt : currentPageData) {
            tableModel.addRow(new Object[]{
                pt.getMaPTFormatted(),
                pt.getMaNVFormatted(),
                pt.getTenKhachHang(),
                pt.getSoDienThoai(),
                pt.getNgayThue()      != null ? pt.getNgayThue().format(FMT)      : "",
                pt.getNgayTraDuKien() != null ? pt.getNgayTraDuKien().format(FMT) : "",
                pt.getTrangThai(),
                "Xem"
            });
        }

        rebuildPagination(totalPages);
        table.getTableHeader().repaint(); // cập nhật mũi tên sort
    }

    // ── Helpers ────────────────────────────────────────────────

    private void validateAndFilter() {
        currentPage = 1;
        renderPage();
    }

    private int parseMa(String formatted) {
        return Integer.parseInt(formatted.replaceAll("\\D", ""));
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private void openDetail(int id) {
        Window parent = SwingUtilities.getWindowAncestor(table);
        RentDetailDialog d = new RentDetailDialog((Frame) parent, id);
        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }

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

        ButtonEditor(JCheckBox checkBox, JTable table) {
            super(checkBox);
            JButton btn = new JButton("Xem");
            btn.setBackground(new Color(130, 90, 230));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                fireEditingStopped();
                String maPT = table.getValueAt(currentRow, 0).toString();
                int id = Integer.parseInt(maPT.replaceAll("\\D", ""));
                Window parent = SwingUtilities.getWindowAncestor(table);
                RentDetailDialog d = new RentDetailDialog((Frame) parent, id);
                d.pack();
                d.setLocationRelativeTo(parent);
                d.setVisible(true);
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
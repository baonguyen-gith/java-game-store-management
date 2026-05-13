package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.controller.ProductController;
import otkhongluong.gamestoremanagement.model.Product;
import otkhongluong.gamestoremanagement.util.IconUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * View – chỉ chịu trách nhiệm hiển thị và thu nhận sự kiện.
 * Mọi nghiệp vụ (load, filter, sort, add, edit, delete) đều
 * uỷ quyền cho ProductController.
 */
public class ProductPanel extends JPanel {

    /* ============ COLORS ============ */
    private static final Color BG_DARK       = new Color(35, 20, 85);
    private static final Color BG_CARD       = Color.WHITE;

    private static final Color PURPLE_HEADER = new Color(155, 135, 245);
    private static final Color PURPLE_ROW    = new Color(245, 242, 255);
    private static final Color PURPLE_ALT    = Color.WHITE;

    private static final Color ACCENT        = new Color(130, 90, 230);

    private static final Color TEXT_WHITE    = Color.WHITE;
    private static final Color TEXT_MUTED    = new Color(120, 120, 140);

    private static final Color INPUT_BG      = Color.WHITE;
    private static final Color BTN_EDIT      = new Color(99, 179, 237);
    private static final Color BTN_DELETE    = new Color(252, 129, 129);
    private static final Color BTN_ADD       = new Color(104, 211, 145);

    /* ============ FONTS ============ */
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_CELL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);

    /* ============ COMPONENTS ============ */
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JPanel paginationPanel;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 8;

    private RoundButton btnFilter;
    private boolean isFilterMode = false;
    private TableRowSorter<DefaultTableModel> rowSorter;

    /* ============ MVC: chỉ giữ Controller, không giữ Service ============ */
    private final ProductController controller;

    /** Dữ liệu gốc (toàn bộ từ DB). */
    private List<Product> allData;
    /** Dữ liệu của trang hiện tại (sau filter + phân trang). */
    private List<Product> currentPageData;

    // ======================================================
    //  KHỞI TẠO
    // ======================================================
    public ProductPanel() {
        controller = new ProductController(this); // View tạo Controller, truyền chính mình

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        loadData();
    }

    // ======================================================
    //  TOP BAR
    // ======================================================
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("QUẢN LÝ SẢN PHẨM");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 20));
        bar.add(title, BorderLayout.WEST);

        bar.add(buildSearchArea(), BorderLayout.EAST);
        return bar;
    }

    private JPanel buildSearchArea() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BG_DARK);

        JLabel lbl = new JLabel("Tìm kiếm từ khóa");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_WHITE);
        p.add(lbl, BorderLayout.NORTH);

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BG_DARK);

        // --- Nút Sắp xếp & Lọc (trái) ---
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftGroup.setBackground(BG_DARK);

        RoundButton btnSort = new RoundButton("Sắp xếp", INPUT_BG, BG_DARK);
        btnSort.setPreferredSize(new Dimension(90, 40));
        btnSort.addActionListener(e -> showSortPopupMenu(btnSort));

        btnFilter = new RoundButton("Lọc", INPUT_BG, BG_DARK);
        btnFilter.setPreferredSize(new Dimension(70, 40));
        btnFilter.addActionListener(e -> toggleFilterMode());

        leftGroup.add(btnSort);
        leftGroup.add(btnFilter);

        // --- Ô tìm kiếm (giữa) ---
        txtSearch = new JTextField(22) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Icon icon = IconUtils.getSearchIcon(14, TEXT_MUTED);
                    icon.paintIcon(this, g2, 8, getHeight() / 2 - 7);
                }
                g2.dispose();
            }
        };
        styleTextField(txtSearch);
        // Sự kiện gõ phím: View chỉ reset trang rồi re-render
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                currentPage = 1;
                renderPage();
            }
        });

        // --- Nút Add & Refresh (phải) ---
        RoundButton btnAdd = new RoundButton("", BTN_ADD, Color.WHITE);
        btnAdd.setIcon(IconUtils.getAddIcon(18, Color.WHITE));
        btnAdd.setPreferredSize(new Dimension(40, 40));
        btnAdd.addActionListener(e -> showAddProductDialog());

        RoundButton btnRefresh = new RoundButton("", INPUT_BG, BG_DARK);
        btnRefresh.setIcon(IconUtils.getRefreshIcon(18, BG_DARK));
        btnRefresh.setPreferredSize(new Dimension(40, 40));
        btnRefresh.addActionListener(e -> loadData());

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setBackground(BG_DARK);
        btnGroup.add(btnAdd);
        btnGroup.add(btnRefresh);

        row.add(leftGroup, BorderLayout.WEST);
        row.add(txtSearch, BorderLayout.CENTER);
        row.add(btnGroup,  BorderLayout.EAST);
        p.add(row, BorderLayout.CENTER);
        return p;
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_MUTED);
        tf.setCaretColor(TEXT_WHITE);
        tf.setFont(FONT_CELL);
        tf.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(6, 10, 6, 32)
        ));
        tf.setOpaque(false);
        tf.setPreferredSize(new Dimension(0, 40));
    }

    // ======================================================
    //  TABLE
    // ======================================================
    private JScrollPane buildTable() {
        String[] cols = {"Mã SP", "Mã Game", "Giá Bán", "Giá Thuê Ngày"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (c instanceof JLabel)
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
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
        table.setForeground(TEXT_WHITE);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString());
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(PURPLE_HEADER);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
                return lbl;
            }
        });
        header.setBackground(PURPLE_HEADER);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());

        int[] widths = {100, 100, 150, 150};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        // Click tiêu đề cột để lọc (chỉ khi isFilterMode bật)
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!isFilterMode) return;
                int viewCol  = table.columnAtPoint(e.getPoint());
                if (viewCol < 0) return;
                int modelCol = table.convertColumnIndexToModel(viewCol);
                String colName = table.getColumnName(viewCol);

                String input = JOptionPane.showInputDialog(null, "Lọc theo " + colName + ":");
                if (input != null && !input.trim().isEmpty()) {
                    rowSorter.setRowFilter(
                            RowFilter.regexFilter("(?i)" + input.trim(), modelCol));
                } else if (input != null) {
                    rowSorter.setRowFilter(null);
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(PURPLE_HEADER, 1, true));
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(PURPLE_ALT);
        return sp;
    }

    // ======================================================
    //  BOTTOM BAR
    // ======================================================
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(14, 0, 0, 0));

        paginationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        paginationPanel.setBackground(BG_DARK);
        rebuildPagination(paginationPanel);
        bar.add(paginationPanel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(BG_DARK);

        // --- Nút Sửa ---
        RoundButton btnEdit = new RoundButton(" Sửa", BTN_EDIT, BG_DARK);
        btnEdit.setIcon(IconUtils.getEditIcon(16, BG_DARK));
        btnEdit.setPreferredSize(new Dimension(110, 40));
        btnEdit.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần sửa!");
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            Product selected = currentPageData.get(modelRow);
            showEditProductDialog(selected);
        });

        // --- Nút Xóa ---
        RoundButton btnDelete = new RoundButton(" Xóa", BTN_DELETE, BG_DARK);
        btnDelete.setIcon(IconUtils.getDeleteIcon(16, BG_DARK));
        btnDelete.setPreferredSize(new Dimension(110, 40));
        btnDelete.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để xóa!");
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            Product sp = currentPageData.get(modelRow);

            // Giao toàn bộ logic xác nhận + xóa cho Controller
            if (controller.handleDelete(sp)) {
                loadData();
            }
        });

        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        bar.add(btnPanel, BorderLayout.EAST);
        return bar;
    }

    private void rebuildPagination(JPanel panel) {
        panel.removeAll();
        List<Product> filtered = getFilteredData();
        final int total = Math.max(1,
                (int) Math.ceil((double) filtered.size() / PAGE_SIZE));

        RoundButton btnPrev = new RoundButton("<", INPUT_BG, BG_DARK);
        btnPrev.setPreferredSize(new Dimension(40, 36));
        btnPrev.setEnabled(currentPage > 1);
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) { currentPage--; renderPage(); }
        });

        JLabel lblPageInfo = new JLabel("Trang " + currentPage + " / " + total);
        lblPageInfo.setForeground(TEXT_WHITE);
        lblPageInfo.setFont(FONT_LABEL);
        lblPageInfo.setBorder(new EmptyBorder(0, 10, 0, 10));

        RoundButton btnNext = new RoundButton(">", INPUT_BG, BG_DARK);
        btnNext.setPreferredSize(new Dimension(40, 36));
        btnNext.setEnabled(currentPage < total);
        btnNext.addActionListener(e -> {
            if (currentPage < total) { currentPage++; renderPage(); }
        });

        panel.add(btnPrev);
        panel.add(lblPageInfo);
        panel.add(btnNext);
        panel.revalidate();
        panel.repaint();
    }

    // ======================================================
    //  DATA – View chỉ gọi Controller
    // ======================================================

    /** Tải lại toàn bộ dữ liệu từ Controller rồi re-render. */
    private void loadData() {
        allData = controller.loadAll(); // ← không gọi service trực tiếp
        renderPage();
    }

    /**
     * Lấy danh sách đã lọc theo từ khóa hiện tại.
     * Uỷ quyền logic lọc cho Controller.
     */
    private List<Product> getFilteredData() {
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();
        return controller.filter(allData, keyword); // ← không tự lọc
    }

    /** Render trang hiện tại lên JTable. */
    private void renderPage() {
        tableModel.setRowCount(0);
        List<Product> filtered = getFilteredData();

        int totalPage = Math.max(1,
                (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        if (currentPage > totalPage) currentPage = totalPage;

        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filtered.size());

        currentPageData = filtered.subList(from, to);

        for (Product sp : currentPageData) {
            tableModel.addRow(new Object[]{
                "SP" + String.format("%03d", sp.getMaSP()),
                "G"  + String.format("%03d", sp.getMaGame()),
                String.format("%,.0f đ", sp.getGiaBan()),
                String.format("%,.0f đ", sp.getGiaThueNgay())
            });
        }

        rebuildPagination(paginationPanel);
    }

    // ======================================================
    //  DIALOGS – View thu thập dữ liệu, Controller xử lý
    // ======================================================

    private void showAddProductDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtMaGame  = new JTextField();
        JTextField txtGiaBan  = new JTextField("0");
        JTextField txtGiaThue = new JTextField("0");

        panel.add(new JLabel("Mã Game liên kết (*):"));  panel.add(txtMaGame);
        panel.add(new JLabel("Giá Bán (ROM) - VNĐ:"));   panel.add(txtGiaBan);
        panel.add(new JLabel("Giá Thuê (CD) - VNĐ:"));   panel.add(txtGiaThue);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Thêm Sản Phẩm Mới", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // View chỉ thu thập text, Controller lo validate + gọi Service
            boolean ok = controller.handleAdd(
                    txtMaGame.getText(), txtGiaBan.getText(), txtGiaThue.getText());
            if (ok) loadData();
        }
    }

    private void showEditProductDialog(Product sp) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtMaGame  = new JTextField(String.valueOf(sp.getMaGame()));
        JTextField txtGiaBan  = new JTextField(String.valueOf(sp.getGiaBan()));
        JTextField txtGiaThue = new JTextField(String.valueOf(sp.getGiaThueNgay()));

        panel.add(new JLabel("Mã Game liên kết (*):"));  panel.add(txtMaGame);
        panel.add(new JLabel("Giá Bán (ROM) - VNĐ:"));   panel.add(txtGiaBan);
        panel.add(new JLabel("Giá Thuê (CD) - VNĐ:"));   panel.add(txtGiaThue);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Cập Nhật Sản Phẩm", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            boolean ok = controller.handleUpdate(
                    sp, txtMaGame.getText(), txtGiaBan.getText(), txtGiaThue.getText());
            if (ok) loadData();
        }
    }

    // ======================================================
    //  SORT / FILTER UI
    // ======================================================

    private void toggleFilterMode() {
        isFilterMode = !isFilterMode;
        if (isFilterMode) {
            btnFilter.setBackground(ACCENT);
            btnFilter.setForeground(Color.WHITE);
            JOptionPane.showMessageDialog(this,
                    "Chế độ Lọc ĐÃ BẬT.\nHãy nhấn vào tên cột trên bảng để lọc.");
        } else {
            btnFilter.setBackground(INPUT_BG);
            btnFilter.setForeground(BG_DARK);
            rowSorter.setRowFilter(null);
        }
    }

    private void showSortPopupMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        addSortItem(menu, "Mã Sản Phẩm", "MaSP");
        addSortItem(menu, "Mã Game",      "MaGame");
        addSortItem(menu, "Giá Bán",      "GiaBan");
        addSortItem(menu, "Giá Thuê",     "GiaThue");
        menu.show(invoker, 0, invoker.getHeight());
    }

    private void addSortItem(JPopupMenu menu, String label, String type) {
        JMenuItem asc  = new JMenuItem(label + " (Thấp -> Cao)");
        JMenuItem desc = new JMenuItem(label + " (Cao -> Thấp)");

        // View yêu cầu Controller sắp xếp, rồi tự re-render
        asc.addActionListener(e  -> { controller.sort(allData, type, true);  currentPage = 1; renderPage(); });
        desc.addActionListener(e -> { controller.sort(allData, type, false); currentPage = 1; renderPage(); });

        menu.add(asc);
        menu.add(desc);
        menu.addSeparator();
    }

    // ======================================================
    //  INNER: RoundButton
    // ======================================================
    static class RoundButton extends JButton {
        private Color bg;
        private final Color fg;

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

        /** Cho phép đổi màu nền lúc runtime (dùng cho nút Lọc). */
        @Override public void setBackground(Color bg) {
            this.bg = bg;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
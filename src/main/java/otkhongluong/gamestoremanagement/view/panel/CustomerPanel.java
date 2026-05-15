package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.controller.CustomerController;
import otkhongluong.gamestoremanagement.controller.CustomerController.ActionResult;
import otkhongluong.gamestoremanagement.util.IconUtils;
import otkhongluong.gamestoremanagement.view.dialog.CustomerPointDialog;
import otkhongluong.gamestoremanagement.util.RoundButton;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class CustomerPanel extends JPanel {

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
    private static final Color BTN_POINT     = new Color(246, 173, 85);  // cam – nút quản lý điểm

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

    // ✅ controller khởi tạo trước tất cả buildXxx()
    private final CustomerController controller;
    private List<Customer> allData;
    private List<Customer> currentPageData;

    public CustomerPanel() {
        // ✅ FIX: controller PHẢI được khởi tạo trước khi build UI
        this.controller = new CustomerController();

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        loadData();
    }

    /* ======================================================
        TOP BAR
    ====================================================== */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 20));
        bar.add(title, BorderLayout.WEST);

        bar.add(labeledSearch(), BorderLayout.EAST);
        return bar;
    }

    private JPanel labeledSearch() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BG_DARK);
        JLabel lbl = new JLabel("Tìm kiếm từ khóa");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_WHITE);
        p.add(lbl, BorderLayout.NORTH);

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BG_DARK);

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

        txtSearch = new JTextField(22) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Icon searchIcon = IconUtils.getSearchIcon(14, TEXT_MUTED);
                    searchIcon.paintIcon(this, g2, 8, getHeight() / 2 - 7);
                }
                g2.dispose();
            }
        };
        styleTextField(txtSearch);
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { currentPage = 1; renderPage(); }
        });

        RoundButton btnAdd = new RoundButton("", BTN_ADD, Color.WHITE);
        btnAdd.setIcon(IconUtils.getAddIcon(18, Color.WHITE));
        btnAdd.setPreferredSize(new Dimension(40, 40));
        btnAdd.setToolTipText("Thêm khách hàng");
        btnAdd.addActionListener(e -> showAddCustomerDialog());

        RoundButton btnRefresh = new RoundButton("", INPUT_BG, BG_DARK);
        btnRefresh.setIcon(IconUtils.getRefreshIcon(18, BG_DARK));
        btnRefresh.setPreferredSize(new Dimension(40, 40));
        btnRefresh.setToolTipText("Làm mới");
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

    /* ======================================================
        TABLE
    ====================================================== */
    private JScrollPane buildTable() {
        String[] cols = {"Mã KH", "Họ Tên", "Số Điện Thoại", "CCCD", "Email", "Điểm Tích Lũy"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (c instanceof JLabel)
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                   ((JLabel) c).setBorder(new EmptyBorder(0, 12, 0, 12));
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
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
                return lbl;
            }
        });
        header.setBackground(PURPLE_HEADER);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());

        int[] widths = {80, 200, 120, 120, 180, 120};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(PURPLE_HEADER, 1, true));
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(PURPLE_ALT);

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        // ✅ Lọc theo cột khi click tiêu đề (chỉ hoạt động khi isFilterMode = true)
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!isFilterMode) return;
                int col = table.columnAtPoint(e.getPoint());
                String colName = table.getColumnName(col);
                String filter = JOptionPane.showInputDialog(
                    CustomerPanel.this, "Lọc theo " + colName + ":");
                if (filter != null && !filter.trim().isEmpty())
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + filter.trim(), col));
                else if (filter != null)
                    rowSorter.setRowFilter(null);
            }
        });

        return sp;
    }

    /* ======================================================
        BOTTOM BAR
    ====================================================== */
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(14, 0, 0, 0));

        paginationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        paginationPanel.setBackground(BG_DARK);
        rebuildPagination(1);
        bar.add(paginationPanel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(BG_DARK);

        // ✅ Nút Quản lý điểm
        RoundButton btnDiem = new RoundButton(" Điểm", BTN_POINT, Color.WHITE);
        btnDiem.setPreferredSize(new Dimension(130, 40));
        btnDiem.setToolTipText("Quản lý điểm tích lũy");
        btnDiem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để quản lý điểm!");
                return;
            }
            Customer kh = currentPageData.get(row);
            new CustomerPointDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                kh,
                this::loadData   // callback: reload bảng sau khi thay đổi điểm
            ).setVisible(true);
        });

        RoundButton btnEdit = new RoundButton(" Sửa", BTN_EDIT, BG_DARK);
        btnEdit.setIcon(IconUtils.getEditIcon(16, BG_DARK));
        btnEdit.setPreferredSize(new Dimension(110, 40));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!");
                return;
            }
            showEditCustomerDialog(currentPageData.get(row));
        });

        RoundButton btnDelete = new RoundButton(" Xóa", BTN_DELETE, BG_DARK);
        btnDelete.setIcon(IconUtils.getDeleteIcon(16, BG_DARK));
        btnDelete.setPreferredSize(new Dimension(110, 40));
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để xóa!");
                return;
            }
            Customer kh = currentPageData.get(row);

            // ✅ View tự hỏi confirm, rồi mới gọi controller
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận xóa khách hàng: " + kh.getHoTen() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            ActionResult result = controller.handleDelete(kh.getMaKH());
            JOptionPane.showMessageDialog(this, result.message);
            if (result.success) loadData();
        });

        btnPanel.add(btnDiem);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        bar.add(btnPanel, BorderLayout.EAST);

        return bar;
    }

    private void rebuildPagination(int totalPages) {
        paginationPanel.removeAll();

        RoundButton btnPrev = new RoundButton("<", INPUT_BG, BG_DARK);
        btnPrev.setPreferredSize(new Dimension(40, 36));
        btnPrev.setEnabled(currentPage > 1);
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                renderPage();
            }
        });

        JLabel lblPageInfo = new JLabel("Trang " + currentPage + " / " + totalPages);
        lblPageInfo.setForeground(TEXT_WHITE);
        lblPageInfo.setFont(FONT_LABEL);
        lblPageInfo.setBorder(new EmptyBorder(0, 10, 0, 10));

        RoundButton btnNext = new RoundButton(">", INPUT_BG, BG_DARK);
        btnNext.setPreferredSize(new Dimension(40, 36));
        btnNext.setEnabled(currentPage < totalPages);
        btnNext.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                renderPage();
            }
        });

        paginationPanel.add(btnPrev);
        paginationPanel.add(lblPageInfo);
        paginationPanel.add(btnNext);

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    /* ======================================================
        DATA
    ====================================================== */
    private void loadData() {
        allData = controller.loadAll();
        currentPage = 1;
        renderPage();
    }

    private void renderPage() {
        tableModel.setRowCount(0);

        String keyword = (txtSearch == null)
            ? ""
            : txtSearch.getText().trim();

        CustomerController.PageResult<Customer> r =
            controller.getPage(allData, keyword, currentPage, PAGE_SIZE);

        currentPage = r.currentPage;
        currentPageData = r.data;

        for (Customer kh : r.data) {
            tableModel.addRow(new Object[]{
                "KH" + String.format("%03d", kh.getMaKH()),
                kh.getHoTen(),
                kh.getSdt()   != null ? kh.getSdt()   : "",
                kh.getCccd()  != null ? kh.getCccd()  : "",
                kh.getEmail() != null ? kh.getEmail() : "",
                kh.getDiemTichLuy()
            });
        }

        rebuildPagination(r.totalPages);
    }

    /* ======================================================
        DIALOG: ADD / EDIT
    ====================================================== */
    private void showAddCustomerDialog() {
        JPanel panel = buildCustomerForm(null);
        JTextField[] fields = extractFields(panel);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Thêm Khách Hàng Mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // ✅ View hiện thông báo từ ActionResult
            ActionResult ar = controller.handleAdd(
                fields[0].getText(), fields[1].getText(),
                fields[2].getText(), fields[3].getText(), fields[4].getText()
            );
            JOptionPane.showMessageDialog(this, ar.message);
            if (ar.success) loadData();
        }
    }

    private void showEditCustomerDialog(Customer kh) {
        JPanel panel = buildCustomerForm(kh);
        JTextField[] fields = extractFields(panel);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Cập Nhật Thông Tin Khách Hàng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            ActionResult ar = controller.handleUpdate(kh,
                fields[0].getText(), fields[1].getText(),
                fields[2].getText(), fields[3].getText(), fields[4].getText()
            );
            JOptionPane.showMessageDialog(this, ar.message);
            if (ar.success) loadData();
        }
    }

    /** Tạo form chung cho Add và Edit */
    private JPanel buildCustomerForm(Customer kh) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Họ tên (*):")); panel.add(new JTextField(kh != null ? kh.getHoTen() : ""));
        panel.add(new JLabel("Số điện thoại (*):")); panel.add(new JTextField(kh != null && kh.getSdt()   != null ? kh.getSdt()   : ""));
        panel.add(new JLabel("CCCD (*):")); panel.add(new JTextField(kh != null && kh.getCccd()  != null ? kh.getCccd()  : ""));
        panel.add(new JLabel("Email:")); panel.add(new JTextField(kh != null && kh.getEmail() != null ? kh.getEmail() : ""));
        panel.add(new JLabel("Địa chỉ:")); panel.add(new JTextField(kh != null && kh.getDiaChi() != null ? kh.getDiaChi() : ""));
        return panel;
    }

    /** Lấy 5 JTextField theo thứ tự từ form */
    private JTextField[] extractFields(JPanel panel) {
        JTextField[] result = new JTextField[5];
        int idx = 0;
        for (Component c : panel.getComponents())
            if (c instanceof JTextField) result[idx++] = (JTextField) c;
        return result;
    }

    /* ======================================================
        SORT / FILTER
    ====================================================== */
    private void showSortPopupMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem m1 = new JMenuItem("Mã KH (Tăng dần)");
        m1.addActionListener(e -> executeSort("MaKH", true));
        JMenuItem m2 = new JMenuItem("Mã KH (Giảm dần)");
        m2.addActionListener(e -> executeSort("MaKH", false));
        JMenuItem m3 = new JMenuItem("Điểm tích lũy (Tăng dần)");
        m3.addActionListener(e -> executeSort("Diem", true));
        JMenuItem m4 = new JMenuItem("Điểm tích lũy (Giảm dần)");
        m4.addActionListener(e -> executeSort("Diem", false));
        menu.add(m1); menu.add(m2);
        menu.addSeparator();
        menu.add(m3); menu.add(m4);
        menu.show(invoker, 0, invoker.getHeight());
    }

    private void executeSort(String type, boolean ascending) {
        if (allData == null) return;
        controller.sort(allData, type, ascending);
        currentPage = 1;
        renderPage();
    }

    private void toggleFilterMode() {
        isFilterMode = !isFilterMode;
        if (isFilterMode) {
            btnFilter.setBackground(ACCENT);
            btnFilter.setForeground(Color.WHITE);
            JOptionPane.showMessageDialog(this,
                "Chế độ Lọc ĐÃ BẬT.\nHãy nhấn vào tiêu đề cột (VD: 'Họ Tên') để lọc.");
        } else {
            btnFilter.setBackground(INPUT_BG);
            btnFilter.setForeground(BG_DARK);
            if (rowSorter != null) rowSorter.setRowFilter(null);
        }
    }

    private void showSimpleFilterDialog() {
        String[] options = {"Tất cả", "Khách hàng thân thiết (>= 100 điểm)", "Khách hàng mới (< 100 điểm)"};
        String selection = (String) JOptionPane.showInputDialog(this,
            "Chọn chế độ lọc nhanh:", "Bộ lọc",
            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selection == null) return;

        String mode = selection.contains(">= 100") ? "loyal"
                    : selection.contains("< 100")  ? "new"
                    : "all";
        allData = controller.filterByPointMode(controller.loadAll(), mode);
        currentPage = 1;
        renderPage();
    }
}
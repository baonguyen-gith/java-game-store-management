package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.controller.DiscController;
import otkhongluong.gamestoremanagement.controller.ProductController;
import otkhongluong.gamestoremanagement.controller.RomController;
import otkhongluong.gamestoremanagement.model.Disc;
import otkhongluong.gamestoremanagement.model.Product;
import otkhongluong.gamestoremanagement.model.ROM;
import otkhongluong.gamestoremanagement.util.IconUtils;
import otkhongluong.gamestoremanagement.util.RoundButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * View – chỉ chịu trách nhiệm hiển thị và thu nhận sự kiện.
 * Mọi nghiệp vụ uỷ quyền cho ProductController / RomController / DiscController.
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
    private static final Color BTN_ROM       = new Color(255, 193, 80);   // vàng cam
    private static final Color BTN_DISC      = new Color(100, 200, 220);  // xanh ngọc

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

    /* ============ CONTROLLERS ============ */
    private final ProductController controller;
    private final RomController romController;
    private final DiscController discController;

    /* ============ DATA ============ */
    private List<Product> allData;
    private List<Product> currentPageData;

    // ======================================================
    //  KHỞI TẠO
    // ======================================================
    public ProductPanel() {
        controller     = new ProductController();
        romController  = new RomController();
        discController = new DiscController();

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

        // Nút Sắp xếp & Lọc
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

        // Ô tìm kiếm
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
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                currentPage = 1;
                renderPage();
            }
        });

        // Nút Add & Refresh
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
        String[] cols = {"Mã SP", "Mã Game", "Giá Bán", "Giá Thuê Ngày", "Loại"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return String.class; } // tất cả String
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (c instanceof JLabel) {
                    JLabel lbl = (JLabel) c;
                    lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
                    lbl.setHorizontalAlignment(col >= 4 ? SwingConstants.CENTER : SwingConstants.LEFT);
                    if (col == 4 && !isRowSelected(row)) {
                        String val = (String) tableModel.getValueAt(
                            table.convertRowIndexToModel(row), col);
                        lbl.setForeground(
                            "ROM".equals(val) ? new Color(255, 193, 80)  :   // vàng cam
                            "CD".equals(val)  ? new Color(80, 200, 120)  :   // xanh lá
                            "ROM + CD".equals(val) ? new Color(130, 90, 230) : // tím accent
                                                new Color(160, 160, 160));      // xám --- 
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else if (col < 4) {
                        lbl.setFont(FONT_CELL);
                    }
                }
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? PURPLE_ROW : PURPLE_ALT);
                    if (col < 4) c.setForeground(new Color(40, 40, 40));
                } else {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
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

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString());
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(PURPLE_HEADER);
                lbl.setHorizontalAlignment(c >= 4 ? SwingConstants.CENTER : SwingConstants.LEFT);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
                return lbl;
            }
        });
        header.setBackground(PURPLE_HEADER);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());

        int[] widths = {80, 80, 150, 150, 80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!isFilterMode) return;
                int viewCol = table.columnAtPoint(e.getPoint());
                if (viewCol < 0) return;
                int modelCol = table.convertColumnIndexToModel(viewCol);
                String colName = table.getColumnName(viewCol);
                String input = JOptionPane.showInputDialog(null, "Lọc theo " + colName + ":");
                if (input != null && !input.trim().isEmpty())
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + input.trim(), modelCol));
                else if (input != null)
                    rowSorter.setRowFilter(null);
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
        rebuildPagination(1);
        bar.add(paginationPanel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(BG_DARK);

        // --- Nút Quản lý ROM ---
        RoundButton btnRom = new RoundButton(" ROM", BTN_ROM, BG_DARK);
        btnRom.setPreferredSize(new Dimension(120, 40));
        btnRom.setToolTipText("Quản lý thông tin ROM (bản kỹ thuật số) của sản phẩm");
        btnRom.addActionListener(e -> {
            Product sp = getSelectedProduct();
            if (sp == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần quản lý ROM!");
                return;
            }
            showRomDialog(sp);
        });

        // --- Nút Tồn kho CD ---
        RoundButton btnDisc = new RoundButton(" Tồn kho CD", BTN_DISC, BG_DARK);
        btnDisc.setPreferredSize(new Dimension(130, 40));
        btnDisc.setToolTipText("Quản lý tồn kho đĩa CD của sản phẩm");
        btnDisc.addActionListener(e -> {
            Product sp = getSelectedProduct();
            if (sp == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần quản lý tồn kho CD!");
                return;
            }
            showDiscDialog(sp);
        });

        // --- Nút Sửa ---
        RoundButton btnEdit = new RoundButton(" Sửa", BTN_EDIT, BG_DARK);
        btnEdit.setIcon(IconUtils.getEditIcon(16, BG_DARK));
        btnEdit.setPreferredSize(new Dimension(110, 40));
        btnEdit.addActionListener(e -> {
            Product sp = getSelectedProduct();
            if (sp == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần sửa!");
                return;
            }
            showEditProductDialog(sp);
        });

        // --- Nút Xóa ---
        RoundButton btnDelete = new RoundButton(" Xóa", BTN_DELETE, BG_DARK);
        btnDelete.setIcon(IconUtils.getDeleteIcon(16, BG_DARK));
        btnDelete.setPreferredSize(new Dimension(110, 40));
        btnDelete.addActionListener(e -> {
            Product sp = getSelectedProduct();
            if (sp == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để xóa!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận xóa sản phẩm mã: SP" + String.format("%03d", sp.getMaSP()) + "?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            ProductController.ActionResult result = controller.handleDelete(sp.getMaSP());
            JOptionPane.showMessageDialog(
                    this, result.message,
                    result.success ? "Thành công" : "Lỗi",
                    result.success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
            );
            if (result.success) loadData();
        });

        btnPanel.add(btnRom);
        btnPanel.add(btnDisc);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        bar.add(btnPanel, BorderLayout.EAST);
        return bar;
    }

    /** Lấy Product đang được chọn trên bảng, null nếu chưa chọn. */
    private Product getSelectedProduct() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return currentPageData.get(modelRow);
    }

    private void rebuildPagination(int totalPages) {
        paginationPanel.removeAll();

        RoundButton btnPrev = new RoundButton("<", INPUT_BG, BG_DARK);
        btnPrev.setPreferredSize(new Dimension(40, 36));
        btnPrev.setEnabled(currentPage > 1);
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) { currentPage--; renderPage(); }
        });

        JLabel lblPageInfo = new JLabel("Trang " + currentPage + " / " + totalPages);
        lblPageInfo.setForeground(TEXT_WHITE);
        lblPageInfo.setFont(FONT_LABEL);
        lblPageInfo.setBorder(new EmptyBorder(0, 10, 0, 10));

        RoundButton btnNext = new RoundButton(">", INPUT_BG, BG_DARK);
        btnNext.setPreferredSize(new Dimension(40, 36));
        btnNext.setEnabled(currentPage < totalPages);
        btnNext.addActionListener(e -> {
            if (currentPage < totalPages) { currentPage++; renderPage(); }
        });

        paginationPanel.add(btnPrev);
        paginationPanel.add(lblPageInfo);
        paginationPanel.add(btnNext);
        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    // ======================================================
    //  DATA
    // ======================================================
    private void loadData() {
            allData = controller.loadAll();
            renderPage();
        }

    private void renderPage() {
        tableModel.setRowCount(0);
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();
        ProductController.PageResult result =
            controller.getPage(allData, keyword, currentPage, PAGE_SIZE);

        currentPage     = result.currentPage;
        currentPageData = result.data;

        for (Product sp : currentPageData) {
            String loai;
            if (sp.isHasRom() && sp.isHasCd())  loai = "ROM + CD";
            else if (sp.isHasRom())             loai = "ROM";
            else if (sp.isHasCd())              loai = "CD";
            else                                loai = "---";

            tableModel.addRow(new Object[]{
                "SP" + String.format("%03d", sp.getMaSP()),
                "G"  + String.format("%03d", sp.getMaGame()),
                String.format("%,.0f đ", sp.getGiaBan()),
                String.format("%,.0f đ", sp.getGiaThueNgay()),
                loai
            });
        }
        rebuildPagination(result.totalPages);
    }

    // ======================================================
    //  DIALOG: THÊM / SỬA SẢN PHẨM
    // ======================================================
    private void showAddProductDialog() {
        // Bước 1: chọn loại
        String[] loaiOptions = {"ROM (Bản kỹ thuật số)", "CD (Đĩa vật lý)"};
        int loaiChoice = JOptionPane.showOptionDialog(
            this,
            "Chọn loại sản phẩm muốn thêm:",
            "Thêm Sản Phẩm Mới",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, loaiOptions, loaiOptions[0]
        );
        if (loaiChoice < 0) return; // bấm X
        boolean isRom = (loaiChoice == 0);

        // Bước 2: form nhập liệu
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtMaGame  = new JTextField();
        JTextField txtGiaBan  = new JTextField("0");
        JTextField txtGiaThue = new JTextField("0");

        panel.add(new JLabel("Mã Game liên kết (*):"));
        panel.add(txtMaGame);

        if (isRom) {
            panel.add(new JLabel("Giá Bán ROM - VNĐ (*):"));
            panel.add(txtGiaBan);
        } else {
            panel.add(new JLabel("Giá Bán CD - VNĐ (để trống nếu không bán):"));
            panel.add(txtGiaBan);
            panel.add(new JLabel("Giá Thuê CD/ngày - VNĐ (để trống nếu không cho thuê):"));
            panel.add(txtGiaThue);
        }

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Thêm " + (isRom ? "ROM" : "CD"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            ProductController.ActionResult actionResult = controller.handleAdd(
                isRom ? "ROM" : "CD",
                txtMaGame.getText(),
                txtGiaBan.getText(),
                isRom ? "0" : txtGiaThue.getText()
            );
            showResult(actionResult);
            if (actionResult.success) loadData();
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
                "Cập Nhật Sản Phẩm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            ProductController.ActionResult actionResult =
                    controller.handleUpdate(sp, txtMaGame.getText(), txtGiaBan.getText(), txtGiaThue.getText());
            showResult(actionResult);
            if (actionResult.success) loadData();
        }
    }

    // ======================================================
    //  DIALOG: QUẢN LÝ ROM
    // ======================================================
    private void showRomDialog(Product sp) {
        String maSPLabel = "SP" + String.format("%03d", sp.getMaSP());

        // Load ROM hiện tại (nếu có)
        ROM existing = romController.loadRom(sp.getMaSP());

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 8));

        JTextField txtDungLuong  = new JTextField(existing != null ? existing.getDungLuong()  : "");
        JTextField txtLink       = new JTextField(existing != null ? existing.getLinkLuuTru() : "");
        JTextField txtSoLuotBan  = new JTextField(existing != null ? String.valueOf(existing.getSoLuotBan()) : "0");
        txtSoLuotBan.setEditable(false); // Số lượt bán chỉ đọc, không nhập tay

        panel.add(new JLabel("Sản phẩm: " + maSPLabel));
        panel.add(new JSeparator());
        panel.add(new JLabel("Dung lượng (*):"));   panel.add(txtDungLuong);
        panel.add(new JLabel("Link lưu trữ (*):"));  panel.add(txtLink);
        panel.add(new JLabel("Số lượt bán:"));       panel.add(txtSoLuotBan);

        // Tuỳ chỉnh nút: nếu đã có ROM thì hiện thêm nút Xóa ROM
        Object[] options = existing != null
                ? new Object[]{"Lưu", "Xóa ROM", "Hủy"}
                : new Object[]{"Lưu", "Hủy"};

        int choice = JOptionPane.showOptionDialog(
                this, panel,
                "Quản lý ROM – " + maSPLabel,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            // Lưu (insert hoặc update)
            ProductController.ActionResult result = romController.handleSave(
                    sp.getMaSP(),
                    txtDungLuong.getText(),
                    txtLink.getText(),
                    txtSoLuotBan.getText()
            );
            showResult(result);
        } else if (choice == 1 && existing != null) {
            // Xóa ROM
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Xác nhận xóa ROM của " + maSPLabel + "?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                showResult(romController.handleDelete(sp.getMaSP()));
            }
        }
    }

    // ======================================================
    //  DIALOG: QUẢN LÝ TỒN KHO CD
    // ======================================================
    private void showDiscDialog(Product sp) {
        String maSPLabel = "SP" + String.format("%03d", sp.getMaSP());

        // Tạo JDialog để có thể refresh danh sách bên trong
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Tồn kho CD – " + maSPLabel, true);
        dialog.setSize(560, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getRootPane().setBorder(new EmptyBorder(12, 12, 12, 12));

        // --- Header thông tin tồn kho ---
        JLabel lblInfo = buildDiscInfoLabel(sp.getMaSP());
        dialog.add(lblInfo, BorderLayout.NORTH);

        // --- Bảng danh sách đĩa ---
        String[] cols = {"Mã CD", "Tình Trạng", "Trạng Thái"};
        DefaultTableModel discModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable discTable = new JTable(discModel);
        discTable.setRowHeight(32);
        discTable.setFont(FONT_CELL);
        discTable.getTableHeader().setFont(FONT_HEADER);
        discTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(discTable);
        dialog.add(scroll, BorderLayout.CENTER);

        // Hàm nội bộ để load lại bảng đĩa
        Runnable refreshDiscTable = () -> {
            discModel.setRowCount(0);
            List<Disc> discs = discController.loadByMaSP(sp.getMaSP());
            for (Disc d : discs) {
                discModel.addRow(new Object[]{
                    "CD" + String.format("%03d", d.getMaCD()),
                    d.getTinhTrang(),
                    d.getTrangThai()
                });
            }
            // Cập nhật label tồn kho
            lblInfo.setText(buildDiscInfoText(sp.getMaSP()));
        };

        refreshDiscTable.run();

        // --- Panel nút hành động ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        // Nút Thêm đĩa
        RoundButton btnThem = new RoundButton("+ Thêm đĩa", BTN_ADD, Color.WHITE);
        btnThem.setPreferredSize(new Dimension(120, 36));
        btnThem.addActionListener(e -> {
            JTextField txtSoLuong = new JTextField("1", 6);
            JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            inputPanel.add(new JLabel("Số lượng nhập kho:"));
            inputPanel.add(txtSoLuong);
            inputPanel.add(new JLabel("(Tình trạng mặc định: Mới)"));

            int choice = JOptionPane.showConfirmDialog(dialog, inputPanel,
                    "Thêm đĩa CD – " + maSPLabel,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (choice == JOptionPane.OK_OPTION) {
                ProductController.ActionResult result =
                        discController.handleThemNhieuDia(sp.getMaSP(), txtSoLuong.getText());
                showResult(result);
                if (result.success) refreshDiscTable.run();
            }
        });

        // Nút Sửa tình trạng
        RoundButton btnSuaTinhTrang = new RoundButton("Sửa tình trạng", BTN_EDIT, BG_DARK);
        btnSuaTinhTrang.setPreferredSize(new Dimension(140, 36));
        btnSuaTinhTrang.addActionListener(e -> {
            int viewRow = discTable.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn đĩa cần sửa!");
                return;
            }
            String maCDStr = discModel.getValueAt(viewRow, 0).toString().replaceAll("[^\\d]", "");
            int maCD = Integer.parseInt(maCDStr);
            String tinhTrangCu = discModel.getValueAt(viewRow, 1).toString();

            // >>> BẮT ĐẦU ĐOẠN MỚI >>>
            String[] options = {"Mới", "Tốt", "Cũ", "Hỏng"};
            JComboBox<String> comboTinhTrang = new JComboBox<>(options);
            comboTinhTrang.setSelectedItem(tinhTrangCu);

            JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
            inputPanel.add(new JLabel("Chọn tình trạng mới:"), BorderLayout.NORTH);
            inputPanel.add(comboTinhTrang, BorderLayout.CENTER);

            UIManager.put("OptionPane.okButtonText", "Xác nhận");
            UIManager.put("OptionPane.cancelButtonText", "Hủy");

            int choice = JOptionPane.showConfirmDialog(
                    dialog, inputPanel,
                    "Sửa tình trạng – CD" + String.format("%03d", maCD),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            UIManager.put("OptionPane.okButtonText", "OK");
            UIManager.put("OptionPane.cancelButtonText", "Cancel");

            if (choice == JOptionPane.OK_OPTION) {
                String tinhTrangMoi = (String) comboTinhTrang.getSelectedItem();
                ProductController.ActionResult result =
                        discController.handleCapNhatTinhTrang(maCD, tinhTrangMoi);
                showResult(result);
                if (result.success) refreshDiscTable.run();
            }
            // <<< KẾT THÚC ĐOẠN MỚI <
        });

        // Nút Xóa đĩa
        RoundButton btnXoa = new RoundButton("Xóa đĩa", BTN_DELETE, BG_DARK);
        btnXoa.setPreferredSize(new Dimension(100, 36));
        btnXoa.addActionListener(e -> {
            int viewRow = discTable.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn đĩa cần xóa!");
                return;
            }
            String maCDStr = discModel.getValueAt(viewRow, 0).toString().replaceAll("[^\\d]", "");
            int maCD = Integer.parseInt(maCDStr);
            String trangThai = discModel.getValueAt(viewRow, 2).toString();

            if (!"SanSang".equals(trangThai)) {
                JOptionPane.showMessageDialog(dialog,
                        "Chỉ có thể xóa đĩa đang ở trạng thái Sẵn Sàng!\n" +
                        "Đĩa này đang ở trạng thái: " + trangThai,
                        "Không thể xóa", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Xác nhận xóa đĩa CD" + String.format("%03d", maCD) + "?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                ProductController.ActionResult result = discController.handleXoaDia(maCD);
                showResult(result);
                if (result.success) refreshDiscTable.run();
            }
        });

        // Nút Đóng
        RoundButton btnDong = new RoundButton("Đóng", INPUT_BG, BG_DARK);
        btnDong.setPreferredSize(new Dimension(90, 36));
        btnDong.addActionListener(e -> dialog.dispose());

        actionPanel.add(btnThem);
        actionPanel.add(btnSuaTinhTrang);
        actionPanel.add(btnXoa);
        actionPanel.add(btnDong);
        dialog.add(actionPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /** Tạo label tóm tắt tồn kho. */
    private JLabel buildDiscInfoLabel(int maSP) {
        JLabel lbl = new JLabel(buildDiscInfoText(maSP));
        lbl.setFont(FONT_LABEL);
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        return lbl;
    }

    private String buildDiscInfoText(int maSP) {
        int tongTon  = discController.getTongTon(maSP);
        int sanSang  = discController.getSanSang(maSP);
        int dangThue = tongTon - sanSang;
        return String.format(
                "Tổng tồn kho: %d đĩa  |  Sẵn sàng: %d  |  Đang thuê / Hỏng: %d",
                tongTon, sanSang, dangThue
        );
    }

    // ======================================================
    //  SORT / FILTER
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
        asc.addActionListener(e  -> { controller.sort(allData, type, true);  currentPage = 1; renderPage(); });
        desc.addActionListener(e -> { controller.sort(allData, type, false); currentPage = 1; renderPage(); });
        menu.add(asc);
        menu.add(desc);
        menu.addSeparator();
    }

    // ======================================================
    //  HELPER
    // ======================================================
    /** Hiện thông báo kết quả từ ActionResult. */
    private void showResult(ProductController.ActionResult result) {
        JOptionPane.showMessageDialog(
                this,
                result.message,
                result.success ? "Thành công" : "Lỗi",
                result.success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
        );
    }
}
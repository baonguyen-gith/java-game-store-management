package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.SanPham;
import otkhongluong.gamestoremanagement.service.SanPhamService;
import otkhongluong.gamestoremanagement.util.IconUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

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
    private RoundButton btnSort, btnFilter; 
    private boolean isFilterMode = false;   
    private TableRowSorter<DefaultTableModel> rowSorter;

    private SanPhamService service = new SanPhamService();
    private List<SanPham> allData;
    private List<SanPham> currentPageData;

    public ProductPanel() {
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

        JLabel title = new JLabel("QUẢN LÝ SẢN PHẨM");
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
        lbl.setFont(FONT_LABEL); lbl.setForeground(TEXT_WHITE);
        p.add(lbl, BorderLayout.NORTH);

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BG_DARK);

        // --- Cụm nút Lọc & Sắp xếp bên TRÁI ---
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftGroup.setBackground(BG_DARK);

        btnSort = new RoundButton("Sắp xếp", INPUT_BG, BG_DARK);
        btnSort.setPreferredSize(new Dimension(90, 40));
        btnSort.addActionListener(e -> showSortPopupMenu(btnSort));

        btnFilter = new RoundButton("Lọc", INPUT_BG, BG_DARK);
        btnFilter.setPreferredSize(new Dimension(70, 40));
        btnFilter.addActionListener(e -> toggleFilterMode());

        leftGroup.add(btnSort);
        leftGroup.add(btnFilter);

        // --- Ô tìm kiếm ở giữa ---
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

        // --- Cụm nút bên PHẢI (Add, Refresh) ---
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
        btnGroup.add(btnAdd); btnGroup.add(btnRefresh);

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
        String[] cols = {"Mã SP", "Mã Game", "Giá Bán", "Giá Thuê Ngày"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
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

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(PURPLE_HEADER, 1, true));
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(PURPLE_ALT);

        // Khởi tạo bộ lọc bảng
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        // Sự kiện click tiêu đề cột để lọc
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isFilterMode) {
                    int viewCol = table.columnAtPoint(e.getPoint());
                    if (viewCol == -1) return;
                    int modelCol = table.convertColumnIndexToModel(viewCol);
                    String colName = table.getColumnName(viewCol);
                    
                    String filter = JOptionPane.showInputDialog(null, "Lọc theo " + colName + ":");
                    if (filter != null && !filter.trim().isEmpty()) {
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + filter.trim(), modelCol));
                    } else if (filter != null) {
                        rowSorter.setRowFilter(null);
                    }
                }
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
        rebuildPagination(paginationPanel);
        bar.add(paginationPanel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(BG_DARK);

        // Tìm đoạn btnEdit trong hàm buildBottomBar()
        RoundButton btnEdit = new RoundButton(" Sửa", BTN_EDIT, BG_DARK);
        btnEdit.setIcon(IconUtils.getEditIcon(16, BG_DARK));
        btnEdit.setPreferredSize(new Dimension(110, 40));
        btnEdit.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) { 
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần sửa!"); 
                return; 
            }
            
            // Lấy dữ liệu sản phẩm từ hàng đang chọn (xử lý chính xác cả khi đang lọc/sắp xếp)
            int modelRow = table.convertRowIndexToModel(viewRow);
            SanPham selected = currentPageData.get(modelRow);
            
            // Gọi hàm mở Dialog sửa
            showEditProductDialog(selected); 
        });

        // Tìm đoạn btnDelete trong hàm buildBottomBar()
        RoundButton btnDelete = new RoundButton(" Xóa", BTN_DELETE, BG_DARK);
        btnDelete.setIcon(IconUtils.getDeleteIcon(16, BG_DARK));
        btnDelete.setPreferredSize(new Dimension(110, 40));
        btnDelete.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) { 
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm để xóa!"); 
                return; 
            }
            
            // Chuyển đổi chỉ số hàng từ giao diện sang model (quan trọng khi đang lọc/sắp xếp)
            int modelRow = table.convertRowIndexToModel(viewRow);
            SanPham sp = currentPageData.get(modelRow);
            
            // Hiện hộp thoại xác nhận
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Xác nhận xóa sản phẩm mã: SP" + String.format("%03d", sp.getMaSP()) + "?", 
                "Xác nhận", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                // Gọi Service để xóa dưới Database
                if (service.deleteSanPham(sp.getMaSP())) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadData(); // Tải lại bảng để cập nhật danh sách
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi: Không thể xóa sản phẩm này (có thể nó đang nằm trong một hóa đơn cũ).");
                }
            }
        });

        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);
        bar.add(btnPanel, BorderLayout.EAST);

        return bar;
    }

    private void rebuildPagination(JPanel panel) {
        panel.removeAll();
        List<SanPham> filtered = getFilteredData();
        final int total = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));

        // Nút Quay lại <
        RoundButton btnPrev = new RoundButton("<", INPUT_BG, BG_DARK);
        btnPrev.setPreferredSize(new Dimension(40, 36));
        btnPrev.setEnabled(currentPage > 1);
        btnPrev.addActionListener(e -> { 
            if (currentPage > 1) { 
                currentPage--; 
                renderPage(); 
            } 
        });

        // Nhãn hiển thị Trang X / Y
        JLabel lblPageInfo = new JLabel("Trang " + currentPage + " / " + total);
        lblPageInfo.setForeground(TEXT_WHITE);
        lblPageInfo.setFont(FONT_LABEL);
        lblPageInfo.setBorder(new EmptyBorder(0, 10, 0, 10));

        // Nút Tiếp theo >
        RoundButton btnNext = new RoundButton(">", INPUT_BG, BG_DARK);
        btnNext.setPreferredSize(new Dimension(40, 36));
        btnNext.setEnabled(currentPage < total);
        btnNext.addActionListener(e -> { 
            if (currentPage < total) { 
                currentPage++; 
                renderPage(); 
            } 
        });

        panel.add(btnPrev);
        panel.add(lblPageInfo);
        panel.add(btnNext);

        panel.revalidate();
        panel.repaint();
    }

    /* ======================================================
        DATA
    ====================================================== */
    private void loadData() {
        allData = service.getAllSanPham();
        renderPage();
    }
    
    private List<SanPham> getFilteredData() {
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();

        return allData == null ? java.util.Collections.emptyList() :
            allData.stream()
                .filter(sp -> {
                    if (keyword.isEmpty()) return true;
                    
                    // 1. Mã SP (VD: "SP001" và "1")
                    String maSPStr = "sp" + String.format("%03d", sp.getMaSP());
                    String maSPRaw = String.valueOf(sp.getMaSP());
                    
                    // 2. Mã Game (VD: "G005" và "5")
                    String maGameStr = "g" + String.format("%03d", sp.getMaGame());
                    String maGameRaw = String.valueOf(sp.getMaGame());
                    
                    // 3. Giá tiền (dạng số thuần túy)
                    String giaBan = String.valueOf(sp.getGiaBan());
                    String giaThue = String.valueOf(sp.getGiaThueNgay());
                    
                    return maSPStr.contains(keyword) || maSPRaw.contains(keyword)
                        || maGameStr.contains(keyword) || maGameRaw.contains(keyword)
                        || giaBan.contains(keyword) || giaThue.contains(keyword);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private void renderPage() {
        tableModel.setRowCount(0);
        List<SanPham> filtered = getFilteredData();

        int totalPage = (int) Math.ceil((double) filtered.size() / PAGE_SIZE);
        if (currentPage > totalPage) currentPage = totalPage == 0 ? 1 : totalPage;

        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filtered.size());

        currentPageData = filtered.subList(from, to);

        for (SanPham sp : currentPageData) {
            tableModel.addRow(new Object[]{
                "SP" + String.format("%03d", sp.getMaSP()),
                "G" + String.format("%03d", sp.getMaGame()),
                String.format("%,.0f đ", sp.getGiaBan()),
                String.format("%,.0f đ", sp.getGiaThueNgay())
            });
        }

        rebuildPagination(paginationPanel);
    }

    /* ======================================================
        INNER: RoundButton
    ====================================================== */
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
            g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    private void showAddProductDialog() {
        // Tạo Panel nhập liệu đơn giản
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        JTextField txtMaGame = new JTextField();
        JTextField txtGiaBan = new JTextField("0");
        JTextField txtGiaThue = new JTextField("0");

        panel.add(new JLabel("Mã Game liên kết (*):")); 
        panel.add(txtMaGame);
        panel.add(new JLabel("Giá Bán (ROM) - VNĐ:")); 
        panel.add(txtGiaBan);
        panel.add(new JLabel("Giá Thuê (CD) - VNĐ:")); 
        panel.add(txtGiaThue);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Thêm Sản Phẩm Mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Kiểm tra trống
                if (txtMaGame.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã Game!");
                    return;
                }

                // Khởi tạo đối tượng Sản phẩm
                SanPham sp = new SanPham();
                sp.setMaGame(Integer.parseInt(txtMaGame.getText().trim()));
                sp.setGiaBan(Double.parseDouble(txtGiaBan.getText().trim()));
                sp.setGiaThueNgay(Double.parseDouble(txtGiaThue.getText().trim()));

                // Gọi Service lưu vào DB
                if (service.addSanPham(sp)) {
                    JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!");
                    loadData(); // Tải lại bảng
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy Mã Game này hoặc lỗi kết nối!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Mã Game và Giá tiền phải là con số!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage());
            }
        }
    }

    private void showEditProductDialog(SanPham sp) {
        // Tạo Panel chứa các ô nhập liệu và đổ dữ liệu hiện tại vào
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        JTextField txtMaGame = new JTextField(String.valueOf(sp.getMaGame()));
        JTextField txtGiaBan = new JTextField(String.valueOf(sp.getGiaBan()));
        JTextField txtGiaThue = new JTextField(String.valueOf(sp.getGiaThueNgay()));

        panel.add(new JLabel("Mã Game liên kết (*):")); 
        panel.add(txtMaGame);
        panel.add(new JLabel("Giá Bán (ROM) - VNĐ:")); 
        panel.add(txtGiaBan);
        panel.add(new JLabel("Giá Thuê (CD) - VNĐ:")); 
        panel.add(txtGiaThue);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Cập Nhật Sản Phẩm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Cập nhật thông tin vào đối tượng
                sp.setMaGame(Integer.parseInt(txtMaGame.getText().trim()));
                sp.setGiaBan(Double.parseDouble(txtGiaBan.getText().trim()));
                sp.setGiaThueNgay(Double.parseDouble(txtGiaThue.getText().trim()));

                // Gọi Service để lưu xuống Database
                if (service.updateSanPham(sp)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    loadData(); // Tải lại bảng để thấy thay đổi
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại! Vui lòng kiểm tra lại Mã Game.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Dữ liệu nhập vào phải là con số hợp lệ!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        }
    }

    private void toggleFilterMode() {
        isFilterMode = !isFilterMode;
        if (isFilterMode) {
            btnFilter.setBackground(ACCENT);
            btnFilter.setForeground(Color.WHITE);
            JOptionPane.showMessageDialog(this, "Chế độ Lọc ĐÃ BẬT.\nHãy nhấn vào tên cột trên bảng để lọc.");
        } else {
            btnFilter.setBackground(INPUT_BG);
            btnFilter.setForeground(BG_DARK);
            rowSorter.setRowFilter(null);
        }
    }

    private void showSortPopupMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        
        addSortItem(menu, "Mã Sản Phẩm", "MaSP");
        addSortItem(menu, "Mã Game", "MaGame");
        addSortItem(menu, "Giá Bán", "GiaBan");
        addSortItem(menu, "Giá Thuê", "GiaThue");

        menu.show(invoker, 0, invoker.getHeight());
    }

    private void addSortItem(JPopupMenu menu, String label, String type) {
        JMenuItem asc = new JMenuItem(label + " (Thấp -> Cao)");
        asc.addActionListener(e -> executeSort(type, true));
        JMenuItem desc = new JMenuItem(label + " (Cao -> Thấp)");
        desc.addActionListener(e -> executeSort(type, false));
        menu.add(asc);
        menu.add(desc);
        menu.addSeparator();
    }

    private void executeSort(String type, boolean ascending) {
        if (allData == null) return;
        allData.sort((s1, s2) -> {
            int res = 0;
            switch (type) {
                case "MaSP": res = Integer.compare(s1.getMaSP(), s2.getMaSP()); break;
                case "MaGame": res = Integer.compare(s1.getMaGame(), s2.getMaGame()); break;
                case "GiaBan": res = Double.compare(s1.getGiaBan(), s2.getGiaBan()); break;
                case "GiaThue": res = Double.compare(s1.getGiaThueNgay(), s2.getGiaThueNgay()); break;
            }
            return ascending ? res : -res;
        });
        currentPage = 1;
        renderPage();
    }
}
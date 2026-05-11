package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.service.GameService;
import otkhongluong.gamestoremanagement.util.IconUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class GameManagePanel extends JPanel {

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
    private RoundButton btnSort;
    private RoundButton btnFilter;
    private boolean isFilterMode = false;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private GameService service = new GameService();
    private List<Game> allData;
    private List<Game> currentPageData;

    public GameManagePanel() {
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

        JLabel title = new JLabel("QUẢN LÝ GAME");
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

        // --- Cụm nút bên TRÁI ô tìm kiếm (MỚI) ---
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftGroup.setBackground(BG_DARK);

        btnSort = new RoundButton("Sắp xếp", INPUT_BG, BG_DARK);
        btnSort.setPreferredSize(new Dimension(90, 40));
        btnSort.addActionListener(e -> showSortPopupMenu(btnSort));

        btnFilter = new RoundButton("Lọc", INPUT_BG, BG_DARK);
        btnFilter.setPreferredSize(new Dimension(70, 40));
        btnFilter.addActionListener(e -> {
            isFilterMode = !isFilterMode;
            if (isFilterMode) {
                btnFilter.setBackground(ACCENT);
                btnFilter.setForeground(Color.WHITE);
                JOptionPane.showMessageDialog(this, "Chế độ Lọc ĐÃ BẬT.\nHãy nhấn vào tiêu đề cột để tiến hành lọc.");
            } else {
                btnFilter.setBackground(INPUT_BG);
                btnFilter.setForeground(BG_DARK);
                rowSorter.setRowFilter(null);
            }
        });

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
        btnAdd.setToolTipText("Thêm Game");
        btnAdd.addActionListener(e -> showAddGameDialog());

        RoundButton btnRefresh = new RoundButton("", INPUT_BG, BG_DARK);
        btnRefresh.setIcon(IconUtils.getRefreshIcon(18, BG_DARK));
        btnRefresh.setPreferredSize(new Dimension(40, 40));
        btnRefresh.setToolTipText("Làm mới");
        btnRefresh.addActionListener(e -> loadData());

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setBackground(BG_DARK);
        btnGroup.add(btnAdd);
        btnGroup.add(btnRefresh);

        // Ráp vào row: Trái (Sắp xếp/Lọc) - Giữa (Search) - Phải (Thêm/Refresh)
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
        String[] cols = {"Mã Game", "Tên Game", "Thể Loại", "Nền Tảng", "Giá CD", "Giá ROM", "Giá Thuê"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override 
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
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

        // --- PHẦN QUAN TRỌNG: Kích hoạt bộ lọc và sự kiện click tiêu đề ---
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isFilterMode) {
                    int viewColumn = table.columnAtPoint(e.getPoint());
                    if (viewColumn == -1) return;
                    
                    int modelColumn = table.convertColumnIndexToModel(viewColumn);
                    String colName = table.getColumnName(viewColumn);
                    
                    String filter = JOptionPane.showInputDialog(null, 
                        "Nhập từ khóa để lọc cột [" + colName + "]:");
                    
                    if (filter != null && !filter.trim().isEmpty()) {
                        // Lọc không phân biệt hoa thường (?i)
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + filter.trim(), modelColumn));
                    } else if (filter != null) {
                        // Nếu nhấn OK mà để trống thì xóa lọc của cột đó
                        rowSorter.setRowFilter(null);
                    }
                }
            }
        });
        // -----------------------------------------------------------------

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
            @Override 
            public Component getTableCellRendererComponent(
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

        int[] widths = {80, 180, 100, 100, 100, 100, 120}; 
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(PURPLE_HEADER, 1, true));
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(PURPLE_ALT);
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

        RoundButton btnEdit = new RoundButton(" Sửa", BTN_EDIT, BG_DARK);
        btnEdit.setIcon(IconUtils.getEditIcon(16, BG_DARK));
        btnEdit.setPreferredSize(new Dimension(110, 40));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { 
                JOptionPane.showMessageDialog(this, "Vui lòng chọn game cần sửa!"); 
                return; 
            }
            // Lấy game từ danh sách dữ liệu hiện tại của trang
            Game selectedGame = currentPageData.get(row);
            // GỌI HÀM NÀY ĐỂ MỞ DIALOG (Đổi tên hàm cho đúng thẻ Game)
            showEditGameDialog(selectedGame); 
        });

        RoundButton btnDelete = new RoundButton(" Xóa", BTN_DELETE, BG_DARK);
        btnDelete.setIcon(IconUtils.getDeleteIcon(16, BG_DARK));
        btnDelete.setPreferredSize(new Dimension(110, 40));
        btnDelete.addActionListener(e -> {
            int viewRow = table.getSelectedRow(); // Dòng đang chọn trên màn hình
            if (viewRow < 0) { 
                JOptionPane.showMessageDialog(this, "Vui lòng chọn game để xóa!"); 
                return; 
            }
            
            // Chuyển chỉ số từ giao diện sang chỉ số thực tế trong Model
            int modelRow = table.convertRowIndexToModel(viewRow);
            Game g = currentPageData.get(modelRow);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Xác nhận xóa game: " + g.getTenGame() + "?", 
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (service.deleteGame(g.getMaGame())) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadData(); // Tải lại bảng để cập nhật danh sách mới
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Lỗi khi xóa! Game này có thể đang tồn tại trong Hóa đơn hoặc Phiếu thuê.");
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
        List<Game> filtered = getFilteredData();
        final int total = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));

        // Nút Quay lại <
        RoundButton btnPrev = new RoundButton("<", INPUT_BG, BG_DARK);
        btnPrev.setPreferredSize(new Dimension(40, 36));
        btnPrev.setEnabled(currentPage > 1);
        btnPrev.addActionListener(e -> { 
            if (currentPage > 1) { currentPage--; renderPage(); } 
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
            if (currentPage < total) { currentPage++; renderPage(); } 
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
        allData = service.getAllGames();
        renderPage();
    }
    
    private List<Game> getFilteredData() {
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();

        return allData == null ? java.util.Collections.emptyList() :
            allData.stream()
                .filter(g -> {
                    if (keyword.isEmpty()) return true;
                    
                    String maFull = "g" + String.format("%03d", g.getMaGame());
                    String maSo = String.valueOf(g.getMaGame());
                    
                    return g.getTenGame().toLowerCase().contains(keyword)
                        || (g.getTheLoai() != null && g.getTheLoai().toLowerCase().contains(keyword))
                        || (g.getNenTang() != null && g.getNenTang().toLowerCase().contains(keyword)) // THÊM DÒNG NÀY
                        || maFull.contains(keyword)
                        || maSo.contains(keyword);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private void showAddGameDialog() {
        // Tạo Panel chứa các ô nhập liệu
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        JTextField txtTen = new JTextField();
        JTextField txtTheLoai = new JTextField();
        JTextField txtNenTang = new JTextField();
        JTextField txtGhiChu = new JTextField();
        JTextField txtHinhAnh = new JTextField();
        JTextField txtGiaCD = new JTextField("0");
        JTextField txtGiaROM = new JTextField("0");
        JTextField txtGiaThue = new JTextField("0");

        panel.add(new JLabel("Tên Game (*):")); panel.add(txtTen);
        panel.add(new JLabel("Thể Loại (*):")); panel.add(txtTheLoai);
        panel.add(new JLabel("Nền Tảng (*):")); panel.add(txtNenTang);
        panel.add(new JLabel("Ghi Chú:")); panel.add(txtGhiChu);
        panel.add(new JLabel("Link Hình Ảnh:")); panel.add(txtHinhAnh);
        panel.add(new JLabel("Giá CD (VNĐ):")); panel.add(txtGiaCD);
        panel.add(new JLabel("Giá ROM (VNĐ):")); panel.add(txtGiaROM);
        panel.add(new JLabel("Giá Thuê/Ngày (VNĐ):")); panel.add(txtGiaThue);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Thêm Game Mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Khởi tạo đối tượng Game và gán dữ liệu
                Game g = new Game();
                g.setTenGame(txtTen.getText().trim());
                g.setTheLoai(txtTheLoai.getText().trim());
                g.setNenTang(txtNenTang.getText().trim());
                g.setGhiChu(txtGhiChu.getText().trim());
                g.setHinhAnh(txtHinhAnh.getText().trim());
                
                // Chuyển đổi dữ liệu số cho các loại giá
                g.setGiaCD(Double.parseDouble(txtGiaCD.getText().trim()));
                g.setGiaROM(Double.parseDouble(txtGiaROM.getText().trim()));
                g.setGiaThueNgay(Double.parseDouble(txtGiaThue.getText().trim()));

                // Gọi Service để lưu vào Database
                if (service.addGame(g)) {
                    JOptionPane.showMessageDialog(this, "Thêm game thành công!");
                    loadData(); // Tải lại bảng để cập nhật danh sách
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm thất bại! Vui lòng kiểm tra lại kết nối Database.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Giá tiền phải là con số!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage());
            }
        }
    }

    

    private void renderPage() {
        tableModel.setRowCount(0);
        List<Game> filtered = getFilteredData();

        int totalPage = (int) Math.ceil((double) filtered.size() / PAGE_SIZE);
        if (currentPage > totalPage) currentPage = totalPage == 0 ? 1 : totalPage;

        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filtered.size());

        currentPageData = filtered.subList(from, to);

        for (Game g : currentPageData) {
            tableModel.addRow(new Object[]{
                "G" + String.format("%03d", g.getMaGame()),
                g.getTenGame(),
                g.getTheLoai() != null ? g.getTheLoai() : "",
                g.getNenTang() != null ? g.getNenTang() : "",
                g.getGiaCDText(),
                g.getGiaROMText(),
                g.getGiaThueText() // THÊM DÒNG NÀY ĐỂ HIỆN GIÁ THUÊ
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

    // Đổi tên từ showEditCustomerDialog thành showEditGameDialog
    private void showEditGameDialog(Game g) { 
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        JTextField txtTen = new JTextField(g.getTenGame());
        JTextField txtTheLoai = new JTextField(g.getTheLoai());
        JTextField txtNenTang = new JTextField(g.getNenTang());
        JTextField txtGhiChu = new JTextField(g.getGhiChu());
        JTextField txtHinhAnh = new JTextField(g.getHinhAnh());
        
        // Đổ dữ liệu giá hiện tại
        JTextField txtGiaCD = new JTextField(String.valueOf(g.getGiaCD()));
        JTextField txtGiaROM = new JTextField(String.valueOf(g.getGiaROM()));
        JTextField txtGiaThue = new JTextField(String.valueOf(g.getGiaThueNgay()));

        panel.add(new JLabel("Tên Game (*):")); panel.add(txtTen);
        panel.add(new JLabel("Thể Loại (*):")); panel.add(txtTheLoai);
        panel.add(new JLabel("Nền Tảng (*):")); panel.add(txtNenTang);
        panel.add(new JLabel("Ghi Chú:")); panel.add(txtGhiChu);
        panel.add(new JLabel("Link Hình Ảnh:")); panel.add(txtHinhAnh);
        panel.add(new JLabel("Giá CD (VNĐ):")); panel.add(txtGiaCD);
        panel.add(new JLabel("Giá ROM (VNĐ):")); panel.add(txtGiaROM);
        panel.add(new JLabel("Giá Thuê/Ngày (VNĐ):")); panel.add(txtGiaThue);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Cập Nhật Thông Tin Game", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                g.setTenGame(txtTen.getText().trim());
                g.setTheLoai(txtTheLoai.getText().trim());
                g.setNenTang(txtNenTang.getText().trim());
                g.setGhiChu(txtGhiChu.getText().trim());
                g.setHinhAnh(txtHinhAnh.getText().trim());
                g.setGiaCD(Double.parseDouble(txtGiaCD.getText().trim()));
                g.setGiaROM(Double.parseDouble(txtGiaROM.getText().trim()));
                g.setGiaThueNgay(Double.parseDouble(txtGiaThue.getText().trim()));

                if (service.updateGame(g)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        }
    }

    private void showSortPopupMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem m1 = new JMenuItem("Mã Game (Tăng dần)");
        m1.addActionListener(e -> executeSort("MaGame", true));
        JMenuItem m2 = new JMenuItem("Mã Game (Giảm dần)");
        m2.addActionListener(e -> executeSort("MaGame", false));
        
        JMenuItem m3 = new JMenuItem("Giá Bán ROM (Tăng dần)");
        m3.addActionListener(e -> executeSort("GiaROM", true));
        JMenuItem m4 = new JMenuItem("Giá Bán ROM (Giảm dần)");
        m4.addActionListener(e -> executeSort("GiaROM", false));
        
        JMenuItem m5 = new JMenuItem("Giá Thuê CD (Tăng dần)");
        m5.addActionListener(e -> executeSort("GiaThue", true));
        JMenuItem m6 = new JMenuItem("Giá Thuê CD (Giảm dần)");
        m6.addActionListener(e -> executeSort("GiaThue", false));

        menu.add(m1); menu.add(m2);
        menu.addSeparator();
        menu.add(m3); menu.add(m4);
        menu.addSeparator();
        menu.add(m5); menu.add(m6);
        
        menu.show(invoker, 0, invoker.getHeight());
    }

    private void executeSort(String type, boolean ascending) {
        if (allData == null) return;
        
        allData.sort((g1, g2) -> {
            int res = 0;
            switch (type) {
                case "MaGame":
                    res = Integer.compare(g1.getMaGame(), g2.getMaGame());
                    break;
                case "GiaROM":
                    // Sắp xếp số học cho giá tiền (Double)
                    double r1 = g1.getGiaROM() == null ? 0 : g1.getGiaROM();
                    double r2 = g2.getGiaROM() == null ? 0 : g2.getGiaROM();
                    res = Double.compare(r1, r2);
                    break;
                case "GiaThue":
                    double t1 = g1.getGiaThueNgay() == null ? 0 : g1.getGiaThueNgay();
                    double t2 = g2.getGiaThueNgay() == null ? 0 : g2.getGiaThueNgay();
                    res = Double.compare(t1, t2);
                    break;
            }
            return ascending ? res : -res;
        });
        
        currentPage = 1; // Quay về trang đầu để thấy kết quả sắp xếp
        renderPage();
    }
}
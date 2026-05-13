package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.controller.GameController;
import otkhongluong.gamestoremanagement.util.IconUtils;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.List;

public class GameManagePanel extends JPanel {

    /* ============ COLORS & FONTS ============ */
    private static final Color BG_DARK       = new Color(35, 20, 85);
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
    private boolean isFilterMode = false;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private final GameController controller = new GameController();
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

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("QUẢN LÝ GAME");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
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

        RoundButton btnViewDetail = new RoundButton("Xem chi tiết", INPUT_BG, BG_DARK);
        btnViewDetail.setPreferredSize(new Dimension(110, 40));
        btnViewDetail.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                JOptionPane.showMessageDialog(this, "Chọn game để xem chi tiết");
                return;
            }
            int modelRow = table.convertRowIndexToModel(viewRow);
            Game g = currentPageData.get(modelRow);
            showViewDetailDialog(g);
        });

        RoundButton btnSort = new RoundButton("Sắp xếp", INPUT_BG, BG_DARK);
        btnSort.setPreferredSize(new Dimension(90, 40));
        btnSort.addActionListener(e -> showSortPopupMenu(btnSort));

        RoundButton btnFilter = new RoundButton("Lọc", INPUT_BG, BG_DARK);
        btnFilter.setPreferredSize(new Dimension(70, 40));
        btnFilter.addActionListener(e -> toggleFilterMode(btnFilter));

        leftGroup.add(btnViewDetail);
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
        btnAdd.addActionListener(e -> showGameDialog(null));

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
        tf.setForeground(new Color(40,40,40));
        tf.setCaretColor(ACCENT);
        tf.setFont(FONT_CELL);
        tf.setBorder(new EmptyBorder(6, 32, 6, 10));
        tf.setOpaque(false);
    }

    private JScrollPane buildTable() {
        // --- ĐÃ THÊM CỘT HÌNH ẢNH Ở ĐÂY ---
        String[] cols = {"Mã Game", "Tên Game", "Thể Loại", "Nền Tảng", "Ghi Chú", "Hình Ảnh"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (c instanceof JLabel) ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                if (isRowSelected(row)) {
                    c.setBackground(ACCENT); c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? PURPLE_ROW : PURPLE_ALT);
                    c.setForeground(new Color(40, 40, 40));
                }
                return c;
            }
        };

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        JScrollPane sp = new JScrollPane(table); 
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 245)));

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!isFilterMode) return;
                int viewCol = table.columnAtPoint(e.getPoint());
                if (viewCol == -1) return;
                int modelCol = table.convertColumnIndexToModel(viewCol);
                String colName = table.getColumnName(viewCol);
                String filter = JOptionPane.showInputDialog(null, "Lọc cột [" + colName + "]:");
                if (filter != null) rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + filter.trim(), modelCol));
            }
        });

        table.setFont(FONT_CELL); table.setRowHeight(38); table.setShowGrid(false);
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString());
                lbl.setFont(FONT_HEADER); lbl.setForeground(Color.WHITE);
                lbl.setBackground(PURPLE_HEADER); lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true); lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
                return lbl;
            }
        });
        header.setPreferredSize(new Dimension(0, 42));

        // Tăng độ rộng để chứa thêm cột Hình ảnh
        int[] widths = {80, 200, 110, 110, 150, 150}; 
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        return sp;
    }

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
            if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn game cần sửa!"); return; }
            showGameDialog(currentPageData.get(table.convertRowIndexToModel(row)));
        });

        RoundButton btnDelete = new RoundButton(" Xóa", BTN_DELETE, BG_DARK);
        btnDelete.setIcon(IconUtils.getDeleteIcon(16, BG_DARK));
        btnDelete.setPreferredSize(new Dimension(110, 40));
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn game để xóa!"); return; }
            Game g = currentPageData.get(table.convertRowIndexToModel(row));
            if (JOptionPane.showConfirmDialog(this, "Xóa game: " + g.getTenGame() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (controller.deleteGame(g.getMaGame())) { loadData(); JOptionPane.showMessageDialog(this, "Đã xóa!"); }
            }
        });

        btnPanel.add(btnEdit); btnPanel.add(btnDelete);
        bar.add(btnPanel, BorderLayout.EAST);
        return bar;
    }

    private void showGameDialog(Game g) {
        boolean isEdit = (g != null);
        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 15, 10));
        
        JTextField txtTen = new JTextField(isEdit ? g.getTenGame() : "");
        JTextField txtTheLoai = new JTextField(isEdit ? g.getTheLoai() : "");
        JTextField txtNenTang = new JTextField(isEdit ? g.getNenTang() : "");
        JTextField txtGhiChu = new JTextField(isEdit ? g.getGhiChu() : "");
        // 1. Khai báo ô nhập và nút chọn ảnh
    JTextField txtHinh = new JTextField(isEdit ? g.getHinhAnh() : "");
    RoundButton btnBrowse = new RoundButton("...", INPUT_BG, BG_DARK);
    btnBrowse.setPreferredSize(new Dimension(40, 30));

    // 2. Tạo một Panel phụ để xếp ô nhập và nút nằm cạnh nhau
    JPanel imgRow = new JPanel(new BorderLayout(5, 0));
    imgRow.setOpaque(false);
    imgRow.add(txtHinh, BorderLayout.CENTER);
    imgRow.add(btnBrowse, BorderLayout.EAST);

    // 3. Viết sự kiện khi bấm nút "..."
    btnBrowse.addActionListener(e -> {
        JFileChooser fileChooser = new JFileChooser();
        
        // Chỉ hiện các file là hình ảnh (tùy chọn)
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Hình ảnh (jpg, png, webp)", "jpg", "png", "jpeg", "webp"));
            
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // Lấy đường dẫn tuyệt đối của file đã chọn và đưa vào ô text
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            txtHinh.setText(path);
        }
    });

        JTextField txtRating = new JTextField(isEdit ? g.getRating() : "");
        JTextField txtGenre = new JTextField(isEdit ? g.getGenre() : "");
        JTextField txtRegion = new JTextField(isEdit ? g.getRegion() : "");
        JTextField txtRelease = new JTextField(isEdit && g.getReleaseDate() != null ? g.getReleaseDate().toString() : "yyyy-MM-dd");
        JTextArea txtMoTa = new JTextArea(isEdit ? g.getMoTa() : "", 3, 20);

        mainPanel.add(new JLabel("Tên Game (*):")); mainPanel.add(txtTen);
        mainPanel.add(new JLabel("Thể Loại (*):")); mainPanel.add(txtTheLoai);
        mainPanel.add(new JLabel("Nền Tảng (*):")); mainPanel.add(txtNenTang);
        mainPanel.add(new JLabel("Ghi Chú:")); mainPanel.add(txtGhiChu);
        mainPanel.add(new JLabel("Link Ảnh:")); mainPanel.add(txtHinh);
        mainPanel.add(new JLabel("--- Chi tiết ---")); mainPanel.add(new JLabel(""));
        mainPanel.add(new JLabel("Rating:")); mainPanel.add(txtRating);
        mainPanel.add(new JLabel("Genre:")); mainPanel.add(txtGenre);
        mainPanel.add(new JLabel("Region:")); mainPanel.add(txtRegion);
        mainPanel.add(new JLabel("Ngày (ISO):")); mainPanel.add(txtRelease);
        mainPanel.add(new JLabel("Mô tả:")); mainPanel.add(new JScrollPane(txtMoTa));
        mainPanel.add(new JLabel("Link Ảnh:")); mainPanel.add(imgRow);

        int res = JOptionPane.showConfirmDialog(this, new JScrollPane(mainPanel), isEdit ? "Sửa Game" : "Thêm Game", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            Game target = isEdit ? g : new Game();
            target.setTenGame(txtTen.getText().trim()); target.setTheLoai(txtTheLoai.getText().trim());
            target.setNenTang(txtNenTang.getText().trim()); target.setGhiChu(txtGhiChu.getText().trim());
            target.setHinhAnh(txtHinh.getText().trim()); target.setRating(txtRating.getText().trim());
            target.setGenre(txtGenre.getText().trim()); target.setRegion(txtRegion.getText().trim());
            target.setMoTa(txtMoTa.getText().trim());
            try { target.setReleaseDate(LocalDate.parse(txtRelease.getText().trim())); } catch (Exception ignored) {}

            boolean success = isEdit ? controller.updateGame(target) : controller.addGame(target);
            if (success) { loadData(); JOptionPane.showMessageDialog(this, "Thành công!"); }
        }
    }

    private void rebuildPagination(JPanel panel) {
        panel.removeAll();
        List<Game> filtered = getFilteredData();
        int total = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        RoundButton prev = new RoundButton("<", INPUT_BG, BG_DARK);
        prev.setEnabled(currentPage > 1);
        prev.addActionListener(e -> { if (currentPage > 1) { currentPage--; renderPage(); } });
        JLabel lbl = new JLabel("Trang " + currentPage + " / " + total);
        lbl.setForeground(TEXT_WHITE); lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
        RoundButton next = new RoundButton(">", INPUT_BG, BG_DARK);
        next.setEnabled(currentPage < total);
        next.addActionListener(e -> { if (currentPage < total) { currentPage++; renderPage(); } });
        panel.add(prev); panel.add(lbl); panel.add(next);
        panel.revalidate(); panel.repaint();
    }

    private void loadData() { allData = controller.loadAllGames(); renderPage(); }
    
    private List<Game> getFilteredData() {
        String kw = txtSearch == null ? "" : txtSearch.getText().trim();
        return controller.filterForManage(kw);
    }

    private void renderPage() {
        tableModel.setRowCount(0);
        List<Game> filtered = getFilteredData();
        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        if (currentPage > totalPages) currentPage = 1;
        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filtered.size());
        currentPageData = new ArrayList<>(filtered.subList(from, to));
        // --- HIỆN THỊ ĐỦ 6 CỘT TRONG BẢNG ---
        for (Game g : currentPageData) tableModel.addRow(new Object[]{
            "G" + String.format("%03d", g.getMaGame()), 
            g.getTenGame(), 
            g.getTheLoai(), 
            g.getNenTang(), 
            g.getGhiChu(),
            g.getHinhAnh()
        });
        rebuildPagination(paginationPanel);
    }

    private void toggleFilterMode(RoundButton btn) {
        isFilterMode = !isFilterMode;
        btn.setBackground(isFilterMode ? ACCENT : INPUT_BG);
        btn.setForeground(isFilterMode ? Color.WHITE : BG_DARK);
        if (!isFilterMode) rowSorter.setRowFilter(null);
    }

    private void showSortPopupMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        addSortItem(menu, "Mã Game", "MaGame");
        addSortItem(menu, "Tên Game", "TenGame");
        menu.show(invoker, 0, invoker.getHeight());
    }

    private void addSortItem(JPopupMenu menu, String label, String type) {
        JMenuItem asc = new JMenuItem(label + " (A-Z)"); asc.addActionListener(e -> executeSort(type, true));
        JMenuItem desc = new JMenuItem(label + " (Z-A)"); desc.addActionListener(e -> executeSort(type, false));
        menu.add(asc); menu.add(desc);
    }

    private void executeSort(String type, boolean asc) {
        controller.sortCache(type, asc);
        allData = controller.loadAllGames(); // lấy lại cache đã sort
        currentPage = 1; renderPage();
    }

    static class RoundButton extends JButton {
        private final Color bg, fg;
        RoundButton(String t, Color b, Color f) { super(t); this.bg = b; this.fg = f; setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false); setForeground(fg); setFont(new Font("Segoe UI", Font.BOLD, 13)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getModel().isRollover() ? bg.brighter() : bg); g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12)); super.paintComponent(g2); g2.dispose(); }
    }

    // ── HÀM HIỂN THỊ CHI TIẾT GAME (CHỈ ĐỌC) ──────────────────────
    private void showViewDetailDialog(Game g) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panel chứa thông tin dạng lưới 2 cột
        JPanel grid = new JPanel(new GridLayout(0, 2, 20, 12));
        grid.setBackground(Color.WHITE);

        // --- Nhóm 1: Thông tin cơ bản (Bảng GAME) ---
        addDetailRow(grid, "Mã Game:", "G" + String.format("%03d", g.getMaGame()));
        addDetailRow(grid, "Tên Game:", nvl(g.getTenGame()));
        addDetailRow(grid, "Thể Loại:", nvl(g.getTheLoai()));
        addDetailRow(grid, "Nền Tảng:", nvl(g.getNenTang()));
        addDetailRow(grid, "Ghi Chú:", nvl(g.getGhiChu()));
        addDetailRow(grid, "Link Hình Ảnh:", nvl(g.getHinhAnh()));

        // Dòng kẻ phân cách ảo
        JLabel lblSep = new JLabel("<html><b style='color:#825AE6'>--- THÔNG TIN MỞ RỘNG ---</b></html>");
        grid.add(lblSep);
        grid.add(new JLabel(""));

        // --- Nhóm 2: Thông tin mở rộng (Bảng GAME_CHITIET) ---
        addDetailRow(grid, "Rating:", nvl(g.getRating()));
        addDetailRow(grid, "Genre:", nvl(g.getGenre()));
        addDetailRow(grid, "Phương thức:", nvl(g.getDeliveryMethod()));
        addDetailRow(grid, "Ngày Phát Hành:", g.getReleaseDate() != null ? g.getReleaseDate().toString() : "N/A");
        addDetailRow(grid, "Khu vực:", nvl(g.getRegion()));
        addDetailRow(grid, "Tính năng:", nvl(g.getFeatures()));
        addDetailRow(grid, "Ngôn ngữ:", nvl(g.getLanguage()));
        addDetailRow(grid, "Tiền tệ:", nvl(g.getCurrency()));

        // --- Phần mô tả dài (Dùng JTextArea) ---
        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.setBackground(Color.WHITE);
        descPanel.setBorder(new TitledBorder(new LineBorder(new Color(230,230,230)), "Mô tả chi tiết"));
        
        JTextArea area = new JTextArea(nvl(g.getMoTa()), 5, 45);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBackground(new Color(250, 250, 255));
        descPanel.add(new JScrollPane(area), BorderLayout.CENTER);

        mainPanel.add(grid, BorderLayout.NORTH);
        mainPanel.add(descPanel, BorderLayout.CENTER);

        // Hiển thị Dialog
        JOptionPane.showMessageDialog(this, new JScrollPane(mainPanel), 
            "Thông tin chi tiết: " + g.getTenGame(), JOptionPane.PLAIN_MESSAGE);
    }

    // Hàm vẽ từng dòng thông tin (Label bên trái - Text không cho sửa bên phải)
    private void addDetailRow(JPanel p, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(70, 70, 90));
        p.add(lbl);

        JTextField txt = new JTextField(value.isEmpty() ? "---" : value);
        txt.setEditable(false);
        txt.setBorder(null);
        txt.setBackground(null);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(txt);
    }

    // Hàm hỗ trợ xử lý chuỗi null để tránh lỗi hiển thị
    private String nvl(String s) {
        return (s == null) ? "" : s.trim();
    }
}
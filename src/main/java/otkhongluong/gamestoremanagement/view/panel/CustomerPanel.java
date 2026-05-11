package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.KhachHang;
import otkhongluong.gamestoremanagement.service.KhachHangService;
import otkhongluong.gamestoremanagement.util.IconUtils;

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
    private RoundButton btnEdit;
    private RoundButton btnDelete;
    private RoundButton btnSort;
    private RoundButton btnFilter;
    private boolean isAscending = true;
    private String currentSortColumn = "MaKH";
    private boolean isFilterMode = false;
    private TableRowSorter<DefaultTableModel> rowSorter;
    

    private KhachHangService service = new KhachHangService();
    private List<KhachHang> allData;
    private List<KhachHang> currentPageData;

    public CustomerPanel() {
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

        // --- Cụm nút bên TRÁI ô tìm kiếm ---
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftGroup.setBackground(BG_DARK);

        btnSort = new RoundButton("Sắp xếp", INPUT_BG, BG_DARK);
        btnSort.setPreferredSize(new Dimension(90, 40));
        btnSort.addActionListener(e -> showSortPopupMenu(btnSort));

        // --- Nút Lọc (Chế độ lọc giống Nhân viên) ---
        btnFilter = new RoundButton("Lọc", INPUT_BG, BG_DARK);
        btnFilter.setPreferredSize(new Dimension(70, 40));
        btnFilter.addActionListener(e -> {
            isFilterMode = !isFilterMode;
            if (isFilterMode) {
                btnFilter.setBackground(ACCENT);
                btnFilter.setForeground(Color.WHITE);
                JOptionPane.showMessageDialog(this, "Chế độ Lọc ĐÃ BẬT.\nHãy nhấn vào tiêu đề cột (VD: 'Họ Tên') để lọc.");
            } else {
                btnFilter.setBackground(INPUT_BG);
                btnFilter.setForeground(BG_DARK);
                rowSorter.setRowFilter(null); // Tắt lọc
            }
        });

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
        // Tìm đoạn btnAdd trong hàm labeledSearch() và sửa lại:
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

        int[] widths = {80, 200, 120, 120, 180, 120};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(PURPLE_HEADER, 1, true));
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(PURPLE_ALT);


        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        
        // Sự kiện click vào tiêu đề cột để lọc (giống trang Nhân viên)
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isFilterMode) {
                    int col = table.columnAtPoint(e.getPoint());
                    String colName = table.getColumnName(col);
                    String filter = JOptionPane.showInputDialog(null, "Lọc theo " + colName + ":");
                    if (filter != null && !filter.trim().isEmpty()) {
                        // Lọc không phân biệt hoa thường
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + filter, col));
                    } else {
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

        // PHẢI KHỞI TẠO NÚT TRƯỚC KHI GÁN SỰ KIỆN
        btnEdit = new RoundButton(" Sửa", BTN_EDIT, BG_DARK);
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

        btnDelete = new RoundButton(" Xóa", BTN_DELETE, BG_DARK);
        btnDelete.setIcon(IconUtils.getDeleteIcon(16, BG_DARK));
        btnDelete.setPreferredSize(new Dimension(110, 40));
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để xóa!");
                return;
            }
            KhachHang kh = currentPageData.get(row);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Xác nhận xóa khách hàng: " + kh.getHoTen() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (service.deleteKhachHang(kh.getMaKH())) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa!");
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
        List<KhachHang> filtered = getFilteredData();
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
        allData = service.getAllKhachHang();
        renderPage();
    }
    
    private List<KhachHang> getFilteredData() {
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();

        return allData == null ? java.util.Collections.emptyList() :
            allData.stream()
                .filter(kh -> {
                    if (keyword.isEmpty()) return true;
                    
                    // Tạo chuỗi mã để so sánh (cả số và định dạng KH00x)
                    String maKHStr = String.valueOf(kh.getMaKH());
                    String maKHFull = "kh" + String.format("%03d", kh.getMaKH());
                    
                    return kh.getHoTen().toLowerCase().contains(keyword)
                        || (kh.getSdt() != null && kh.getSdt().contains(keyword))
                        || (kh.getCccd() != null && kh.getCccd().contains(keyword))
                        || maKHStr.contains(keyword) // Tìm theo số: "1"
                        || maKHFull.contains(keyword); // Tìm theo mã: "kh001"
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private void renderPage() {
        tableModel.setRowCount(0);
        List<KhachHang> filtered = getFilteredData();

        int totalPage = (int) Math.ceil((double) filtered.size() / PAGE_SIZE);
        if (currentPage > totalPage) currentPage = totalPage == 0 ? 1 : totalPage;

        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filtered.size());

        currentPageData = filtered.subList(from, to);

        for (KhachHang kh : currentPageData) {
            tableModel.addRow(new Object[]{
                "KH" + String.format("%03d", kh.getMaKH()),
                kh.getHoTen(),
                kh.getSdt() != null ? kh.getSdt() : "",
                kh.getCccd() != null ? kh.getCccd() : "",
                kh.getEmail() != null ? kh.getEmail() : "",
                kh.getDiemTichLuy()
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

    private void showAddCustomerDialog() {
        // Tạo Panel chứa form nhập liệu
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtHoTen = new JTextField();
        JTextField txtSdt = new JTextField();
        JTextField txtCccd = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtDiaChi = new JTextField();

        panel.add(new JLabel("Họ tên (*):"));
        panel.add(txtHoTen);
        panel.add(new JLabel("Số điện thoại (*):"));
        panel.add(txtSdt);
        panel.add(new JLabel("CCCD (*):"));
        panel.add(txtCccd);
        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);
        panel.add(new JLabel("Địa chỉ:"));
        panel.add(txtDiaChi);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Thêm Khách Hàng Mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Khởi tạo đối tượng từ dữ liệu nhập vào
                KhachHang kh = new KhachHang();
                kh.setHoTen(txtHoTen.getText().trim());
                kh.setSdt(txtSdt.getText().trim());
                kh.setCccd(txtCccd.getText().trim());
                kh.setEmail(txtEmail.getText().trim());
                kh.setDiaChi(txtDiaChi.getText().trim());
                kh.setDiemTichLuy(0);

                // Gọi Service xử lý (Service sẽ tự gọi Validation và DAO)
                if (service.addKhachHang(kh)) {
                    JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
                    loadData(); // Load lại bảng để cập nhật dữ liệu mới
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm thất bại! Vui lòng kiểm tra lại kết nối Database.");
                }
            } catch (IllegalArgumentException ex) {
                // Bắt lỗi từ ValidationService (ví dụ: SDT không đúng định dạng)
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void showEditCustomerDialog(KhachHang kh) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        JTextField txtHoTen = new JTextField(kh.getHoTen());
        JTextField txtSdt = new JTextField(kh.getSdt());
        JTextField txtCccd = new JTextField(kh.getCccd());
        JTextField txtEmail = new JTextField(kh.getEmail());
        JTextField txtDiaChi = new JTextField(kh.getDiaChi());

        panel.add(new JLabel("Họ tên (*):")); panel.add(txtHoTen);
        panel.add(new JLabel("Số điện thoại (*):")); panel.add(txtSdt);
        panel.add(new JLabel("CCCD (*):")); panel.add(txtCccd);
        panel.add(new JLabel("Email:")); panel.add(txtEmail);
        panel.add(new JLabel("Địa chỉ:")); panel.add(txtDiaChi);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Cập Nhật Thông Tin Khách Hàng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Cập nhật thông tin mới vào đối tượng hiện tại
                kh.setHoTen(txtHoTen.getText().trim());
                kh.setSdt(txtSdt.getText().trim());
                kh.setCccd(txtCccd.getText().trim());
                kh.setEmail(txtEmail.getText().trim());
                kh.setDiaChi(txtDiaChi.getText().trim());

                // Gọi Service để update xuống DB
                if (service.updateKhachHang(kh)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

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
        
        allData.sort((kh1, kh2) -> {
            int res = 0;
            if (type.equals("MaKH")) {
                res = Integer.compare(kh1.getMaKH(), kh2.getMaKH());
            } else {
                res = Integer.compare(kh1.getDiemTichLuy(), kh2.getDiemTichLuy());
            }
            return ascending ? res : -res;
        });
        
        currentPage = 1; // Về trang đầu sau khi sắp xếp
        renderPage();
    }

    private void sortData(String column) {
        if (allData == null || allData.isEmpty()) return;

        // Nếu nhấn lại cột cũ thì đảo chiều, nhấn cột mới thì mặc định tăng dần
        if (currentSortColumn.equals(column)) {
            isAscending = !isAscending;
        } else {
            currentSortColumn = column;
            isAscending = true;
        }

        allData.sort((kh1, kh2) -> {
            int result = 0;
            if (column.equals("MaKH")) {
                result = Integer.compare(kh1.getMaKH(), kh2.getMaKH());
            } else {
                result = Integer.compare(kh1.getDiemTichLuy(), kh2.getDiemTichLuy());
            }
            return isAscending ? result : -result;
        });

        renderPage();
    }

    private void showSimpleFilterDialog() {
        String[] options = {"Tất cả", "Khách hàng thân thiết (>= 100 điểm)", "Khách hàng mới (< 100 điểm)"};
        String selection = (String) JOptionPane.showInputDialog(this, 
                "Chọn chế độ lọc nhanh:", "Bộ lọc", 
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (selection == null) return;

        if (selection.equals("Tất cả")) {
            loadData(); // Load lại toàn bộ từ DB
        } else {
            // Lọc trực tiếp trên allData hiện có
            allData = service.getAllKhachHang(); // Đảm bảo lấy data mới nhất
            if (selection.contains(">= 100")) {
                allData.removeIf(kh -> kh.getDiemTichLuy() < 100);
            } else {
                allData.removeIf(kh -> kh.getDiemTichLuy() >= 100);
            }
            currentPage = 1;
            renderPage();
        }
    }
}
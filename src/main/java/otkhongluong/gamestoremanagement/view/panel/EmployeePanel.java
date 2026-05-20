    package otkhongluong.gamestoremanagement.view.panel;

    import otkhongluong.gamestoremanagement.model.Employee;
    import otkhongluong.gamestoremanagement.model.User;
    import otkhongluong.gamestoremanagement.controller.EmployeeController;
    import otkhongluong.gamestoremanagement.controller.EmployeeController.SaveResult;
    import otkhongluong.gamestoremanagement.util.IconUtils;
    import otkhongluong.gamestoremanagement.view.dialog.EmployeeDialog;
    import otkhongluong.gamestoremanagement.util.RoundButton;
    import otkhongluong.gamestoremanagement.util.FormatUtil;
    import javax.swing.*;
    import javax.swing.border.*;
    import javax.swing.table.*;
    import java.awt.*;
    import java.awt.event.*;
    import java.awt.geom.RoundRectangle2D;
    import java.time.format.DateTimeFormatter;
    import java.util.List;

    public class EmployeePanel extends JPanel {

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
        private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 13);

        /* ============ COMPONENTS ============ */
        private JTable table;
        private DefaultTableModel tableModel;
        private JTextField txtSearch;
        private boolean isFilterMode = false;
        private TableRowSorter<DefaultTableModel> rowSorter;
        private JPanel paginationPanel;
        private int currentPage = 1;
        private static final int PAGE_SIZE = 8;

        private User currentUser;

        // ✅ Controller không nhận View, allData sống ở đây nhưng chỉ là cache hiển thị
        private final EmployeeController controller;

        // allData: cache dữ liệu từ DB sau mỗi lần loadData()
        // currentPageData: subList hiện tại đang hiển thị trên bảng
        private List<Employee> allData;
        private List<Employee> currentPageData;

        public EmployeePanel(User user) {
            this.currentUser = user;

            // ✅ FIX: Khởi tạo controller TRƯỚC KHI build UI
            //    Trước đây controller được gán SAU buildBottomBar() → NullPointerException
            this.controller = new EmployeeController();

            setLayout(new BorderLayout(0, 0));
            setBackground(BG_DARK);
            setBorder(new EmptyBorder(20, 20, 20, 20));

            add(buildTopBar(),    BorderLayout.NORTH);
            add(buildTable(),     BorderLayout.CENTER);
            add(buildBottomBar(), BorderLayout.SOUTH);

            // loadData() gọi cuối cùng sau khi toàn bộ UI đã sẵn sàng
            loadData();
        }

        /* ======================================================
            TOP BAR
        ====================================================== */
        private JPanel buildTopBar() {
            JPanel bar = new JPanel(new BorderLayout());
            bar.setBackground(BG_DARK);
            bar.setBorder(new EmptyBorder(0, 0, 12, 0));

            JLabel title = new JLabel("QUẢN LÝ NHÂN VIÊN");
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
            
            RoundButton btnSort = new RoundButton(" Sắp xếp", Color.WHITE, BG_DARK);
            btnSort.setPreferredSize(new Dimension(90, 40));
            btnSort.addActionListener(e -> showSortMenu(btnSort));

            RoundButton btnFilter = new RoundButton(" Lọc", Color.WHITE, BG_DARK);
            btnFilter.setPreferredSize(new Dimension(70, 40));
            btnFilter.addActionListener(e -> toggleFilterMode(btnFilter));

            

            RoundButton btnAdd = new RoundButton("", BTN_ADD, Color.WHITE);
            btnAdd.setIcon(IconUtils.getAddIcon(18, Color.WHITE));
            btnAdd.setPreferredSize(new Dimension(40, 40));
            btnAdd.addActionListener(e -> {
                new EmployeeDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    null,
                    controller,
                    this::loadData
                ).setVisible(true);
            });

            RoundButton btnUser = new RoundButton("", INPUT_BG, BG_DARK);
            btnUser.setIcon(IconUtils.getUserIcon(18, BG_DARK));
            btnUser.setPreferredSize(new Dimension(40, 40));
            btnUser.addActionListener(e -> openUserPanel());

            JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            leftGroup.setBackground(BG_DARK);
            leftGroup.add(btnSort);
            leftGroup.add(btnFilter);
            
            JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightGroup.setBackground(BG_DARK);
            rightGroup.add(btnAdd);
            rightGroup.add(btnUser);

            row.add(leftGroup,  BorderLayout.WEST);
            row.add(txtSearch,  BorderLayout.CENTER);
            row.add(rightGroup, BorderLayout.EAST);

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
            String[] cols = {"Mã NV", "Họ Tên", "Số Điện Thoại", "CCCD", "Ngày Sinh", "Ngày Vào Làm"};
            tableModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
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

            int[] widths = {80, 200, 120, 120, 120, 120};
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
            // ✅ Lần đầu build, allData chưa có → truyền null-safe vào rebuildPagination
            rebuildPagination(1);
            bar.add(paginationPanel, BorderLayout.WEST);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            btnPanel.setBackground(BG_DARK);

            RoundButton btnEdit = new RoundButton(" Sửa", BTN_EDIT, BG_DARK);
            btnEdit.setIcon(IconUtils.getEditIcon(16, BG_DARK));
            btnEdit.setPreferredSize(new Dimension(110, 40));
            btnEdit.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Chọn nhân viên để sửa!");
                    return;
                }
                Employee nv = currentPageData.get(row);
                new EmployeeDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    nv,
                    controller,       // ✅ truyền controller vào Dialog
                    this::loadData
                ).setVisible(true);
            });

            RoundButton btnDelete = new RoundButton(" Xóa", BTN_DELETE, BG_DARK);
            btnDelete.setIcon(IconUtils.getDeleteIcon(16, BG_DARK));
            btnDelete.setPreferredSize(new Dimension(110, 40));
            btnDelete.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Chọn nhân viên để xóa!");
                    return;
                }
                Employee nv = currentPageData.get(row);

                // ✅ View tự hỏi confirm, rồi mới gọi controller.handleDelete()
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận xóa nhân viên " + nv.getHoTen() + "?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION
                );
                if (confirm != JOptionPane.YES_OPTION) return;

                boolean ok = controller.handleDelete(nv.getMaNV());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa nhân viên!");
                }
            });

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

            String keyword = (txtSearch == null) ? "" : txtSearch.getText().trim();

            EmployeeController.PageResult<Employee> result =
                controller.getPage(allData, keyword, currentPage, PAGE_SIZE);

            currentPage = result.currentPage;
            currentPageData = result.data;

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Employee nv : result.data) {
                tableModel.addRow(new Object[]{
                    FormatUtil.formatMa("NV", nv.getMaNV()),
                    nv.getHoTen(),
                    nv.getSdt()        != null ? nv.getSdt()                    : "",
                    nv.getCccd()       != null ? nv.getCccd()                   : "",
                    nv.getNgaySinh()   != null ? nv.getNgaySinh().format(dtf)   : "",
                    nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().format(dtf) : ""
                });
            }

            rebuildPagination(result.totalPages);
        }

        /* ======================================================
            HELPERS
        ====================================================== */
        private void openUserPanel() {
            try {
                JDialog dialog = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Quản lý tài khoản", true
                );
                dialog.setSize(700, 400);
                dialog.setLocationRelativeTo(this);
                dialog.setLayout(new BorderLayout());
                dialog.add(new UserPanel(currentUser), BorderLayout.CENTER);
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi mở UserPanel!");
            }
        }

        private void showSortMenu(Component invoker) {
            JPopupMenu sortMenu = new JPopupMenu();

            JMenu menuMaNV = new JMenu("Mã Nhân Viên");
            JMenuItem maNVAsc  = new JMenuItem("Tăng dần (1, 2, 10...)");
            maNVAsc.addActionListener(e  -> sortData(0, true));
            JMenuItem maNVDesc = new JMenuItem("Giảm dần (10, 2, 1...)");
            maNVDesc.addActionListener(e -> sortData(0, false));
            menuMaNV.add(maNVAsc); menuMaNV.add(maNVDesc);

            JMenu menuNgaySinh = new JMenu("Ngày Sinh");
            JMenuItem birthAsc  = new JMenuItem("Cũ nhất -> Mới nhất");
            birthAsc.addActionListener(e  -> sortData(3, true));
            JMenuItem birthDesc = new JMenuItem("Mới nhất -> Cũ nhất");
            birthDesc.addActionListener(e -> sortData(3, false));
            menuNgaySinh.add(birthAsc); menuNgaySinh.add(birthDesc);

            JMenu menuNgayVaoLam = new JMenu("Ngày Vào Làm");
            JMenuItem workAsc  = new JMenuItem("Cũ nhất -> Mới nhất");
            workAsc.addActionListener(e  -> sortData(5, true));
            JMenuItem workDesc = new JMenuItem("Mới nhất -> Cũ nhất");
            workDesc.addActionListener(e -> sortData(5, false));
            menuNgayVaoLam.add(workAsc); menuNgayVaoLam.add(workDesc);

            sortMenu.add(menuMaNV);
            sortMenu.add(menuNgaySinh);
            sortMenu.add(menuNgayVaoLam);
            sortMenu.show(invoker, 0, invoker.getHeight());
        }

        private void sortData(int colIndex, boolean ascending) {
            controller.sort(allData, colIndex, ascending);
            currentPage = 1;
            renderPage();
        }

        private void toggleFilterMode(RoundButton btnFilter) {
            isFilterMode = !isFilterMode;

            if (isFilterMode) {
                btnFilter.setBackground(BTN_ADD);
                if (rowSorter == null) {
                    rowSorter = new TableRowSorter<>(tableModel);
                    table.setRowSorter(rowSorter);
                    table.getTableHeader().addMouseListener(new MouseAdapter() {
                        @Override public void mouseClicked(MouseEvent e) {
                            if (!isFilterMode) return;
                            int col = table.columnAtPoint(e.getPoint());
                            String colName = table.getColumnName(col);
                            String keyword = JOptionPane.showInputDialog(
                                EmployeePanel.this,
                                "Nhập từ khóa lọc cho cột [" + colName + "]:",
                                "Lọc dữ liệu", JOptionPane.QUESTION_MESSAGE
                            );
                            if (keyword != null && !keyword.trim().isEmpty()) {
                                rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword.trim(), col));
                            } else if (keyword != null) {
                                rowSorter.setRowFilter(null);
                            }
                        }
                    });
                }
                JOptionPane.showMessageDialog(this,
                    "Chế độ Lọc ĐÃ BẬT.\nHãy click chuột vào Tiêu đề cột trên bảng để lọc.");
            } else {
                btnFilter.setBackground(PURPLE_HEADER);
                if (rowSorter != null) rowSorter.setRowFilter(null);
            }
        }
    }
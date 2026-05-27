package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.Invoice;
import otkhongluong.gamestoremanagement.controller.InvoiceController;
import otkhongluong.gamestoremanagement.view.dialog.InvoiceDetailDialog;
import otkhongluong.gamestoremanagement.view.dialog.InvoiceAddDialog;
import otkhongluong.gamestoremanagement.view.dialog.InvoiceEditDialog;
import otkhongluong.gamestoremanagement.util.RoundButton;
import otkhongluong.gamestoremanagement.util.FormatUtil;
import otkhongluong.gamestoremanagement.util.ExportUtil;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

    public class InvoicePanel extends JPanel {

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

        /* ── Fonts ── */
        private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
        private static final Font FONT_CELL   = new Font("Segoe UI", Font.PLAIN, 13);
        private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);

        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        /* ── Column names ── */
        private static final String[] COLS = {
            "Mã HĐ", "Mã NV", "Khách hàng", "SĐT",
            "Ngày lập", "Tổng tiền", "Chi tiết"
        };


        /* ── Pagination ── */
        private static final int PAGE_SIZE = 10;
        private int currentPage = 1;

        /* ── Data ── */
        private List<Invoice> allData = new ArrayList<>();
        private List<Invoice> currentPageData = new ArrayList<>();
        private final InvoiceController controller = new InvoiceController();

        /* ── Components ── */
        private JTextField txtFrom, txtTo, txtSearch;
        private JTable table;
        private DefaultTableModel tableModel;
        private JPanel paginationPanel;
        private JLabel lblPageInfo;

        // ═══════════════════════════════════════════════════════════
        public InvoicePanel() {
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
                    fireFilter();
                }
            };
            txtFrom.addKeyListener(dateListener);
            txtTo.addKeyListener(dateListener);

            left.add(labeledField("Từ ngày (Ngày lập)", txtFrom));
            left.add(labeledField("Đến ngày (Ngày lập)", txtTo));
            left.add(buildSearchField());

            // RIGHT: nút Thêm
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            right.setBackground(BG_DARK);

            RoundButton btnAdd = new RoundButton("+  Thêm", BTN_ADD, new Color(30, 30, 30));
            btnAdd.setPreferredSize(new Dimension(105, 38));
            btnAdd.addActionListener(e -> {
                Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
                new InvoiceAddDialog(frame).setVisible(true);
                loadData();
            });

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
                        g2.drawString("dd/mm/yyyy", 10, getHeight() / 2 + 5);
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
                public void keyReleased(KeyEvent e) {
                    currentPage = 1;
                    fireFilter();
                }
            });

            p.add(txtSearch, BorderLayout.CENTER);
            return p;
        }

        // ══════════════════════════════════════════════════════════
        // TABLE with sort headers
        // ══════════════════════════════════════════════════════════
        private JScrollPane buildTable() {
            tableModel = new DefaultTableModel(COLS, 0) {
                public boolean isCellEditable(int r, int c) { return c == 6; }
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

            // ── Sort header renderer ──
            JTableHeader header = table.getTableHeader();
            header.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    int sortCol = controller.getSortCol();
                    int[] sortState = controller.getSortState();

                    String arrow = "";
                    if (sortCol == c) {
                        arrow = sortState[c] == 1 ? "  ▲" : sortState[c] == 2 ? "  ▼" : "";
                    } else {
                        if (c < COLS.length - 1) arrow = "";
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
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());

                currentPage = 1;

                render(controller.onSortChanged(
                        col,
                        txtFrom.getText(),
                        txtTo.getText(),
                        txtSearch.getText(),
                        currentPage
                ));
            }
        });

            // Column widths
            int[] widths = {75, 75, 160, 115, 115, 130, 75};
            for (int i = 0; i < widths.length; i++)
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

            // "Chi tiết" button column
            table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer("Xem", ACCENT));
            table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox(), table));

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
            
            RoundButton btnExportPDF = new RoundButton("Xuất PDF", new Color(234, 88, 12), Color.WHITE);
            btnExportPDF.setPreferredSize(new Dimension(105, 38));
            btnExportPDF.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn!");
                    return;
                }
                // Cột 0 là MaHD — kiểm tra lại index theo JTable của bạn
                int maHD = Integer.parseInt(table.getValueAt(row, 0).toString()
                    .replace("HD", "").trim());

                String path = ExportUtil.chooseFilePath(this, "pdf", "PDF Files");
                if (path == null) return;

                try {
                    controller.exportInvoicePDF(maHD, path);
                    JOptionPane.showMessageDialog(this,
                        "Xuất PDF thành công!\n" + path, "OK",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Lỗi: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            });

            RoundButton btnEdit = new RoundButton("Sửa", BTN_EDIT, Color.WHITE);
            btnEdit.setPreferredSize(new Dimension(105, 38));
            btnEdit.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn hóa đơn để sửa!"); return; }
                int maHD = parseMa(tableModel.getValueAt(row, 0).toString());
                Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);  // ✅ View tự lấy Frame
                new InvoiceEditDialog(frame, maHD).setVisible(true);            // ✅ View tự mở Dialog
                loadData();
            });

            RoundButton btnDelete = new RoundButton("Xóa", BTN_DELETE, Color.WHITE);
            btnDelete.setPreferredSize(new Dimension(105, 38));
            // ✅ MỚI
            btnDelete.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(this, "Chọn hóa đơn để xóa!"); return;
                }

                String maHDStr = tableModel.getValueAt(row, 0).toString();
                Invoice hd = currentPageData.get(row);

                // Hiển thị cảnh báo rõ ràng
                int confirm = JOptionPane.showConfirmDialog(this,
                    "⚠ Xác nhận xóa " + maHDStr + "?\n\n" +
                    "  Khách hàng : " + hd.getTenKhachHang() + "\n" +
                    "  Tổng tiền  : " + String.format("%,.0f đ", hd.getTongTien()) + "\n\n" +
                    "Hệ thống sẽ tự động:\n" +
                    "  • Hoàn lại trạng thái CD về Sẵn sàng\n" +
                    "  • Trừ lượt bán ROM\n" +
                    "  • Hoàn/rút lại điểm tích lũy của khách\n\n" +
                    "Thao tác này KHÔNG THỂ hoàn tác!",
                    "Xác nhận xóa hóa đơn",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

                if (confirm != JOptionPane.YES_OPTION) return;

                int maHD = Integer.parseInt(maHDStr.replaceAll("\\D", ""));
                boolean ok = controller.deleteInvoice(maHD);

                if (ok) {
                    JOptionPane.showMessageDialog(this,
                        "✅ Đã xóa " + maHDStr + " và rollback toàn bộ dữ liệu liên quan.",
                        "Xóa thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "❌ Xóa thất bại!\nVui lòng thử lại hoặc kiểm tra log.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            btnPanel.add(btnExportPDF);
            btnPanel.add(btnEdit);
            btnPanel.add(btnDelete);
            bar.add(btnPanel, BorderLayout.EAST);
            return bar;
        }

        // ══════════════════════════════════════════════════════════
        // PAGINATION
        // ══════════════════════════════════════════════════════════
        private void rebuildPagination(int totalPages) {
            paginationPanel.removeAll();

            RoundButton btnPrev = new RoundButton("<", INPUT_BG, new Color(80, 80, 80));
            btnPrev.setPreferredSize(new Dimension(34, 34));
            btnPrev.setEnabled(currentPage > 1);
            btnPrev.addActionListener(e -> {
                if (currentPage > 1) {
                    currentPage--;
                    fireFilter();
                }
            });
            paginationPanel.add(btnPrev);

            RoundButton btnNext = new RoundButton(">", INPUT_BG, new Color(80, 80, 80));
            btnNext.setPreferredSize(new Dimension(34, 34));
            btnNext.setEnabled(currentPage < totalPages);
            btnNext.addActionListener(e -> { 
                if (currentPage < totalPages) { 
                    currentPage++; 
                    fireFilter(); 
                } 
            });
            paginationPanel.add(btnNext);

            lblPageInfo.setText("Trang " + currentPage + " / " + Math.max(1, totalPages));

            paginationPanel.revalidate();
            paginationPanel.repaint();
        }

        // ══════════════════════════════════════════════════════════
        // DATA PIPELINE
        // ══════════════════════════════════════════════════════════
        private void loadData() {
            allData = controller.getAllInvoices();
            currentPage = 1;
            fireFilter();
        }

        private void render(InvoiceController.InvoicePageResult result) {
            txtFrom.setBackground(result.fromDateError ? INPUT_ERROR : INPUT_BG);
            txtTo.setBackground(result.toDateError ? INPUT_ERROR : INPUT_BG);

            if (result.hasDateError()) return;

            tableModel.setRowCount(0);
            currentPageData = new ArrayList<>(result.rows);

            for (Invoice hd : result.rows) {
                tableModel.addRow(new Object[]{
                    FormatUtil.formatMa("HD", hd.getMaHD()),
                    FormatUtil.formatMa("NV", hd.getMaNV()),
                    hd.getTenKhachHang(),
                    hd.getSoDienThoai(),
                    hd.getNgayLap() != null ? hd.getNgayLap().format(FMT) : "",
                    String.format("%,.0f đ", hd.getTongTien()),
                    "Xem"
                });
            }

            rebuildPagination(result.totalPages);
            table.getTableHeader().repaint();
        }
        
        private void fireFilter() {
            render(controller.query(
                txtFrom.getText(),
                txtTo.getText(),
                txtSearch.getText(),
                currentPage
                // Controller tự lấy sortCol và asc từ internal state của nó
            ));
        }

        // ── Helpers ────────────────────────────────────────────────
        private int parseMa(String formatted) {
            return Integer.parseInt(formatted.replaceAll("\\D", ""));
        }

        private String nvl(String s) { return s == null ? "" : s; }

        private void openDetail(int id) {
            Window parent = SwingUtilities.getWindowAncestor(table);
            InvoiceDetailDialog d = new InvoiceDetailDialog((Frame) parent, id);
            d.setLocationRelativeTo(parent);
            d.setVisible(true);
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
                    String maHD = table.getValueAt(currentRow, 0).toString();
                    int id = Integer.parseInt(maHD.replaceAll("\\D", ""));
                    Window parent = SwingUtilities.getWindowAncestor(table);
                    InvoiceDetailDialog d = new InvoiceDetailDialog((Frame) parent, id);
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
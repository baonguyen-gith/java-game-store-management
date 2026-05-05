package otkhongluong.gamestoremanagement.view.panel;
import otkhongluong.gamestoremanagement.view.dialog.RentDetailDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentEditDialog; 
import otkhongluong.gamestoremanagement.view.dialog.RentReturnDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentAddDialog;
import otkhongluong.gamestoremanagement.dao.PhieuThueDAO;
import otkhongluong.gamestoremanagement.model.PhieuThue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.util.function.IntConsumer;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import javax.swing.table.TableCellEditor;
import javax.swing.BorderFactory;
import javax.swing.border.CompoundBorder;
import javax.swing.BorderFactory;
import javax.swing.*;

import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class RentPanel extends JPanel {

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
    private JTextField txtFrom, txtTo, txtSearch;
    private JLabel lblPage;
    private JPanel paginationPanel;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 8;

    private java.util.List<PhieuThue> allData;
    private java.util.List<PhieuThue> currentPageData;

    public RentPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        loadData();
    }

    /* TOP BAR — date pickers + search */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        bar.add(labeledField("Từ ngày", txtFrom = datePicker()));
        bar.add(labeledField("Đến ngày", txtTo = datePicker()));
        bar.add(labeledSearch());

        return bar;
    }

    private JPanel labeledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BG_DARK);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_WHITE);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField datePicker() {
        JTextField tf = new JTextField("dd/mm/yyyy", 11) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
                // calendar icon
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
                g2.drawString("📅", getWidth() - 28, getHeight() / 2 + 5);
                g2.dispose();
            }
        };
        styleTextField(tf);
        return tf;
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
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
                    g2.drawString("🔍", 8, getHeight() / 2 + 5);
                }
                g2.dispose();
            }
        };
        styleTextField(txtSearch);
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { currentPage = 1; renderPage(); }
        });

        RoundButton btnAdd = new RoundButton("+", BTN_ADD, Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(40, 40));
        btnAdd.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            RentAddDialog dialog = new RentAddDialog((Frame) parent);
            dialog.setVisible(true);

            loadData();
        });

        row.add(txtSearch, BorderLayout.CENTER);
        row.add(btnAdd, BorderLayout.EAST);
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

    /* TABLE */
    private JScrollPane buildTable() {
        String[] cols = {
                            "Mã PT", "Mã NV", "Khách hàng", "SĐT",
                            "Ngày thuê", "Ngày trả DK", "Trạng thái", "Chi tiết"
                        };
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
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
        table.setForeground(TEXT_WHITE);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString());
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(PURPLE_HEADER);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
                return lbl;
            }
        });
        header.setBackground(PURPLE_HEADER);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createEmptyBorder());

        // Column widths
        int[] widths = {80, 80, 160, 120, 130, 130, 120, 80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // "Chi tiết" button column
        table.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer("Xem", ACCENT));
        table.getColumnModel().getColumn(7).setCellEditor(
            new ButtonEditor(new JCheckBox(), table, "Xem")
        );

        // Double click row
        table.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                String maPT = table.getValueAt(row, 0).toString();
                int id = Integer.parseInt(maPT.replaceAll("\\D", ""));
                Window parent = SwingUtilities.getWindowAncestor(table);
                RentDetailDialog dialog = new RentDetailDialog((Frame) parent, id);
                dialog.setVisible(true);
            }
        }
    });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(PURPLE_HEADER, 1, true));
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(PURPLE_ALT);
        return sp;
    }

    /* BOTTOM BAR — pagination + buttons */
    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(14, 0, 0, 0));

        // Pagination (left)
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        paginationPanel.setBackground(BG_DARK);
        rebuildPagination(paginationPanel);
        bar.add(paginationPanel, BorderLayout.WEST);

        // Sửa / Xóa (right)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(BG_DARK);

        RoundButton btnEdit = new RoundButton("✏  Sửa", BTN_EDIT, BG_DARK);
        btnEdit.setPreferredSize(new Dimension(110, 40));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn phiếu thuê để sửa!");
                return;
            }

            PhieuThue pt = currentPageData.get(row);
            int id = pt.getMaPT();

            Window parent = SwingUtilities.getWindowAncestor(this);
            RentEditDialog dialog = new RentEditDialog((Frame) parent, pt);
            dialog.setVisible(true);

            loadData(); // refresh
        });
        
        RoundButton btnReturn = new RoundButton("↩  Trả CD", new Color(255, 165, 0), BG_DARK);
        btnReturn.setPreferredSize(new Dimension(130, 40));

        btnReturn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn phiếu thuê để trả!");
                return;
            }

            int id = Integer.parseInt(
                tableModel.getValueAt(row, 0).toString().replaceAll("\\D", "")
            );

            Window parent = SwingUtilities.getWindowAncestor(this);
            RentReturnDialog dialog = new RentReturnDialog((Frame) parent, id);
            dialog.setVisible(true);

            loadData(); // refresh
        });

        RoundButton btnDelete = new RoundButton("🗑  Xóa", BTN_DELETE, BG_DARK);
        btnDelete.setPreferredSize(new Dimension(110, 40));
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn phiếu thuê để xóa!"); return; }
            int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận xóa phiếu thuê " + tableModel.getValueAt(row, 0) + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tableModel.removeRow(row);
                JOptionPane.showMessageDialog(this, "Đã xóa!");
            }
        });

        btnPanel.add(btnEdit);
        btnPanel.add(btnReturn);
        btnPanel.add(btnDelete);
        bar.add(btnPanel, BorderLayout.EAST);

        return bar;
    }

    private void rebuildPagination(JPanel panel) {
        panel.removeAll();
        List<PhieuThue> filtered = getFilteredData(); // Giữ nguyên phương thức lọc dữ liệu
        final int total = Math.max(1,
            (int) Math.ceil((double) filtered.size() / PAGE_SIZE)
        );

        for (int i = 1; i <= Math.min(total, 4); i++) {
            final int pg = i;
            RoundButton btn = new RoundButton(String.valueOf(i),
                pg == currentPage ? ACCENT : INPUT_BG, TEXT_WHITE);
            btn.setPreferredSize(new Dimension(36, 36));
            btn.addActionListener(e -> { currentPage = pg; renderPage(); });
            panel.add(btn);
        }
        if (total > 4) {
            RoundButton btnNext = new RoundButton(">>", INPUT_BG, TEXT_WHITE);
            btnNext.setPreferredSize(new Dimension(44, 36));
            btnNext.addActionListener(e -> {
                if (currentPage < total) { currentPage++; renderPage(); }
            });
            panel.add(btnNext);
        }
        panel.revalidate();
        panel.repaint();
    }

    private void loadData() {
        PhieuThueDAO dao = new PhieuThueDAO();
        allData = dao.findAll();
        renderPage();
    }

    private java.util.List<PhieuThue> getFilteredData() {
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();

        return allData == null ? java.util.Collections.emptyList() :
            allData.stream()
                .filter(pt -> keyword.isEmpty()
                    || pt.getTenKhachHang().toLowerCase().contains(keyword)
                    || pt.getMaPTFormatted().toLowerCase().contains(keyword)
                    || pt.getSoDienThoai().toLowerCase().contains(keyword))
                .collect(java.util.stream.Collectors.toList());
    }

    private void renderPage() {
        tableModel.setRowCount(0);

        List<PhieuThue> filtered = getFilteredData();

        int totalPage = (int) Math.ceil((double) filtered.size() / PAGE_SIZE);
        if (currentPage > totalPage) currentPage = totalPage == 0 ? 1 : totalPage;

        int from = (currentPage - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filtered.size());

        currentPageData = filtered.subList(from, to);

        for (PhieuThue pt : currentPageData) {
            tableModel.addRow(new Object[]{
                pt.getMaPTFormatted(),
                pt.getMaNVFormatted(),
                pt.getTenKhachHang(),
                pt.getSoDienThoai(),
                formatDate(pt.getNgayThue()),
                formatDate(pt.getNgayTraDuKien()),   // 🔥 thêm
                pt.getTrangThai(),       // 🔥 thêm
                "Xem"
            });
        }

        rebuildPagination(paginationPanel);
    }
    
    // Inner classes RoundButton, ButtonRenderer, ButtonEditor giữ nguyên hoặc tùy chỉnh theo phong cách của bạn
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
        private final java.util.function.IntConsumer onClick;
        private int currentRow;

        ButtonEditor(JCheckBox checkBox, JTable table, String type) {
            super(checkBox);

            // ✅ GÁN TRƯỚC
            this.onClick = row -> {
                String maPT = table.getValueAt(row, 0).toString();
                int id = Integer.parseInt(maPT.replaceAll("\\D", ""));

                Window parent = SwingUtilities.getWindowAncestor(table);
                RentDetailDialog dialog = new RentDetailDialog((Frame) parent, id);
                dialog.setVisible(true);
            };

            JButton btn = new JButton("Xem");
            btn.setBackground(ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);

            // ✅ DÙNG SAU
            btn.addActionListener(e -> {
                fireEditingStopped();
                onClick.accept(currentRow);
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
    
    private String formatDate(java.time.LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
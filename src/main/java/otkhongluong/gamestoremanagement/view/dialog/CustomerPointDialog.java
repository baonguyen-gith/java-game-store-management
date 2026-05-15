package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.controller.PointController;
import otkhongluong.gamestoremanagement.controller.PointController.ActionResult;
import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.model.Point;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dialog quản lý điểm tích lũy của 1 khách hàng.
 * Cho phép: Cộng điểm | Trừ điểm | Sửa điểm | Xóa bản ghi lịch sử (+ hoàn tác).
 */
public class CustomerPointDialog extends JDialog {

    /* ============ COLORS ============ */
    private static final Color BG_DARK       = new Color(35, 20, 85);
    private static final Color BG_CARD       = Color.WHITE;
    private static final Color PURPLE_HEADER = new Color(155, 135, 245);
    private static final Color PURPLE_ROW    = new Color(245, 242, 255);
    private static final Color PURPLE_ALT    = Color.WHITE;
    private static final Color ACCENT        = new Color(130, 90, 230);
    private static final Color TEXT_WHITE    = Color.WHITE;
    private static final Color TEXT_DARK     = new Color(40, 40, 40);
    private static final Color BTN_GREEN     = new Color(104, 211, 145);
    private static final Color BTN_RED       = new Color(252, 129, 129);
    private static final Color BTN_BLUE      = new Color(99, 179, 237);
    private static final Color BTN_ORANGE    = new Color(246, 173, 85);

    /* ============ FONTS ============ */
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_CELL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_POINT  = new Font("Segoe UI", Font.BOLD, 28);

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /* ============ STATE ============ */
    private final Customer               customer;
    private final PointController   controller;
    private final Runnable               onChanged; // callback để CustomerPanel reload

    /* ============ COMPONENTS ============ */
    private JLabel       lblDiem;
    private JTable       table;
    private DefaultTableModel tableModel;

    // ================================================================
    public CustomerPointDialog(Frame parent, Customer customer, Runnable onChanged) {
        super(parent, "Quản lý điểm – " + customer.getHoTen(), true);
        this.customer   = customer;
        this.controller = new PointController();
        this.onChanged  = onChanged;

        setSize(780, 560);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_DARK);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        loadHistory();
    }

    /* ======================================================
        HEADER – thông tin KH + điểm hiện tại
    ====================================================== */
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(20, 0));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(18, 20, 10, 20));

        // Bên trái: tên + mã KH
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(BG_DARK);

        JLabel lblName = new JLabel(customer.getHoTen());
        lblName.setFont(FONT_TITLE);
        lblName.setForeground(TEXT_WHITE);

        JLabel lblMa = new JLabel("KH" + String.format("%03d", customer.getMaKH())
                                  + "   |   SĐT: " + nvl(customer.getSdt()));
        lblMa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMa.setForeground(new Color(180, 170, 220));

        left.add(lblName);
        left.add(Box.createVerticalStrut(4));
        left.add(lblMa);

        // Bên phải: điểm hiện tại (to, nổi bật)
        JPanel right = new JPanel(new BorderLayout(8, 0));
        right.setBackground(new Color(60, 40, 120));
        right.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(10, 20, 10, 20)
        ));
        right.setPreferredSize(new Dimension(200, 70));

        JLabel lblTitle = new JLabel("Điểm tích lũy", SwingConstants.CENTER);
        lblTitle.setFont(FONT_LABEL);
        lblTitle.setForeground(new Color(180, 170, 220));

        lblDiem = new JLabel(String.valueOf(customer.getDiemTichLuy()), SwingConstants.CENTER);
        lblDiem.setFont(FONT_POINT);
        lblDiem.setForeground(new Color(104, 211, 145));

        right.add(lblTitle, BorderLayout.NORTH);
        right.add(lblDiem,  BorderLayout.CENTER);

        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    /* ======================================================
        CENTER – bảng lịch sử điểm
    ====================================================== */
    private JScrollPane buildCenter() {
        String[] cols = {"#", "Loại", "Số Điểm", "Ngày", "Ghi Chú"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (c instanceof JLabel)
                    ((JLabel) c).setHorizontalAlignment(col == 4 ? SwingConstants.LEFT : SwingConstants.CENTER);
                if (isRowSelected(row)) {
                    c.setBackground(ACCENT); c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? PURPLE_ROW : PURPLE_ALT);
                    // Màu cột Loại
                    if (col == 1) {
                        Object val = getValueAt(row, col);
                        if (val != null && val.toString().contains("Cộng"))
                            c.setForeground(new Color(39, 174, 96));
                        else
                            c.setForeground(new Color(192, 57, 43));
                    } else {
                        c.setForeground(TEXT_DARK);
                    }
                }
                return c;
            }
        };

        table.setFont(FONT_CELL);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setBackground(PURPLE_ALT);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString());
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(PURPLE_HEADER);
                lbl.setHorizontalAlignment(c == 4 ? SwingConstants.LEFT : SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(8, 12, 8, 12));
                return lbl;
            }
        });
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createEmptyBorder());

        // Column widths
        int[] widths = {50, 90, 90, 140, 300};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new CompoundBorder(
            new EmptyBorder(0, 20, 0, 20),
            new LineBorder(PURPLE_HEADER, 1, true)
        ));
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(PURPLE_ALT);
        return sp;
    }

    /* ======================================================
        BUTTONS – Cộng | Trừ | Sửa | Xóa lịch sử | Đóng
    ====================================================== */
    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 14));
        p.setBackground(BG_DARK);

        RoundBtn btnCong  = new RoundBtn("➕  Cộng điểm",  BTN_GREEN,  Color.WHITE);
        RoundBtn btnTru   = new RoundBtn("➖  Trừ điểm",   BTN_RED,    Color.WHITE);
        RoundBtn btnSua   = new RoundBtn("✏  Sửa điểm",   BTN_BLUE,   Color.WHITE);
        RoundBtn btnXoa   = new RoundBtn("🗑  Xóa lịch sử", BTN_ORANGE, Color.WHITE);
        RoundBtn btnDong  = new RoundBtn("Đóng",            new Color(100, 100, 120), Color.WHITE);

        Dimension btnSize = new Dimension(150, 40);
        for (RoundBtn b : new RoundBtn[]{btnCong, btnTru, btnSua, btnXoa, btnDong})
            b.setPreferredSize(btnSize);

        btnCong.addActionListener(e -> doAction("cong"));
        btnTru.addActionListener(e  -> doAction("tru"));
        btnSua.addActionListener(e  -> doAction("sua"));
        btnXoa.addActionListener(e  -> doDeleteHistory());
        btnDong.addActionListener(e -> dispose());

        p.add(btnCong); p.add(btnTru); p.add(btnSua); p.add(btnXoa); p.add(btnDong);
        return p;
    }

    /* ======================================================
        ACTIONS
    ====================================================== */

    /**
     * Hiện input dialog cho Cộng / Trừ / Sửa, gọi controller, cập nhật UI.
     */
    private void doAction(String type) {
        String title;
        switch (type) {
            case "cong":
                title = "Cộng điểm";
                break;
            case "tru":
                title = "Trừ điểm";
                break;
            default:
                title = "Sửa điểm (nhập giá trị mới)";
                break;
        };

        // --- Form nhập liệu ---
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setBorder(new EmptyBorder(4, 0, 4, 0));

        String diemLabel = type.equals("sua") ? "Điểm mới:" : "Số điểm:";
        form.add(new JLabel(diemLabel));
        JTextField txtDiem = new JTextField(
            type.equals("sua") ? String.valueOf(customer.getDiemTichLuy()) : "");
        form.add(txtDiem);
        form.add(new JLabel("Ghi chú (tùy chọn):"));
        JTextField txtNote = new JTextField();
        form.add(txtNote);

        int ok = JOptionPane.showConfirmDialog(this, form, title,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        // --- Gọi controller ---
        ActionResult result;
        switch (type) {
            case "cong":
                result = controller.congDiem(customer.getMaKH(), txtDiem.getText(), txtNote.getText());
                break;
            case "tru":
                result = controller.truDiem(customer.getMaKH(), txtDiem.getText(), txtNote.getText());
                break;
            default:
                result = controller.suaDiem(customer.getMaKH(), txtDiem.getText(), txtNote.getText());
                break;
        };

        // --- Phản hồi ---
        JOptionPane.showMessageDialog(this, result.message,
            result.success ? "Thành công" : "Lỗi",
            result.success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        if (result.success) {
            refreshDiem();
            if (onChanged != null) onChanged.run();
        }
    }

    /** Xóa 1 bản ghi lịch sử được chọn (đồng thời hoàn tác điểm). */
    private void doDeleteHistory() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn bản ghi lịch sử cần xóa!");
            return;
        }

        int maLS = (int) tableModel.getValueAt(row, 0);
        String loai  = tableModel.getValueAt(row, 1).toString();
        int    diem  = (int) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Xóa bản ghi " + loai + " " + diem + " điểm?\n"
            + "⚠ Hệ thống sẽ tự động hoàn tác số điểm tương ứng!",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        ActionResult result = controller.xoaLichSu(maLS);
        JOptionPane.showMessageDialog(this, result.message,
            result.success ? "Thành công" : "Lỗi",
            result.success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        if (result.success) {
            refreshDiem();
            if (onChanged != null) onChanged.run();
        }
    }

    /* ======================================================
        DATA
    ====================================================== */

    /** Load toàn bộ lịch sử điểm vào bảng */
    private void loadHistory() {
        tableModel.setRowCount(0);
        List<Point> list = controller.getLichSu(customer.getMaKH());
        for (Point d : list) {
            tableModel.addRow(new Object[]{
                d.getMaLS(),
                d.getLoaiDisplay(),
                d.getSoDiem(),
                d.getNgay() != null ? d.getNgay().format(DTF) : "",
                nvl(d.getGhiChu())
            });
        }
    }

    /**
     * Sau mỗi thao tác: reload lịch sử + cập nhật nhãn điểm hiện tại.
     * Lấy điểm mới trực tiếp từ DB thông qua controller (tránh cache cũ).
     */
    private void refreshDiem() {
        // Cập nhật điểm trong object customer từ DB
        List<Point> list = controller.getLichSu(customer.getMaKH());

        // Tính lại điểm từ lịch sử (hoặc reload từ DB qua CustomerController)
        // Dùng cách đơn giản: tái dùng CustomerController để lấy điểm mới
        otkhongluong.gamestoremanagement.controller.CustomerController cc =
            new otkhongluong.gamestoremanagement.controller.CustomerController();
        Customer fresh = cc.getById(customer.getMaKH());
        if (fresh != null) {
            customer.setDiemTichLuy(fresh.getDiemTichLuy());
            lblDiem.setText(String.valueOf(fresh.getDiemTichLuy()));
        }

        loadHistory();
    }

    /* ======================================================
        HELPERS
    ====================================================== */
    private String nvl(String s) { return s != null ? s : ""; }

    /* ======================================================
        INNER: RoundBtn
    ====================================================== */
    static class RoundBtn extends JButton {
        private final Color bg;
        RoundBtn(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
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
}
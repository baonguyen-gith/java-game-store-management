package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.model.PhieuThue;
import otkhongluong.gamestoremanagement.service.ThueService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RentEditDialog extends JDialog {

    private static final Color BG_DARK       = new Color(35, 20, 85);
    private static final Color BG_CARD       = new Color(55, 35, 110);
    private static final Color PURPLE_HEADER = new Color(155, 135, 245);
    private static final Color ACCENT        = new Color(130, 90, 230);
    private static final Color TEXT_WHITE    = Color.WHITE;
    private static final Color TEXT_DARK     = new Color(40, 40, 40);
    private static final Color TEXT_MUTED    = new Color(120, 120, 140);
    private static final Color INPUT_BG      = Color.WHITE;
    private static final Color BTN_SAVE      = new Color(104, 211, 145);
    private static final Color BTN_CANCEL    = new Color(252, 129, 129);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_CELL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);

    private final ThueService service = new ThueService();
    private final PhieuThue pt;

    private JTextField txtTenNV;
    private JTextField txtTienCoc;

    public RentEditDialog(Frame parent, PhieuThue pt) {
        super(parent, "Sửa Phiếu Thuê — PT" + pt.getMaPT(), true);
        this.pt = pt;
        setSize(480, 340);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        getRootPane().setBorder(BorderFactory.createLineBorder(ACCENT, 2));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        loadData();
    }
    
    static class RoundBtn extends JButton {
        private final Color bg, fg;
        RoundBtn(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg; this.fg = fg;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(18, 24, 10, 24));

        JLabel title = new JLabel("✏  SỬA PHIẾU THUÊ  —  PT" + pt.getMaPT());
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_WHITE);
        p.add(title, BorderLayout.WEST);

        JPanel sep = new JPanel();
        sep.setBackground(PURPLE_HEADER);
        sep.setPreferredSize(new Dimension(0, 1));
        p.add(sep, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout(0, 16));
        outer.setBackground(BG_DARK);
        outer.setBorder(new EmptyBorder(20, 24, 0, 24));

        // Info-only: ten KH + trang thai (read-only, hien thi de tham chieu)
        JPanel infoRow = new JPanel(new GridLayout(1, 2, 10, 0));
        infoRow.setBackground(BG_DARK);
        JLabel lblKH = readOnlyCard("Khách hàng", nvl(pt.getTenKhachHang()));
        JLabel lblTT = readOnlyCard("Trạng thái",
            "DaTra".equalsIgnoreCase(pt.getTrangThai()) ? "✔ Đã trả" : "⏳ Đang thuê");
        infoRow.add(lblKH.getParent()); // card panel
        infoRow.add(lblTT.getParent());

        // Editable fields
        JPanel editGrid = new JPanel(new GridLayout(2, 1, 0, 14));
        editGrid.setBackground(BG_DARK);

        txtTenNV   = styledInput(nvl(pt.getTenNhanVien()));
        txtTienCoc = styledInput(String.format("%.0f", pt.getTienCoc()));
        setupAutoComplete(txtTenNV, service.getAllNhanVienNames());

        editGrid.add(formRow("Nhân viên phụ trách", txtTenNV));
        editGrid.add(formRow("Tiền cọc  (VNĐ) — chỉ sửa khi nhập nhầm", txtTienCoc));

        JLabel hint = new JLabel(
            "* Để thay đổi ngày trả hoặc trạng thái, dùng nút Gia hạn / Trả CD.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(TEXT_MUTED);

        outer.add(infoRow,  BorderLayout.NORTH);
        outer.add(editGrid, BorderLayout.CENTER);
        outer.add(hint,     BorderLayout.SOUTH);
        return outer;
    }

    /** Tra ve label de lay .getParent() = card panel */
    private JLabel readOnlyCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(new Color(55, 35, 110));
        card.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(TEXT_MUTED);
        JLabel lblVal = new JLabel(value);
        lblVal.setFont(FONT_HEADER);
        lblVal.setForeground(TEXT_WHITE);
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal,   BorderLayout.CENTER);
        return lblVal; // caller goi .getParent() de lay card
    }

    private JPanel formRow(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(BG_DARK);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MUTED);
        p.add(lbl,   BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField styledInput(String value) {
        JTextField tf = new JTextField(value);
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_DARK);
        tf.setCaretColor(ACCENT);
        tf.setFont(FONT_CELL);
        tf.setPreferredSize(new Dimension(0, 40));
        tf.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private void setupAutoComplete(JTextField field, List<String> data) {
        JPopupMenu popup  = new JPopupMenu();
        JList<String> lst = new JList<>();
        lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lst.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                field.setText(lst.getSelectedValue());
                popup.setVisible(false);
            }
        });
        field.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                String text = field.getText().toLowerCase();
                DefaultListModel<String> model = new DefaultListModel<>();
                for (String item : data)
                    if (item.toLowerCase().contains(text)) model.addElement(item);
                if (model.isEmpty()) { popup.setVisible(false); return; }
                lst.setModel(model);
                popup.removeAll();
                popup.add(new JScrollPane(lst));
                popup.show(field, 0, field.getHeight());
            }
        });
    }

    private void loadData() { /* da set trong styledInput */ }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(10, 24, 16, 24));

        JPanel sep = new JPanel();
        sep.setBackground(PURPLE_HEADER);
        sep.setPreferredSize(new Dimension(0, 1));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnRow.setBackground(BG_DARK);

        RoundBtn btnCancel = new RoundBtn("✕  Hủy", BTN_CANCEL, TEXT_WHITE);
        RoundBtn btnSave   = new RoundBtn("✔  Lưu", BTN_SAVE,   TEXT_DARK);
        btnCancel.setPreferredSize(new Dimension(110, 40));
        btnSave  .setPreferredSize(new Dimension(110, 40));
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> doSave());

        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        p.add(sep,    BorderLayout.NORTH);
        p.add(btnRow, BorderLayout.CENTER);
        return p;
    }

    private void doSave() {
        try {
            String tenNV = txtTenNV.getText().trim();
            double tienCoc = Double.parseDouble(
                txtTienCoc.getText().trim().replace(",", "").replace(".", ""));

            if (tenNV.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Tên nhân viên không được để trống!",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (tienCoc < 0) {
                JOptionPane.showMessageDialog(this,
                    "Tiền cọc không hợp lệ!",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            pt.setTenNhanVien(tenNV);
            pt.setTienCoc(tienCoc);

            boolean ok = service.updatePhieuThue(pt);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                    "Cập nhật thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Tiền cọc phải là số!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }

    // ... RoundBtn class giong cac dialog khac
}
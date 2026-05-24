package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.controller.UserController;
import otkhongluong.gamestoremanagement.model.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog thêm / sửa tài khoản.
 * - Hiển thị dropdown chọn nhân viên (tên + mã) thay vì nhập tay MaNV.
 * - Dropdown Role hiển thị tên rõ ràng thay vì nhập số.
 * - Khi sửa: pre-fill đúng nhân viên và role đang có.
 */
public class UserDialog extends JDialog {

    /* ── màu theo EmployeePanel ── */
    private static final Color BG_DARK      = new Color(35, 20, 85);
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color ACCENT       = new Color(130, 90, 230);
    private static final Color TEXT_WHITE   = Color.WHITE;
    private static final Color TEXT_DARK    = new Color(40, 40, 40);
    private static final Color BTN_SAVE     = new Color(104, 211, 145);
    private static final Color BTN_CANCEL   = new Color(252, 129, 129);
    private static final Font  FONT_LABEL   = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font  FONT_INPUT   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_BTN     = new Font("Segoe UI", Font.BOLD,  13);

    /* ── components ── */
    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cboRole;
    private JComboBox<String> cboNhanVien;   // hiển thị "NV001 — Nguyễn Văn A"

    /* ── data ── */
    private final UserController controller;
    private final User           editUser;      // null = thêm mới
    private final Runnable       onSuccess;

    /** Mảng maNV tương ứng với từng mục trong cboNhanVien */
    private int[] maNVArr;

    // ═══════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════
    public UserDialog(Frame parent,
                      User editUser,
                      UserController controller,
                      Runnable onSuccess) {
        super(parent, editUser == null ? "Thêm tài khoản" : "Sửa tài khoản", true);
        this.editUser   = editUser;
        this.controller = controller;
        this.onSuccess  = onSuccess;

        buildUI();
        if (editUser != null) fillForm();

        pack();
        setMinimumSize(new Dimension(460, 0));
        setResizable(false);
    }

    // ═══════════════════════════════════════════════════════
    // BUILD UI
    // ═══════════════════════════════════════════════════════
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        /* ── tiêu đề ── */
        JLabel title = new JLabel(
            editUser == null ? "  Thêm tài khoản mới" : "  Sửa tài khoản",
            JLabel.LEFT
        );
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_WHITE);
        title.setBorder(new EmptyBorder(18, 20, 12, 20));
        root.add(title, BorderLayout.NORTH);

        /* ── form card ── */
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new EmptyBorder(0, 16, 16, 16),
            new CompoundBorder(
                new LineBorder(ACCENT, 1, true),
                new EmptyBorder(20, 24, 20, 24)
            )
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(7, 4, 7, 4);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        /* nhân viên */
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        card.add(label("Nhân viên *"), gc);
        gc.gridx = 1; gc.weightx = 1;
        cboNhanVien = buildNhanVienCombo();
        card.add(cboNhanVien, gc);

        /* username */
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        card.add(label("Username *"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtUsername = styledField();
        card.add(txtUsername, gc);

        /* password */
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        card.add(label(editUser == null ? "Password *" : "Password mới\n(bỏ trống = giữ cũ)"), gc);
        gc.gridx = 1; gc.weightx = 1;
        txtPassword = new JPasswordField();
        stylePasswordField(txtPassword);
        card.add(txtPassword, gc);

        /* role */
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0;
        card.add(label("Phân quyền *"), gc);
        gc.gridx = 1; gc.weightx = 1;
        cboRole = new JComboBox<>(new String[]{
            "1 — Admin",
            "2 — Quản lý",
            "3 — Nhân viên"
        });
        cboRole.setFont(FONT_INPUT);
        cboRole.setBackground(Color.WHITE);
        card.add(cboRole, gc);

        root.add(card, BorderLayout.CENTER);

        /* ── nút ── */
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnRow.setBackground(BG_DARK);

        JButton btnCancel = roundBtn("Hủy", BTN_CANCEL);
        JButton btnSave   = roundBtn(editUser == null ? "Thêm" : "Lưu", BTN_SAVE);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());

        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        root.add(btnRow, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ═══════════════════════════════════════════════════════
    // DROPDOWN NHÂN VIÊN
    // ═══════════════════════════════════════════════════════
    private JComboBox<String> buildNhanVienCombo() {
        List<Object[]> nvData = controller.getNhanVienDropdown(
            editUser == null ? -1 : editUser.getMaNV()
        );

        maNVArr = new int[nvData.size()];
        String[] labels = new String[nvData.size()];

        for (int i = 0; i < nvData.size(); i++) {
            maNVArr[i]       = (int)     nvData.get(i)[0];
            String ma        = (String)  nvData.get(i)[1];
            String ten       = (String)  nvData.get(i)[2];
            boolean daCoTK   = (boolean) nvData.get(i)[3];
            labels[i] = ma + " — " + ten + (daCoTK ? "  ✓ đã có TK" : "");
        }

        JComboBox<String> cbo = new JComboBox<>(labels);
        cbo.setFont(FONT_INPUT);
        cbo.setBackground(Color.WHITE);
        return cbo;
    }

    // ═══════════════════════════════════════════════════════
    // FILL FORM KHI SỬA
    // ═══════════════════════════════════════════════════════
    private void fillForm() {
        txtUsername.setText(editUser.getUsername());

        // Chọn đúng nhân viên trong dropdown
        for (int i = 0; i < maNVArr.length; i++) {
            if (maNVArr[i] == editUser.getMaNV()) {
                cboNhanVien.setSelectedIndex(i);
                break;
            }
        }

        // Chọn đúng role (index = maRole - 1)
        int roleIndex = editUser.getMaRole() - 1;
        if (roleIndex >= 0 && roleIndex < cboRole.getItemCount())
            cboRole.setSelectedIndex(roleIndex);
    }

    // ═══════════════════════════════════════════════════════
    // SAVE
    // ═══════════════════════════════════════════════════════
    private void handleSave() {
        String username  = txtUsername.getText().trim();
        String password  = new String(txtPassword.getPassword()).trim();
        int    roleIndex = cboRole.getSelectedIndex();      // 0-based
        int    maRole    = roleIndex + 1;                   // 1=Admin, 2=QuanLy, 3=NhanVien
        int    nvIndex   = cboNhanVien.getSelectedIndex();

        if (nvIndex < 0 || nvIndex >= maNVArr.length) {
            showError("Vui lòng chọn nhân viên!");
            return;
        }
        int maNV = maNVArr[nvIndex];

        String error;
        if (editUser == null) {
            // THÊM MỚI
            if (password.isEmpty()) { showError("Password không được để trống!"); return; }
            error = controller.addUser(username, password, String.valueOf(maRole), maNV);
        } else {
            // SỬA — password rỗng = giữ nguyên
            error = controller.updateUser(
                editUser.getMaUser(), username, password, String.valueOf(maRole), maNV
            );
        }

        if (error != null) {
            showError(error);
        } else {
            JOptionPane.showMessageDialog(this,
                editUser == null ? "Thêm tài khoản thành công!" : "Cập nhật thành công!",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            if (onSuccess != null) onSuccess.run();
            dispose();
        }
    }

    // ═══════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_DARK);
        return l;
    }

    private JTextField styledField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_INPUT);
        tf.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        tf.setPreferredSize(new Dimension(220, 34));
        return tf;
    }

    private void stylePasswordField(JPasswordField pf) {
        pf.setFont(FONT_INPUT);
        pf.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        pf.setPreferredSize(new Dimension(220, 34));
    }

    private JButton roundBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setBackground(bg);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(100, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
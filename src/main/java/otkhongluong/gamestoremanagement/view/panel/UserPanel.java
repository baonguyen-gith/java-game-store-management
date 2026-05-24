package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.controller.UserController;
import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.view.dialog.UserDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserPanel extends JPanel {

    // ===== UI =====
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;

    // ===== MVC =====
    private final UserController controller = new UserController();
    private final User currentUser;

    public UserPanel(User currentUser) {
        this.currentUser = currentUser;
        initUI();
        initPermissions();
        loadData();
        initEvents();
    }

    // ===== GIAO DIỆN =====
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 50));

        JLabel title = new JLabel("QUẢN LÝ TÀI KHOẢN", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        String[] cols = {"ID", "Username", "Role", "Mã NV", "Tên Nhân Viên"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);

        btnAdd    = new JButton("➕ Thêm");
        btnEdit   = new JButton("✏️ Sửa");
        btnDelete = new JButton("❌ Xóa");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actions.setBackground(new Color(20, 20, 50));
        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);

        add(title,                  BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(actions,                BorderLayout.SOUTH);
    }

    // ===== PHÂN QUYỀN =====
    private void initPermissions() {
        boolean isAdmin = currentUser.getMaRole() == 1;
        btnAdd.setEnabled(isAdmin);
        btnEdit.setEnabled(isAdmin);
        btnDelete.setEnabled(isAdmin);
    }

    // ===== SỰ KIỆN =====
    private void initEvents() {
        btnAdd.addActionListener(e -> handleAdd());
        btnEdit.addActionListener(e -> handleEdit());
        btnDelete.addActionListener(e -> handleDelete());
        // Double-click mở dialog sửa
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) handleEdit();
            }
        });
    }

    // ===== THÊM =====
    private void handleAdd() {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        Frame frame = (ancestor instanceof Frame) ? (Frame) ancestor : null;
        UserDialog dialog = new UserDialog(frame, null, controller, this::loadData);
        dialog.setAlwaysOnTop(true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    // ===== SỬA =====
    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng!");
            return;
        }

        int maUser = (int) tableModel.getValueAt(row, 0);
        User editUser = controller.getUserById(maUser);
        if (editUser == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy user!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Window ancestor = SwingUtilities.getWindowAncestor(this);
        Frame frame = (ancestor instanceof Frame) ? (Frame) ancestor : null;
        UserDialog dialog = new UserDialog(frame, editUser, controller, this::loadData);
        dialog.setAlwaysOnTop(true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ===== XÓA =====
    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng!");
            return;
        }

        // Ngăn admin tự xóa chính mình
        int maUser = (int) tableModel.getValueAt(row, 0);
        if (maUser == currentUser.getMaUser()) {
            JOptionPane.showMessageDialog(this,
                "Không thể xóa tài khoản đang đăng nhập!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, "Bạn có chắc muốn xóa user này?", "Xác nhận", JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String error = controller.deleteUser(maUser);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadData();
        }
    }

    // ===== LOAD DATA =====
    private void loadData() {
        tableModel.setRowCount(0);
        List<Object[]> list = controller.getAllUsersWithEmployee();
        for (Object[] row : list) tableModel.addRow(row);
    }
}
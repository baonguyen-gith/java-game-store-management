package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.controller.UserController;
import otkhongluong.gamestoremanagement.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserPanel extends JPanel {

    // ===== UI COMPONENTS =====
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;

    // ===== MVC =====
    private final UserController controller = new UserController();
    private final User currentUser;

    // ===========================
    public UserPanel(User currentUser) {
        this.currentUser = currentUser;
        initUI();
        initPermissions();
        loadData();
        initEvents();
    }

    // ===== KHỞI TẠO GIAO DIỆN =====
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 50));

        JLabel title = new JLabel("QUẢN LÝ TÀI KHOẢN", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // ✅ Thêm 2 cột: Mã NV và Tên NV
        String[] cols = {"ID", "Username", "Role", "Mã NV", "Tên Nhân Viên"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Căn chỉnh độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(130);  // Username
        table.getColumnModel().getColumn(2).setPreferredWidth(70);   // Role
        table.getColumnModel().getColumn(3).setPreferredWidth(80);   // Mã NV
        table.getColumnModel().getColumn(4).setPreferredWidth(180);  // Tên NV

        JScrollPane scroll = new JScrollPane(table);

        btnAdd    = new JButton("➕ Thêm");
        btnEdit   = new JButton("✏️ Sửa");
        btnDelete = new JButton("❌ Xóa");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actions.setBackground(new Color(20, 20, 50));
        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);

        add(title,   BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
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
    }

    // ===== XỬ LÝ THÊM =====
    private void handleAdd() {
        String username = JOptionPane.showInputDialog(this, "Nhập username:");
        if (username == null) return;

        String password = JOptionPane.showInputDialog(this, "Nhập password:");
        if (password == null) return;

        String roleStr = JOptionPane.showInputDialog(this, "Role (1=Admin, 2=Staff):");
        if (roleStr == null) return;

        String error = controller.addUser(username, password, roleStr);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadData();
        }
    }

    // ===== XỬ LÝ SỬA =====
    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng!");
            return;
        }

        // Cột 0 vẫn là MaUser (int)
        int id = (int) tableModel.getValueAt(row, 0);

        String username = JOptionPane.showInputDialog(this, "Username mới:");
        if (username == null) return;

        String password = JOptionPane.showInputDialog(this, "Password mới:");
        if (password == null) return;

        String roleStr = JOptionPane.showInputDialog(this, "Role (1=Admin, 2=Staff):");
        if (roleStr == null) return;

        String error = controller.updateUser(id, username, password, roleStr);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Sửa thành công!");
            loadData();
        }
    }

    // ===== XỬ LÝ XÓA =====
    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, "Bạn có chắc muốn xóa user này?", "Xác nhận", JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = (int) tableModel.getValueAt(row, 0);

        String error = controller.deleteUser(id);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadData();
        }
    }

    // ===== NẠP DỮ LIỆU VÀO BẢNG =====
    private void loadData() {
        tableModel.setRowCount(0);
        // ✅ Dùng getAllUsersWithEmployee() thay vì getAllUsers()
        //    để lấy thêm Mã NV (formatted "NV1") và Tên NV từ JOIN NHANVIEN
        List<Object[]> list = controller.getAllUsersWithEmployee();
        for (Object[] row : list) {
            // row = [MaUser(int), Username, Role(String), MaNVFormatted(String), HoTen(String)]
            tableModel.addRow(row);
        }
    }
}
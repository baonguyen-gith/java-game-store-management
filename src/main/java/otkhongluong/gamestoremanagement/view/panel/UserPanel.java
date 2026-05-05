package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.dao.UserDAO;
import otkhongluong.gamestoremanagement.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    private UserDAO userDAO = new UserDAO();
    private User currentUser;

    // 🔥 BUTTON phải khai báo global
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;

    public UserPanel(User user) {
        this.currentUser = user;

        setLayout(new BorderLayout());
        setBackground(new Color(20,20,50));

        // ===== TITLE =====
        JLabel title = new JLabel("QUẢN LÝ TÀI KHOẢN", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        // ===== TABLE =====
        String[] cols = {"ID", "Username", "Role"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);

        JScrollPane scroll = new JScrollPane(table);

        // ===== BUTTONS =====
        JPanel actions = new JPanel();

        btnAdd = new JButton("Thêm");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xóa");

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);

        // ===== ADD UI =====
        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        // ===== PHÂN QUYỀN UI =====
        if (currentUser.getMaRole() != 1) {
            btnAdd.setEnabled(false);
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
        }

        // ===== LOAD DATA =====
        loadData();

        // ===== EVENTS =====

        // ➕ Thêm user
        btnAdd.addActionListener(e -> {
            if (currentUser.getMaRole() != 1) {
                JOptionPane.showMessageDialog(this, "Bạn không có quyền!");
                return;
            }

            String username = JOptionPane.showInputDialog("Nhập username:");
            String password = JOptionPane.showInputDialog("Nhập password:");
            String roleStr = JOptionPane.showInputDialog("Role (1=Admin, 2=Staff):");

            try {
                int role = Integer.parseInt(roleStr);

                User user1 = new User();
                user1.setUsername(username);
                user1.setPassword(password);
                user1.setMaRole(role);

                boolean result = userDAO.insert(user1);

                if (result) {
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm thất bại!");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ!");
            }
        });

        // ✏️ Sửa user
        btnEdit.addActionListener(e -> {
            if (currentUser.getMaRole() != 1) {
                JOptionPane.showMessageDialog(this, "Bạn không có quyền!");
                return;
            }

            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Chọn 1 dòng!");
                return;
            }

            int id = (int) model.getValueAt(row, 0);

            String username = JOptionPane.showInputDialog("Username mới:");
            String password = JOptionPane.showInputDialog("Password mới:");
            String roleStr = JOptionPane.showInputDialog("Role (1=Admin, 2=Staff):");

            try {
                int role = Integer.parseInt(roleStr);

                User user1 = new User();
                user1.setMaUser(id);
                user1.setUsername(username);
                user1.setPassword(password);
                user1.setMaRole(role);

                boolean result = userDAO.update(user1);

                if (result) {
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Sửa thất bại!");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi dữ liệu!");
            }
        });

        // ❌ Xóa user
        btnDelete.addActionListener(e -> {
            if (currentUser.getMaRole() != 1) {
                JOptionPane.showMessageDialog(this, "Bạn không có quyền!");
                return;
            }

            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Chọn 1 dòng!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Xóa user?");
            if (confirm != JOptionPane.YES_OPTION) return;

            int id = (int) model.getValueAt(row, 0);

            boolean result = userDAO.delete(id);

            if (result) {
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        });
    }

    // ===== LOAD DATA =====
    private void loadData() {
        model.setRowCount(0);

        List<User> list = userDAO.findAll();
        for (User u : list) {
            model.addRow(new Object[]{
                    u.getMaUser(),
                    u.getUsername(),
                    u.getMaRole() == 1 ? "Admin" : "Staff"
            });
        }
    }
}
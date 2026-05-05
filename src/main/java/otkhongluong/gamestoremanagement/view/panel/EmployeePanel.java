package otkhongluong.gamestoremanagement.view.panel;
import otkhongluong.gamestoremanagement.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EmployeePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private User currentUser;

    public EmployeePanel(User user) {
        this.currentUser = user;

        setLayout(new BorderLayout());
        setBackground(new Color(20,20,50));

        // ===== TOP BAR =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(20,20,50));

        JLabel title = new JLabel("QUẢN LÝ NHÂN VIÊN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // 👉 NÚT TÀI KHOẢN GÓC PHẢI
        JButton btnUser = new JButton("Tài khoản");

        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnUser, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ===== TABLE =====
        String[] columns = {"ID", "Tên", "SĐT", "Chức vụ"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);

        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        // ===== BUTTON =====
        JPanel btnPanel = new JPanel();

        JButton btnAdd = new JButton("Thêm");
        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");

        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        add(btnPanel, BorderLayout.SOUTH);

        // ===== DEMO DATA =====
        model.addRow(new Object[]{"NV01","Nguyễn Văn A","0123456789","Admin"});

        // ===== EVENT =====
        btnUser.addActionListener(e -> openUserPanel());
    }

    // ===== MỞ USER PANEL =====
    private void openUserPanel() {
        try {
            JDialog dialog = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Quản lý tài khoản",
                    true
            );

            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(this);

            dialog.setLayout(new BorderLayout());
            dialog.add(new UserPanel(currentUser), BorderLayout.CENTER);

            dialog.setVisible(true);
            dialog.setAlwaysOnTop(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi mở UserPanel!");
        }
    }
}
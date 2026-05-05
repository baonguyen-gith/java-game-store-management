package otkhongluong.gamestoremanagement.view.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CustomerPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public CustomerPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(20,20,50));

        JLabel title = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(title, BorderLayout.NORTH);

        // ===== TABLE =====
        String[] columns = {"ID", "Tên", "SĐT", "Điểm"};
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
        model.addRow(new Object[]{"KH01","Trần Thị B","0987654321",120});
    }
}
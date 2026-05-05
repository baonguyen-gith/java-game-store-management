package otkhongluong.gamestoremanagement.view.panel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class GameManagePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public GameManagePanel() {

        setLayout(new BorderLayout());
        setBackground(new Color(20,20,50));

        add(createToolbar(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
    }

    // ================= TOOLBAR =================
    private JPanel createToolbar(){

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        bar.setBackground(new Color(20,20,50));

        JButton btnAdd = new JButton("➕ Thêm");
        JButton btnEdit = new JButton("✏ Sửa");
        JButton btnDelete = new JButton("🗑 Xóa");
        JButton btnRefresh = new JButton("🔄 Refresh");

        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDelete);
        bar.add(btnRefresh);

        return bar;
    }

    // ================= TABLE =================
    private JScrollPane createTable(){

        String[] column = {
                "Mã Game",
                "Tên Game",
                "Thể Loại",
                "Nền Tảng",
                "Giá Bán",
                "Giá Thuê"
        };

        model = new DefaultTableModel(column,0);

        // demo data
        model.addRow(new Object[]{"G01","GTA V","Action","PC",500000,30000});
        model.addRow(new Object[]{"G02","Minecraft","Sandbox","PC",300000,20000});
        model.addRow(new Object[]{"G03","FIFA 24","Sport","PS5",900000,50000});

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI",Font.PLAIN,14));

        JScrollPane scroll = new JScrollPane(table);

        return scroll;
    }
}
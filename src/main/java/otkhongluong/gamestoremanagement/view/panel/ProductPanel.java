package otkhongluong.gamestoremanagement.view.panel;

import javax.swing.*;
import java.awt.*;

public class ProductPanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel content;

    public ProductPanel() {

        setLayout(new BorderLayout());

        // ===== MENU =====
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        JButton btnProduct = new JButton("Sản phẩm");
        JButton btnStock   = new JButton("Tồn kho");

        menu.add(btnProduct);
        menu.add(btnStock);

        add(menu, BorderLayout.NORTH);

        // ===== CONTENT =====
        cardLayout = new CardLayout();
        content = new JPanel(cardLayout);

        content.add(createProductTable(), "PRODUCT");
        content.add(new StockPanel(), "STOCK"); // 👈 đưa tồn kho vào đây

        add(content, BorderLayout.CENTER);

        // ===== ACTION =====
        btnProduct.addActionListener(e -> cardLayout.show(content, "PRODUCT"));
        btnStock.addActionListener(e -> cardLayout.show(content, "STOCK"));
    }

    private JPanel createProductTable() {

        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"ID","Tên","Loại","Giá","Số lượng"};
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(cols,0));

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }
}
package otkhongluong.gamestoremanagement.view.panel;

import javax.swing.*;
import java.awt.*;

public class StockPanel extends JPanel {

    public StockPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(20,20,50));

        JLabel title = new JLabel("QUẢN LÝ TỒN KHO CD", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        String[] cols = {"ID", "Game", "Số lượng"};
        JTable table = new JTable(new Object[][]{}, cols);

        add(title, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
package otkhongluong.gamestoremanagement.view.panel;

import javax.swing.*;
import java.awt.*;

public class PointPanel extends JPanel {

    public PointPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(20,20,50));

        JLabel title = new JLabel("ĐIỂM KHÁCH HÀNG", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        String[] cols = {"Khách hàng", "Điểm"};
        JTable table = new JTable(new Object[][]{}, cols);

        add(title, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
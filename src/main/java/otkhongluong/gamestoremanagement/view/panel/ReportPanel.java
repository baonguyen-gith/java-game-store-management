package otkhongluong.gamestoremanagement.view.panel;

import javax.swing.*;
import java.awt.*;

public class ReportPanel extends JPanel {

    public ReportPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(20,20,50));

        JLabel title = new JLabel("THỐNG KÊ & BÁO CÁO", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel content = new JLabel("Biểu đồ doanh thu (sẽ làm sau)", JLabel.CENTER);
        content.setForeground(Color.WHITE);

        add(title, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }
}
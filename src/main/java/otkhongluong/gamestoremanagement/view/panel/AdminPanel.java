package otkhongluong.gamestoremanagement.view.panel;
import otkhongluong.gamestoremanagement.model.User;

import javax.swing.*;
import java.awt.*;

public class AdminPanel extends JPanel {
    private User currentUser;
    
    public AdminPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(new Color(20,20,50));

        // ===== TABS =====
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Nhân viên", new EmployeePanel(currentUser));
        tabbedPane.addTab("Khách hàng", new CustomerPanel());
        tabbedPane.addTab("Game", new GameManagePanel());
        tabbedPane.addTab("Sản phẩm", new ProductPanel());

        // (optional) style cho đồng bộ theme tối
        tabbedPane.setBackground(new Color(20,20,50));
        tabbedPane.setForeground(Color.WHITE);

        add(tabbedPane, BorderLayout.CENTER);
    }
}
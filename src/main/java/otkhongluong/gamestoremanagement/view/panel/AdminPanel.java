package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.User;

import javax.swing.*;
import java.awt.*;

public class AdminPanel extends JPanel {

    private User currentUser;

    public AdminPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(new Color(13, 13, 35)); // ✅ đồng bộ với SalesPanel

        // ===== TABS =====
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(13, 13, 35)); // ✅ đồng bộ nền tab bar
        tabbedPane.setOpaque(true);                      // 🔥 bắt buộc như SalesPanel
        tabbedPane.setUI(new CustomTabbedPaneUI());      // ✅ dùng cùng custom UI

        tabbedPane.addTab("Nhân viên", new EmployeePanel(currentUser));
        tabbedPane.addTab("Khách hàng", new CustomerPanel());
        tabbedPane.addTab("Game", new GameManagePanel());
        tabbedPane.addTab("Sản phẩm", new ProductPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }
}
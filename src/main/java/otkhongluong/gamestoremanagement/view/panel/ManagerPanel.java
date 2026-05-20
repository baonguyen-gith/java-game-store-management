package otkhongluong.gamestoremanagement.view.panel;

import javax.swing.*;
import java.awt.*;

/**
 * ManagerPanel — tab "Quản trị" dành cho Quản lý (MaRole == 3).
 * Giống AdminPanel nhưng KHÔNG có tab Nhân viên.
 * View KHÔNG chứa business logic, KHÔNG gọi DBConnection trực tiếp.
 */
public class ManagerPanel extends JPanel {

    public ManagerPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 13, 35));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(13, 13, 35));
        tabbedPane.setOpaque(true);
        tabbedPane.setUI(new CustomTabbedPaneUI());

        tabbedPane.addTab("Khách hàng", new CustomerPanel());
        tabbedPane.addTab("Game",       new GameManagePanel());
        tabbedPane.addTab("Sản phẩm",  new ProductPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }
}
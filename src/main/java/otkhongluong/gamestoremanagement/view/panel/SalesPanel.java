package otkhongluong.gamestoremanagement.view.panel;


import javax.swing.*;
import java.awt.*;

public class SalesPanel extends JPanel {

    public SalesPanel() {

        setLayout(new BorderLayout());

        JTabbedPane tab = new JTabbedPane();

        tab.setBackground(new Color(13, 13, 35));     // nền tab bar
        tab.setOpaque(true);                          // 🔥 bắt buộc

        tab.setUI(new CustomTabbedPaneUI());

        tab.addTab("Lịch sử giao dịch", new TransactionPanel());
        tab.addTab("Quản lý hóa đơn", new BillPanel());
        tab.addTab("Quản lý phiếu thuê", new RentPanel());

        add(tab, BorderLayout.CENTER);
    }
}
package otkhongluong.gamestoremanagement.view.panel;

import javax.swing.*;
import java.awt.*;

public class EmployeeDashboardPanel extends JPanel {

    public EmployeeDashboardPanel() {

        setLayout(new BorderLayout());
        setOpaque(false);

        // ===== MAIN CARD =====
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(200,190,220));
        card.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        add(card, BorderLayout.CENTER);

        // ================= TOP =================
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        // ===== BADGE =====
        JLabel badge = new JLabel("ADMIN");
        badge.setOpaque(true);
        badge.setBackground(new Color(160,120,255));
        badge.setForeground(Color.BLACK);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        badge.setBorder(BorderFactory.createEmptyBorder(5,25,5,25));

        JPanel badgeWrapper =
                new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(badge);

        // ===== CLOSE =====
        top.add(badgeWrapper, BorderLayout.CENTER);

        card.add(top);
        card.add(Box.createVerticalStrut(15));

        // ================= AVATAR =================
        JLabel avatar = new JLabel("👩", SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(avatar);
        card.add(Box.createVerticalStrut(10));

        // ================= NAME =================
        JLabel name = new JLabel("Anne Hathaway");
        name.setFont(new Font("Segoe UI", Font.BOLD, 22));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(name);
        card.add(Box.createVerticalStrut(20));

        // ================= INFO =================
        card.add(createInfo("Mã nhân viên:", "100"));
        card.add(createInfo("Ngày sinh:", "1/1/2000"));
        card.add(createInfo("CCCD:", "xxxxxxxxxx"));
        card.add(createInfo("SĐT:", "xxxxxxxxxx"));

        card.add(Box.createVerticalStrut(20));

        // ================= DASHBOARD =================
        card.add(createCard("Doanh thu hôm nay", "2,500,000 VNĐ"));
        card.add(Box.createVerticalStrut(10));

        card.add(createCard("Doanh thu tuần", "12,000,000 VNĐ"));
        card.add(Box.createVerticalStrut(15));

        JPanel stats = new JPanel(new GridLayout(1,2,10,0));
        stats.setOpaque(false);

        stats.add(createCard("Hóa đơn hôm nay", "12"));
        stats.add(createCard("Phiếu thuê hôm nay", "5"));

        card.add(stats);
    }

    // ================= INFO ROW =================
    private JPanel createInfo(String title, String value){

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
        row.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        row.add(t);
        row.add(v);

        return row;
    }

    // ================= CARD =================
    private JPanel createCard(String title, String value){

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(90,95,140));
        card.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.LIGHT_GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 14));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }
}
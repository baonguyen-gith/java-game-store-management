package otkhongluong.gamestoremanagement.view;

import otkhongluong.gamestoremanagement.model.User;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JOptionPane;

public class StaffView extends JFrame {
    private User currentUser;
    
    public StaffView(User user) {
        this.currentUser = user;

        if (user.getMaRole() != 2) {
            JOptionPane.showMessageDialog(this, "Không có quyền!");
            dispose();
            return;
        }
        setTitle("Staff Dashboard");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel label = new JLabel("Xin chào NHÂN VIÊN", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 30));

        add(label);
    }
}

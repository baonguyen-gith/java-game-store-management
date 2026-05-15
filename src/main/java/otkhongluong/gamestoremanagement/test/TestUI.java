package otkhongluong.gamestoremanagement.test;

import otkhongluong.gamestoremanagement.AppNavigator;
import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.view.AdminView;

import javax.swing.UIManager;

public class TestUI {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        User fake = new User();
        fake.setMaUser(1);
        fake.setUsername("admin");
        fake.setMaRole(1);

        // ✅ AdminView cần Navigator để xử lý logout
        // Dùng AppNavigator — lớp implement Navigator duy nhất trong app
        new AdminView(fake, new AppNavigator()).setVisible(true);
    }
}
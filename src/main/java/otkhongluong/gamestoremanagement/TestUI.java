package otkhongluong.gamestoremanagement;

import otkhongluong.gamestoremanagement.view.AdminView;
import otkhongluong.gamestoremanagement.model.User;

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

        new AdminView(fake).setVisible(true);
    }
}
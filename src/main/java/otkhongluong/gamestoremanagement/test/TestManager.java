
package otkhongluong.gamestoremanagement.test;

import otkhongluong.gamestoremanagement.controller.Navigator;
import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.view.ManagerView;

import javax.swing.*;

public class TestManager {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // --- Stub user MaRole = 3 ---
            User fakeManager = new User();
            fakeManager.setMaNV(99);
            fakeManager.setUsername("test_manager");
            fakeManager.setPassword("test");
            fakeManager.setMaRole(3);

            // --- Stub Navigator: goToLogin() chỉ in log, không mở DB ---
            Navigator stubNavigator = new Navigator() {
                @Override
                public void goToLogin() {
                    System.out.println("[TestManager] goToLogin() called — stub, no action.");
                }

                @Override
                public void goToAdmin(User user) {
                    System.out.println("[TestManager] goToAdmin() called — stub, no action.");
                }

                @Override
                public void goToStaff(User user) {
                    System.out.println("[TestManager] goToStaff() called — stub, no action.");
                }

                @Override
                public void goToManager(User user) {
                    System.out.println("[TestManager] goToManager() called — stub, no action.");
                }
            };

            ManagerView view = new ManagerView(fakeManager, stubNavigator);
            view.setVisible(true);
        });
    }
}
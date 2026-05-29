
package otkhongluong.gamestoremanagement.test;

import otkhongluong.gamestoremanagement.controller.Navigator;
import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.view.ManagerView;

import javax.swing.*;

public class TestManager {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // --- Stub user MaRole = 2 ---
            User fakeManager = new User();
            fakeManager.setMaUser(2);
            fakeManager.setMaNV(2);
            fakeManager.setUsername("quanly");
            fakeManager.setPassword("$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu");
            fakeManager.setMaRole(2);

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

package otkhongluong.gamestoremanagement.test;

import otkhongluong.gamestoremanagement.controller.Navigator;
import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.view.StaffView;

import javax.swing.*;

/**
 * Test entry point — mở thẳng StaffView, bỏ qua login.
 *
 * Chạy class này để kiểm tra UI Staff (MaRole = 2)
 * mà không cần DB hay tài khoản thật.
 */
public class TestStaff {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // --- Stub user MaRole = 3 ---
            User fakeStaff = new User();
            fakeStaff.setMaUser(3);
            fakeStaff.setMaNV(3);
            fakeStaff.setUsername("nhanvien1");
            fakeStaff.setPassword("$2a$10$slYQmyNdgTY18LMoDIs11OHQEBsxGBsS3bT.T5ZZNGPDiPDy6JbTu");
            fakeStaff.setMaRole(3); 

            // --- Stub Navigator: goToLogin() chỉ in log, không mở DB ---
            Navigator stubNavigator = new Navigator() {
                @Override
                public void goToLogin() {
                    System.out.println("[TestStaff] goToLogin() called — stub, no action.");
                }

                @Override
                public void goToAdmin(User user) {
                    System.out.println("[TestStaff] goToAdmin() called — stub, no action.");
                }

                @Override
                public void goToStaff(User user) {
                    System.out.println("[TestStaff] goToStaff() called — stub, no action.");
                }

                @Override
                public void goToManager(User user) {
                    System.out.println("[TestStaff] goToManager() called — stub, no action.");
                }
            };

            StaffView view = new StaffView(fakeStaff, stubNavigator);
            view.setVisible(true);
        });
    }
}
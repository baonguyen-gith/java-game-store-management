package otkhongluong.gamestoremanagement;

import otkhongluong.gamestoremanagement.controller.LoginController;
import otkhongluong.gamestoremanagement.controller.Navigator;
import otkhongluong.gamestoremanagement.dao.UserDAO;
import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.service.AuthService;
import otkhongluong.gamestoremanagement.view.AdminView;
import otkhongluong.gamestoremanagement.view.LoginView;
import otkhongluong.gamestoremanagement.view.StaffView;

import javax.swing.*;

/**
 * Composition Root — nơi DUY NHẤT biết tất cả View/Controller/Service/DAO cụ thể.
 *
 * Main chỉ gọi: new AppNavigator().goToLogin()
 * Mọi layer khác (View, Controller, Service) chỉ biết interface Navigator.
 *
 * Luồng:
 *   goToLogin()  → LoginView  ← LoginController ← AuthService ← UserDAO
 *   goToAdmin()  → AdminView(user, navigator)
 *   goToStaff()  → StaffView(user, navigator)
 *   logout       → AdminView/StaffView gọi navigator.goToLogin() → quay lại LoginView
 */
public class AppNavigator implements Navigator {

    // AuthService được tạo một lần duy nhất, tái dùng cho mọi lần goToLogin()
    private final AuthService authService;

    public AppNavigator() {
        this.authService = new AuthService(new UserDAO());
    }

    // ── Entry point ───────────────────────────────────────────

    @Override
    public void goToLogin() {
        SwingUtilities.invokeLater(() -> {
            LoginController controller = new LoginController(authService, this);
            LoginView view = new LoginView(controller);
            view.setVisible(true);
        });
    }

    // ── Sau khi login thành công ──────────────────────────────

    @Override
    public void goToAdmin(User user) {
        SwingUtilities.invokeLater(() -> {
            AdminView view = new AdminView(user, this);
            view.setVisible(true);
        });
    }

    @Override
    public void goToStaff(User user) {
        SwingUtilities.invokeLater(() -> {
            StaffView view = new StaffView(user, this);
            view.setVisible(true);
        });
    }
}
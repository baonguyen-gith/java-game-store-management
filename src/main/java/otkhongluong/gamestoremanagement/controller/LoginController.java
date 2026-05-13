package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.service.AuthService;
import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.view.*;
import otkhongluong.gamestoremanagement.util.Session;

public class LoginController {
    private final AuthService authService;
    private LoginView view;

    public LoginController() {
    this.authService = new AuthService();
}

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    public void setView(LoginView view) {
        this.view = view;
    }

    // ✅ Controller quyết định điều hướng, không phải View
    public void handleLogin(String username, String password) {
        try {
            User user = authService.login(username, password);
            if (user == null) {
                view.showError("Sai tài khoản hoặc mật khẩu!");
                return;
            }
            Session.setMaNV(user.getMaUser());
            view.dispose();

            if (authService.isAdmin(user)) {
                new AdminView(user).setVisible(true);
            } else if (authService.isStaff(user)) {
                new StaffView(user).setVisible(true);
            } else {
                view.showError("Không xác định role!");
            }
        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        }
    }
}
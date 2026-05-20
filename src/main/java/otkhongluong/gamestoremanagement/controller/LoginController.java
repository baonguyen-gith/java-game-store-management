package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.dao.DatabaseException;
import otkhongluong.gamestoremanagement.dao.UserDAO;
import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.service.AuthService;
import otkhongluong.gamestoremanagement.util.Session;
import otkhongluong.gamestoremanagement.view.LoginView;

/**
 * Controller đăng nhập.
 * ✅ FIX: AuthService nhận UserDAO — truyền đúng dependency.
 */
public class LoginController {

    private final AuthService authService;
    private final Navigator   navigator;
    private LoginView view;
    
    /** Dependency Injection đầy đủ — dùng khi test hoặc cần mock AuthService. */
    public LoginController(AuthService authService, Navigator navigator) {
        this.authService = authService;
        this.navigator   = navigator;
    }

    /** LoginView gọi sau khi khởi tạo controller để tự đăng ký. */
    public void setView(LoginView view) {
        this.view = view;
    }

    public void handleLogin(String username, String password) {
        try {
            User user = authService.login(username, password);
            if (user == null) {
                view.showError("Sai tài khoản hoặc mật khẩu!");
                return;
            }

            Session.login(user.getMaNV(), user.getMaRole(), user.getUsername());
            view.dispose();

            if      (authService.isAdmin(user)) navigator.goToAdmin(user);
            else if (authService.isStaff(user)) navigator.goToStaff(user);
            else if (authService.isManager(user)) navigator.goToManager(user);
            else                                view.showError("Không xác định được quyền truy cập!");

        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        } catch (DatabaseException e) {
            view.showError("Lỗi hệ thống, vui lòng thử lại sau.\n(" + e.getMessage() + ")");
        }
    }
}
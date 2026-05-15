package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.dao.DatabaseException;
import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.service.AuthService;
import otkhongluong.gamestoremanagement.util.Session;
import otkhongluong.gamestoremanagement.view.LoginView;

/**
 * Controller đăng nhập.
 *
 * Thay đổi so với bản cũ:
 *  - Nhận AuthService qua constructor (không tự new bên trong)
 *  - Bắt thêm DatabaseException từ DAO để show lỗi hợp lý cho user
 *  - Session.setMaNV() vẫn ở đây vì đây là use-case login,
 *    nhưng được tách sau navigator.go*() để logic rõ ràng hơn
 *
 * ✅ Vẫn giữ: chỉ phụ thuộc AuthService + Navigator interface.
 * ✅ Vẫn giữ: không biết AdminView, StaffView hay View cụ thể nào.
 */
public class LoginController {

    private final AuthService authService;
    private final Navigator   navigator;
    private LoginView view;

    // ✅ Dependency Injection — nhận AuthService từ ngoài, không tự tạo
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

            Session.setMaNV(user.getMaUser());
            view.dispose();

            if      (authService.isAdmin(user)) navigator.goToAdmin(user);
            else if (authService.isStaff(user)) navigator.goToStaff(user);
            else                                view.showError("Không xác định được quyền truy cập!");

        } catch (IllegalArgumentException e) {
            // Validation lỗi (username/password rỗng)
            view.showError(e.getMessage());
        } catch (DatabaseException e) {
            // Lỗi kết nối / truy vấn DB
            view.showError("Lỗi hệ thống, vui lòng thử lại sau.\n(" + e.getMessage() + ")");
        }
    }
}